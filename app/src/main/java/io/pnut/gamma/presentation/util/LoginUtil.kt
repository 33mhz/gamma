package io.pnut.gamma.presentation.util

import android.content.Context
import android.content.Intent
import io.pnut.gamma.R
import io.pnut.gamma.domain.entity.Token
import androidx.core.net.toUri

object LoginUtil {
    private val scopes = arrayOf(
        Token.Scope.BASIC,
        Token.Scope.STREAM,
        Token.Scope.WRITE_POST,
        Token.Scope.FOLLOW,
        Token.Scope.UPDATE_PROFILE,
        Token.Scope.PRESENCE,
        Token.Scope.MESSAGES_CHAT,
        Token.Scope.MESSAGES_PM,
        Token.Scope.FILES_DELTA
    )


    private fun createLoginURL(context: Context): String {
        val clientId = context.getString(R.string.client_id)
        val scopeStr = scopes.joinToString(",")
        return context.getString(R.string.authenticate_url, clientId, scopeStr)
    }

    fun getLoginIntent(context: Context): Intent {
        val url = createLoginURL(context)
        return Intent(Intent.ACTION_VIEW, url.toUri())
    }

    fun launchLogin(context: Context, isEphemeral: Boolean = false) {
        val url = createLoginURL(context)
        Util.openCustomTabUrl(context, url, isEphemeral)
    }
}