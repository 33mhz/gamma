package io.pnut.gamma.service

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.IntentCompat
import androidx.hilt.work.HiltWorker
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.domain.entity.PostBodyOuter
import io.pnut.gamma.domain.entity.raw.OEmbed
import io.pnut.gamma.domain.entity.raw.PollNotice
import io.pnut.gamma.domain.entity.raw.replacement.PostPoll
import io.pnut.gamma.domain.model.io.CreatePollInputData
import io.pnut.gamma.domain.model.io.DeletePostInputData
import io.pnut.gamma.domain.model.io.PostInputData
import io.pnut.gamma.domain.model.io.RepostInputData
import io.pnut.gamma.domain.model.io.StarInputData
import io.pnut.gamma.domain.model.io.UploadFileInputData
import io.pnut.gamma.domain.usecases.CreatePollUseCase
import io.pnut.gamma.domain.usecases.DeletePostUseCase
import io.pnut.gamma.domain.usecases.PostUseCase
import io.pnut.gamma.domain.usecases.RepostUseCase
import io.pnut.gamma.domain.usecases.StarUseCase
import io.pnut.gamma.domain.usecases.UploadFileUseCase
import io.pnut.gamma.util.ErrorIntent
import io.pnut.gamma.util.LogUtil
import io.pnut.gamma.util.MoshiSingleton
import io.pnut.gamma.domain.entity.raw.RawValue

private const val actionPrefix = "io.pnut.gamma.service.PostService"

