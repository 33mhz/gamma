package io.pnut.gamma.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.pnut.gamma.domain.Relationship

class RelationshipReceiver(private val listener: Callback) : BroadcastReceiver() {

    interface Callback {
        fun onRelationshipChanged(userId: String?, relationship: Relationship?)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_RELATIONSHIP_CHANGED) {
            val userId = intent.getStringExtra(EXTRA_USER_ID)
            val relationship = intent.getSerializableExtra(EXTRA_RELATIONSHIP) as? Relationship
            listener.onRelationshipChanged(userId, relationship)
        }
    }

    companion object {
        const val ACTION_RELATIONSHIP_CHANGED = "io.pnut.gamma.ACTION_RELATIONSHIP_CHANGED"
        const val EXTRA_USER_ID = "EXTRA_USER_ID"
        const val EXTRA_RELATIONSHIP = "EXTRA_RELATIONSHIP"

        fun getIntentFilter(): IntentFilter {
            return IntentFilter(ACTION_RELATIONSHIP_CHANGED)
        }

        fun broadcast(context: Context, userId: String? = null, relationship: Relationship? = null) {
            val intent = Intent(ACTION_RELATIONSHIP_CHANGED).apply {
                putExtra(EXTRA_USER_ID, userId)
                putExtra(EXTRA_RELATIONSHIP, relationship)
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }
}
