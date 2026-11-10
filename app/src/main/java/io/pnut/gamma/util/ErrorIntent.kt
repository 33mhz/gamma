package io.pnut.gamma.util

import android.content.Context
import io.pnut.gamma.broadcast.AppEventBus

object ErrorIntent {
    fun createErrorMessage(context: Context, t: Throwable?): String {
        LogUtil.d(t?.toString().orEmpty())
        return if (t != null) {
            ErrorCollections.getErrorMessage(context, t)
        } else {
            Constants.UNKNOWN_ERROR
        }
    }

    fun broadcast(context: Context, t: Throwable) {
        val errorMessage = createErrorMessage(context, t)
        AppEventBus.sendError(errorMessage)
    }
}
