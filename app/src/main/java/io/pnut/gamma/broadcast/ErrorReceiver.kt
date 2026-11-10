package io.pnut.gamma.broadcast

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ErrorReceiver(private val listener: Callback) {

    interface Callback {
        fun onReceiveError(message: String)
    }

    private var job: Job? = null

    fun register(coroutineScope: CoroutineScope) {
        unregister()
        job = coroutineScope.launch {
            AppEventBus.errorEvents.collect { message ->
                listener.onReceiveError(message)
            }
        }
    }

    fun unregister() {
        job?.cancel()
        job = null
    }
}
