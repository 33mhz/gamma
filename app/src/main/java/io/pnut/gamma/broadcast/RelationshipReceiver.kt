package io.pnut.gamma.broadcast

import android.content.Context
import io.pnut.gamma.domain.Relationship
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RelationshipReceiver(private val listener: Callback) {

    interface Callback {
        fun onRelationshipChanged(userId: String?, relationship: Relationship?)
    }

    private var job: Job? = null

    fun register(coroutineScope: CoroutineScope) {
        unregister()
        job = coroutineScope.launch {
            AppEventBus.relationshipEvents.collect { event ->
                listener.onRelationshipChanged(event.userId, event.relationship)
            }
        }
    }

    fun unregister() {
        job?.cancel()
        job = null
    }

    companion object {
        fun broadcast(context: Context? = null, userId: String? = null, relationship: Relationship? = null) {
            AppEventBus.send(userId, relationship)
        }
    }
}
