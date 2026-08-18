package io.pnut.gamma.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.pnut.gamma.R
import io.pnut.gamma.data.db.dao.CacheDao
import io.pnut.gamma.domain.entity.IDs
import io.pnut.gamma.domain.entity.PmPostBody
import io.pnut.gamma.domain.entity.PollPostBody
import io.pnut.gamma.domain.entity.raw.OEmbed
import io.pnut.gamma.domain.entity.raw.PollNotice
import io.pnut.gamma.domain.entity.raw.RawValue
import io.pnut.gamma.domain.entity.raw.Spoiler
import io.pnut.gamma.domain.entity.raw.replacement.PostPoll
import io.pnut.gamma.domain.model.UriInfo
import io.pnut.gamma.domain.model.io.CreatePmMessageInputData
import io.pnut.gamma.domain.model.io.CreatePollInputData
import io.pnut.gamma.domain.model.io.GetExistingPmInputData
import io.pnut.gamma.domain.model.io.UploadFileInputData
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.repository.IPreferenceRepository
import io.pnut.gamma.domain.usecases.CreatePmMessageUseCase
import io.pnut.gamma.domain.usecases.CreatePollUseCase
import io.pnut.gamma.domain.usecases.GetExistingPmUseCase
import io.pnut.gamma.domain.usecases.UploadFileUseCase
import io.pnut.gamma.presentation.util.MentionViewModelDelegate
import io.pnut.gamma.util.Constants
import io.pnut.gamma.util.LogUtil
import io.pnut.gamma.util.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class NewPrivateMessageViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getExistingPmUseCase: GetExistingPmUseCase,
    private val createPmMessageUseCase: CreatePmMessageUseCase,
    private val uploadFileUseCase: UploadFileUseCase,
    private val createPollUseCase: CreatePollUseCase,
    cacheDao: CacheDao,
    accountRepository: IAccountRepository,
    preferenceRepository: IPreferenceRepository
) : ViewModel() {

    private val mentionDelegate = MentionViewModelDelegate(cacheDao, accountRepository, preferenceRepository, viewModelScope)
    val suggestions = mentionDelegate.suggestions
    
    fun onTextChanged(text: String, selectionStart: Int, requireAtSymbol: Boolean = true) {
        mentionDelegate.onTextChanged(text, selectionStart, requireAtSymbol)
    }

    val usernames = MutableLiveData("")
    val text = MutableLiveData("")
    val counter = text.map {
        val messageText = it ?: ""
        Constants.MAX_MESSAGE_TEXT_LENGTH - messageText.codePointCount(0, messageText.length)
    }

    val nsfw = MutableLiveData(false)
    val enablePoll = MutableLiveData(false)
    val spoiler = MutableLiveData<Spoiler?>(null)
    var media: List<UriInfo> = emptyList()

    val loading = MutableLiveData(false)
    val status = MutableLiveData<String?>(null)

    val event = SingleLiveEvent<Event>()

    fun onLookup(context: Context) {
        val idsList = processUsernames(usernames.value ?: "")
        if (idsList.isEmpty()) return

        viewModelScope.launch {
            loading.value = true
            status.value = context.getString(R.string.loading_now)
            runCatching {
                getExistingPmUseCase.run(GetExistingPmInputData(IDs(idsList)))
            }.onSuccess {
                loading.value = false
                val usernames = idsList.map { it.trimStart('@') }
                event.value = Event.NavigateToChannel(it.res.data.id, idsList.joinToString(", "), usernames)
            }.onFailure {
                loading.value = false
                event.value = Event.Error(it)
            }
        }
    }

    fun onSend(context: Context, pollPostBody: PollPostBody?) {
        val idsList = processUsernames(usernames.value ?: "")
        val messageText = text.value ?: ""
        if (idsList.isEmpty()) return

        viewModelScope.launch {
            loading.value = true
            status.value = context.getString(R.string.uploading_images)
            try {
                val cachedFiles = withContext(Dispatchers.IO) {
                    media.mapNotNull { uriInfo ->
                        copyUriToCache(context, uriInfo.uri)
                    }
                }

                val raw = mutableMapOf<String, MutableList<RawValue>>()
                spoiler.value?.let {
                    raw.getOrPut(Spoiler.TYPE) { mutableListOf() }.add(it)
                }

                val replacementFileRawList = withContext(Dispatchers.IO) {
                    cachedFiles.map {
                        val inputStream = context.contentResolver.openInputStream(it.uri)
                        val fileName = getFileName(context, it.uri)
                        val res = uploadFileUseCase.run(UploadFileInputData(it, inputStream, fileName)).postOEmbedRaw
                        inputStream?.close()

                        // Cleanup cache file
                        if (it.uri.scheme == "file") {
                            it.uri.path?.let { path -> File(path).delete() }
                        }

                        res
                    }
                }

                pollPostBody?.let {
                    status.value = context.getString(R.string.creating_poll)
                    val res = withContext(Dispatchers.IO) {
                        createPollUseCase.run(CreatePollInputData(it))
                    }
                    val pollNotice = PostPoll.createFromPoll(res.poll)
                    raw.getOrPut(PollNotice.TYPE) { mutableListOf() }.add(pollNotice)
                }

                replacementFileRawList.forEach {
                    raw.getOrPut(OEmbed.TYPE) { mutableListOf() }.add(it)
                }

                val messageBody = PmPostBody(
                    text = messageText,
                    destinations = idsList,
                    isNsfw = nsfw.value,
                    raw = raw.toMap()
                )

                status.value = context.getString(R.string.creating_message)
                val res = withContext(Dispatchers.IO) {
                    createPmMessageUseCase.run(CreatePmMessageInputData(messageBody))
                }
                val usernames = idsList.map { it.trimStart('@') }
                event.value = Event.NavigateToChannel(res.res.data.channelId, idsList.joinToString(", "), usernames)
            } catch (e: Exception) {
                LogUtil.e(e.message)
                event.value = Event.Error(e)
            } finally {
                loading.value = false
                status.value = null
            }
        }
    }

    private fun copyUriToCache(context: Context, uri: Uri): UriInfo? {
        if (uri.scheme != "content" || uri.authority == "io.pnut.gamma.fileprovider") return UriInfo(uri)
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileName(context, uri) ?: return null
            val cacheFile = File(context.cacheDir, fileName)
            cacheFile.outputStream().use { outputStream ->
                inputStream.use { it.copyTo(outputStream) }
            }
            UriInfo(Uri.fromFile(cacheFile))
        } catch (e: Exception) {
            LogUtil.e(e.message)
            null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index != -1) return cursor.getString(index)
                }
            }
        }
        return uri.lastPathSegment
    }

    private fun processUsernames(input: String): List<String> {
        return input.split(Regex("[,\\s]+"))
            .map { it.trim().trimStart('@') }
            .filter { it.isNotBlank() }
            .map { "@$it" }
            .distinct()
    }

    sealed class Event {
        data class NavigateToChannel(val channelId: String, val title: String, val usernames: List<String>) : Event()
        data class Error(val throwable: Throwable) : Event()
    }
}
