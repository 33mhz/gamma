package io.pnut.gamma.domain.model

import com.squareup.moshi.JsonClass
import io.pnut.gamma.util.Constants

@JsonClass(generateAdapter = true)
data class UserSuggestion(
    val id: String,
    val username: String,
    val name: String?,
    val accessToken: String? = null,
    val youFollow: Boolean = false,
) {
    val avatarUrl: String
        get() = if (accessToken.isNullOrEmpty()) {
            "${Constants.API_BASE_URL}users/$id/avatar?w=200"
        } else {
            "${Constants.API_BASE_URL}users/$id/avatar?w=200&access_token=$accessToken"
        }
}
