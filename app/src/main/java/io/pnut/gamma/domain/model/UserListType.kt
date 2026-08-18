package io.pnut.gamma.domain.model

import com.squareup.moshi.JsonClass

sealed class UserListType {
    @JsonClass(generateAdapter = true)
    data class Following(val userId: String) : UserListType()
    @JsonClass(generateAdapter = true)
    data class Followers(val userId: String) : UserListType()
    @JsonClass(generateAdapter = true)
    data class Search(val keyword: String) : UserListType()
    object Blocked : UserListType()
    object Muted : UserListType()
    object Suggested : UserListType()

    val categoryName: String
        get() {
            val identifier = when (this) {
                is Followers -> userId
                is Following -> userId
                is Search -> keyword
                else -> "me"
            }
            return "${this::class.java.simpleName}/$identifier"
        }
}