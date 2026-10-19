package io.pnut.gamma.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.IntentCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.pnut.gamma.domain.entity.Message

class MessageReceiver(private val listener: Callback) : BroadcastReceiver() {

    interface Callback {
        fun onMessageReceive(message: Message)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_MESSAGE_RECEIVE) {
            val message = IntentCompat.getParcelableExtra(intent, EXTRA_MESSAGE, Message::class.java) ?: return
            listener.onMessageReceive(message)
        }
    }

    companion object {
        const val ACTION_MESSAGE_RECEIVE = "io.pnut.gamma.broadcast.MessageReceiver.ACTION_MESSAGE_RECEIVE"
        const val EXTRA_MESSAGE = "EXTRA_MESSAGE"

        fun getIntentFilter(): IntentFilter {
            return IntentFilter(ACTION_MESSAGE_RECEIVE)
        }

        fun broadcast(context: Context, message: Message) {
            val intent = Intent(ACTION_MESSAGE_RECEIVE).apply {
                putExtra(EXTRA_MESSAGE, message)
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }
}
