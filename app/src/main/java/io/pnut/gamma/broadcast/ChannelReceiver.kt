package io.pnut.gamma.broadcast

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChannelReceiver(private val listener: Callback) {

    interface Callback {
        fun onChannelRead(channelId: String)
    }

    private var job: Job? = null

    fun register(coroutineScope: CoroutineScope) {
        unregister()
        job = coroutineScope.launch {
            AppEventBus.channelEvents.collect { event ->
                when (event) {
                    is AppEventBus.ChannelEvent.ChannelRead -> listener.onChannelRead(event.channelId)
                }
            }
        }
    }

    fun unregister() {
        job?.cancel()
        job = null
    }

    companion object {
        fun broadcastRead(channelId: String) {
            AppEventBus.send(AppEventBus.ChannelEvent.ChannelRead(channelId))
        }
    }
}
