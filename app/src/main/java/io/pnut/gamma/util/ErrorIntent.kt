package io.pnut.gamma.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.pnut.gamma.BuildConfig

object ErrorIntent {
    const val ACTION = "${BuildConfig.APPLICATION_ID}.Error"
    fun createErrorIntent(context: Context, t: Throwable?): Intent {
        LogUtil.d(t.toString())
        val message: String = if (t != null) {
            ErrorCollections.getErrorMessage(context, t)
        } else {
            Constants.UNKNOWN_ERROR
        }
        return Intent().also {
            it.action = ACTION
            it.putExtra(Intent.EXTRA_TEXT, message)
        }
    }

    fun getIntentFilter() = IntentFilter().also {
        it.addAction(ACTION)
    }

    fun broadcast(context: Context, t: Throwable) {
        val errorIntent = createErrorIntent(context, t)
        LocalBroadcastManager.getInstance(context).sendBroadcast(errorIntent)
    }

}