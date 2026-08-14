package io.pnut.gamma.presentation.viewmodel

import androidx.lifecycle.*
import io.pnut.gamma.domain.entity.Token
import io.pnut.gamma.domain.model.io.GetAuthenticatedUserInputData
import io.pnut.gamma.domain.usecases.GetAuthenticatedUserUseCase
import io.pnut.gamma.presentation.activity.MainActivity
import kotlinx.coroutines.launch

class MainActivityViewModel(private val getAuthenticatedUserUseCase: GetAuthenticatedUserUseCase) : EventViewModel<MainActivity.Event>() {
    private val token = MutableLiveData<Token>()
    val user = token.map { it?.user }
    val showAccountMenu = MutableLiveData<Boolean>().apply { value = false }
    init {
        getUserInfo()
    }

    fun refresh() {
        getUserInfo()
    }

    private fun getUserInfo() {
        viewModelScope.launch {
            runCatching {
                getAuthenticatedUserUseCase.run(GetAuthenticatedUserInputData(token))
            }.onFailure {
                sendEvent(MainActivity.Event.Failed(it))
            }
        }
    }

    fun openMyProfile() {
        val user = user.value ?: return
        sendEvent(MainActivity.Event.OpenMyProfile(user))
    }
    fun composePost() {
        sendEvent(MainActivity.Event.ComposePost)
    }

    fun toggleNavigationViewMenu() {
        showAccountMenu.value = !(showAccountMenu.value ?: false)
    }
    class Factory(private val getAuthenticatedUserUseCase: GetAuthenticatedUserUseCase) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel(getAuthenticatedUserUseCase) as T
        }
    }
}