package io.pnut.gamma.broadcast

import android.content.Context
import io.pnut.gamma.domain.entity.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MessageReceiver(private val listener: Callback) {

    interface Callback {
        fun onMessageReceive(message: Message)
    }

    private var job: Job? = null

    fun register(coroutineScope: CoroutineScope) {
        unregister()
        job = coroutineScope.launch {
            AppEventBus.messageEvents.collect { message ->
                listener.onMessageReceive(message)
            }
        }
    }

    fun unregister() {
        job?.cancel()
        job = null
    }

    companion object {
        fun broadcast(context: Context? = null, message: Message) {
            AppEventBus.send(message)
        }
    }
}
