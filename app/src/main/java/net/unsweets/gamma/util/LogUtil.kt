package net.unsweets.gamma.util

import android.util.Log
import net.unsweets.gamma.BuildConfig


object LogUtil {
    private val TAG: String = LogUtil::class.java.canonicalName ?: "LogUtil"
    fun e(message: String? = "") {
        if (BuildConfig.DEBUG) Log.e(TAG, message.orEmpty())
    }

    fun d(message: String? = "") {
        if (BuildConfig.DEBUG) Log.d(TAG, message.orEmpty())
    }

    fun i(message: String? = "") {
        if (BuildConfig.DEBUG) Log.i(TAG, message.orEmpty())
    }

    fun w(message: String? = "") {
        if (BuildConfig.DEBUG) Log.w(TAG, message.orEmpty())
    }

    fun v(message: String? = "") {
        if (BuildConfig.DEBUG) Log.v(TAG, message.orEmpty())
    }
}
