package io.pnut.gamma.service

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.provider.OpenableColumns
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
import io.pnut.gamma.domain.model.io.ReportPostInputData
import io.pnut.gamma.domain.model.io.RepostInputData
import io.pnut.gamma.domain.model.io.StarInputData
import io.pnut.gamma.domain.model.io.UploadFileInputData
import io.pnut.gamma.domain.usecases.CreatePollUseCase
import io.pnut.gamma.domain.usecases.DeletePostUseCase
import io.pnut.gamma.domain.usecases.PostUseCase
import io.pnut.gamma.domain.usecases.ReportPostUseCase
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
    private val reportPostUseCase: ReportPostUseCase,
    private val createPollUseCase: CreatePollUseCase
) : CoroutineWorker(context, params) {

    enum class Actions {
        SendPost, Star, Repost, DeletePost, ReportPost;

        fun getActionName() = "$actionPrefix.$name"

        companion object {
            fun getAction(actionName: String): Actions? {
                return try {
                    valueOf(actionName.split(".").last())
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    private enum class IntentKey { PostBody, PostId, NewState, ReportReason, AccountId }
    enum class ResultIntentKey { Post }

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
                        val fileName = getFileName(it.uri)
                        val res = uploadFileUseCase.run(UploadFileInputData(it, inputStream, fileName)).postOEmbedRaw
                        inputStream?.close()

                        // Cleanup cache file
                        if (it.uri.scheme == "file") {
                            it.uri.path?.let { path -> java.io.File(path).delete() }
                        }

                        res
                    }
                postBodyOuter.pollPostBody?.let { it ->
                    LogUtil.d("pollPostBody $it")
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
            Actions.ReportPost.getActionName() -> {
                val postId = inputData.getString(IntentKey.PostId.name) ?: return Result.failure()
                val reasonString = inputData.getString(IntentKey.ReportReason.name) ?: return Result.failure()
                val accountId = inputData.getString(IntentKey.AccountId.name) ?: return Result.failure()
                val reason = io.pnut.gamma.domain.entity.ReportReason.valueOf(reasonString)
                runCatching {
                    reportPostUseCase.run(ReportPostInputData(postId, reason, accountId))
                    createResultIntent(action)
                }
            }
            else -> return Result.failure()
        }
        
        val responseIntent =
            resultIntent.getOrElse { ErrorIntent.createErrorIntent(applicationContext, it) }
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(responseIntent)
        
        return Result.success()
    }

    private fun sendErrorBroadcast(t: Throwable): Result {
        val intent = ErrorIntent.createErrorIntent(applicationContext, t)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        return Result.failure()
    }

    private fun createResultIntent(action: String): Intent {
        return Intent().also {
            it.action = action
        }
    }

    private fun getFileName(uri: Uri): String? {
        if (uri.scheme == "content") {
            applicationContext.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        return cursor.getString(index)
                    }
                }
            }
        }
        return uri.path?.let { java.io.File(it).name }
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

        fun enqueueReportPost(context: Context, postId: String, reason: io.pnut.gamma.domain.entity.ReportReason, accountId: String) {
            val data = Data.Builder()
                .putString(ACTION_KEY, Actions.ReportPost.getActionName())
                .putString(IntentKey.PostId.name, postId)
                .putString(IntentKey.ReportReason.name, reason.name)
                .putString(IntentKey.AccountId.name, accountId)
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

        fun sendResultBroadcast(context: Context, action: Actions, post: Post) {
            val intent = Intent(action.getActionName()).apply {
                putExtra(ResultIntentKey.Post.name, post)
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }
}
