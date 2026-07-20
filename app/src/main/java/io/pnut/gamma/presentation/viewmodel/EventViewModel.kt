package io.pnut.gamma.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.pnut.gamma.util.SingleLiveEvent

open class EventViewModel<T>: ViewModel() {
    private val _event = SingleLiveEvent<T>()
    val event: LiveData<T> = _event

    fun sendEvent(sendEvent: T) {
        _event.value = sendEvent
    }

}