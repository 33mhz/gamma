package io.pnut.gamma.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class RelationshipReceiver(private val listener: Callback) : BroadcastReceiver() {

    interface Callback {
        fun onRelationshipChanged()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_RELATIONSHIP_CHANGED) {
            listener.onRelationshipChanged()
        }
    }

    companion object {
        const val ACTION_RELATIONSHIP_CHANGED = "io.pnut.gamma.ACTION_RELATIONSHIP_CHANGED"

        fun getIntentFilter(): IntentFilter {
            return IntentFilter(ACTION_RELATIONSHIP_CHANGED)
        }

        fun broadcast(context: Context) {
            val intent = Intent(ACTION_RELATIONSHIP_CHANGED)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }
}
