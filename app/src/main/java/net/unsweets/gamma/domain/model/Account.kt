package net.unsweets.gamma.domain.model
import net.unsweets.gamma.util.Constants.API_BASE_URL

data class Account(
    val id: String,
    val token: String,
    val screenName: String,
    val name: String
) {
    fun getAvatarUrl(size: Int = 96) = API_BASE_URL + "users/$id/avatar?h=$size&w=$size"
    val usernameWithAt = "@$screenName"
}