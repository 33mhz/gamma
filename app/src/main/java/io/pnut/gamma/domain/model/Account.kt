package io.pnut.gamma.domain.model
import com.squareup.moshi.JsonClass
import io.pnut.gamma.util.Constants

@JsonClass(generateAdapter = true)
data class Account(
    val id: String,
    val token: String,
    val screenName: String,
    val name: String
) {
    fun getAvatarUrl(size: Int = 96) = Constants.API_BASE_URL + "users/$id/avatar?h=$size&w=$size"
    val usernameWithAt = "@$screenName"
}