@HiltWorker
class PostWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val postUseCase: PostUseCase,
    private val starUseCase: StarUseCase,
    private val repostUseCase: RepostUseCase,
    private val uploadFileUseCase: UploadFileUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val createPollUseCase: CreatePollUseCase
) : CoroutineWorker(context, params) {

    enum class Actions {
        SendPost, Star, Repost, DeletePost;

        fun getActionName() = "$actionPrefix.$name"

        companion object {
            fun getAction(actionName: String): Actions? {
                return try {
                    valueOf(actionName.split(".").last())
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    private enum class IntentKey { PostBody, PostId, NewState }
    private enum class ResultIntentKey { Post }

    override suspend fun doWork(): Result {
        val action = inputData.getString(ACTION_KEY) ?: return Result.failure()
        val resultIntent = when (action) {
            Actions.SendPost.getActionName() -> {
                val postBodyOuterJson = inputData.getString(IntentKey.PostBody.name) ?: return Result.failure()
                val postBodyOuter = MoshiSingleton.moshi.adapter(PostBodyOuter::class.java).fromJson(postBodyOuterJson) ?: return Result.failure()
                
                val raw = mutableMapOf<String, MutableList<RawValue>>()
                postBodyOuter.postBody.raw?.let {
                    it.forEach { (type, list) ->
                        raw.getOrPut(type) { mutableListOf() }.addAll(list)
                    }
                }
                val replacementFileRawList = postBodyOuter.files
                    .map {
                        val inputStream = applicationContext.contentResolver.openInputStream(it.uri)
                        val res = uploadFileUseCase.run(UploadFileInputData(it, inputStream)).postOEmbedRaw
                        inputStream?.close()
                        res
                    }
                postBodyOuter.pollPostBody?.let { it ->
                    LogUtil.e("pollPostBody $it")
                    runCatching {
                        createPollUseCase.run(CreatePollInputData(it))
                    }.onFailure {
                        return sendErrorBroadcast(it)
                    }.onSuccess {
                        val pollNotice = PostPoll.createFromPoll(it.poll)
                        raw.getOrPut(PollNotice.TYPE) { mutableListOf() }.add(pollNotice)
                    }
                }

                replacementFileRawList.forEach {
                    raw.getOrPut(OEmbed.TYPE) { mutableListOf() }.add(it)
                }
                val modifiedPostBody = postBodyOuter.postBody.copy(raw = raw)
                runCatching {
                    val postOutputData =
                        postUseCase.run(PostInputData(modifiedPostBody, postBodyOuter.accountId))
                    createResultIntent(action).putExtra(
                        ResultIntentKey.Post.name,
                        postOutputData.res.data
                    )
                }
            }
            Actions.Star.getActionName() -> {
                val postId = inputData.getString(IntentKey.PostId.name) ?: return Result.failure()
                val newState = inputData.getBoolean(IntentKey.NewState.name, true)
                runCatching {
                    val postOutputData = starUseCase.run(StarInputData(postId, newState))
                    createResultIntent(action).putExtra(
                        ResultIntentKey.Post.name,
                        postOutputData.res.data
                    )
                }
            }
            Actions.Repost.getActionName() -> {
                val postId = inputData.getString(IntentKey.PostId.name) ?: return Result.failure()
                val newState = inputData.getBoolean(IntentKey.NewState.name, true)
                runCatching {
                    val postOutputData = repostUseCase.run(
                        RepostInputData(
                            postId,
                            newState
                        )
                    )
                    createResultIntent(action).putExtra(
                        ResultIntentKey.Post.name,
                        postOutputData.res.data
                    )
                }
            }
            Actions.DeletePost.getActionName() -> {
                val postId = inputData.getString(IntentKey.PostId.name) ?: return Result.failure()
                runCatching {
                    val postOutputData = deletePostUseCase.run(DeletePostInputData(postId))
                    createResultIntent(action).putExtra(
                        ResultIntentKey.Post.name,
                        postOutputData.res.data
                    )
                }
            }
            else -> return Result.failure()
        }
        
        val responseIntent =
            resultIntent.getOrDefault(ErrorIntent.createErrorIntent(resultIntent.exceptionOrNull()))
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(responseIntent)
        
        return Result.success()
    }

    private fun sendErrorBroadcast(t: Throwable): Result {
        val intent = ErrorIntent.createErrorIntent(t)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        return Result.failure()
    }

    private fun createResultIntent(action: String): Intent {
        return Intent().also {
            it.action = action
        }
    }

    companion object {
        private const val ACTION_KEY = "action"

        fun enqueueSendPost(context: Context, postBodyOuter: PostBodyOuter) {
            val json = MoshiSingleton.moshi.adapter(PostBodyOuter::class.java).toJson(postBodyOuter)
            val data = Data.Builder()
                .putString(ACTION_KEY, Actions.SendPost.getActionName())
                .putString(IntentKey.PostBody.name, json)
                .build()
            val request = OneTimeWorkRequestBuilder<PostWorker>()
                .setInputData(data)
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun enqueueStar(context: Context, postId: String, newState: Boolean) {
            val data = Data.Builder()
                .putString(ACTION_KEY, Actions.Star.getActionName())
                .putString(IntentKey.PostId.name, postId)
                .putBoolean(IntentKey.NewState.name, newState)
                .build()
            val request = OneTimeWorkRequestBuilder<PostWorker>()
                .setInputData(data)
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun enqueueRepost(context: Context, postId: String, newState: Boolean) {
            val data = Data.Builder()
                .putString(ACTION_KEY, Actions.Repost.getActionName())
                .putString(IntentKey.PostId.name, postId)
                .putBoolean(IntentKey.NewState.name, newState)
                .build()
            val request = OneTimeWorkRequestBuilder<PostWorker>()
                .setInputData(data)
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun enqueueDeletePost(context: Context, postId: String) {
            val data = Data.Builder()
                .putString(ACTION_KEY, Actions.DeletePost.getActionName())
                .putString(IntentKey.PostId.name, postId)
                .build()
            val request = OneTimeWorkRequestBuilder<PostWorker>()
                .setInputData(data)
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun getIntentFilter() = IntentFilter().also { intentFilter ->
            Actions.entries.forEach {
                intentFilter.addAction(it.getActionName())
            }
        }

        fun getPost(intent: Intent): Post? =
            IntentCompat.getParcelableExtra(intent, ResultIntentKey.Post.name, Post::class.java)
    }
}
