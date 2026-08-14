package io.pnut.gamma.presentation.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.pnut.gamma.data.db.dao.CacheDao
import io.pnut.gamma.domain.model.UserSuggestion
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.repository.IPreferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MentionViewModelDelegate(
    private val cacheDao: CacheDao,
    private val accountRepository: IAccountRepository,
    private val preferenceRepository: IPreferenceRepository,
    private val coroutineScope: CoroutineScope
) {
    private val _suggestions = MutableLiveData<List<UserSuggestion>>()
    val suggestions: LiveData<List<UserSuggestion>> = _suggestions

    private var searchJob: Job? = null

    fun onTextChanged(text: String, selectionStart: Int, requireAtSymbol: Boolean = true) {
        if (!preferenceRepository.usernameAutocomplete) {
            _suggestions.value = emptyList()
            searchJob?.cancel()
            return
        }

        val query = extractMentionQuery(text, selectionStart, requireAtSymbol)
        if (query == null) {
            _suggestions.value = emptyList()
            searchJob?.cancel()
            return
        }

        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            try {
                val currentUserId = accountRepository.getDefaultAccount()?.id ?: ""
                val result = cacheDao.searchSuggestions(query, currentUserId)
                val token = accountRepository.getDefaultAccount()?.token
                _suggestions.value = result.map {
                    UserSuggestion(it.id, it.username, it.name, token, it.youFollow)
                }.sortedBy { it.username.lowercase() }
            } catch (_: Exception) {
                _suggestions.value = emptyList()
            }
        }
    }

    private fun extractMentionQuery(text: String, selectionStart: Int, requireAtSymbol: Boolean): String? {
        if (selectionStart == 0) return null
        
        val subText = text.substring(0, selectionStart)
        
        val mentionText = if (requireAtSymbol) {
            val lastAtPos = subText.lastIndexOf('@')
            if (lastAtPos == -1) return null

            // Check if there's a space, comma or if it's the start of the string
            if (lastAtPos > 0 && !subText[lastAtPos - 1].isWhitespace() && subText[lastAtPos - 1] != ',') return null

            subText.substring(lastAtPos + 1)
        } else {
            val lastDelimiterPos = maxOf(subText.lastIndexOf(' '), subText.lastIndexOf(','))
            subText.substring(lastDelimiterPos + 1).trimStart('@')
        }
        
        // Don't show suggestions until at least two characters are typed
        if (mentionText.length < 2) return null

        // Username regex: alphanumeric + underscore, up to 20 chars
        val regex = Regex("^[a-zA-Z0-9_]{2,20}$")
        return if (regex.matches(mentionText)) mentionText else null
    }
}
