package io.pnut.gamma.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.pnut.gamma.util.ErrorIntent

class ErrorReceiver(private val listener: Callback) : BroadcastReceiver() {

    interface Callback {
        fun onReceiveError(message: String)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val message = intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        if (ErrorIntent.ACTION == action) {
            listener.onReceiveError(message)
        }
    }
}
