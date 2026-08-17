package io.pnut.gamma.domain.model

sealed class UserListType {
    data class Following(val userId: String) : UserListType()
    data class Followers(val userId: String) : UserListType()
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