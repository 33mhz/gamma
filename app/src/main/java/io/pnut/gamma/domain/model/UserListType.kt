package io.pnut.gamma.domain.model

sealed class UserListType {
    data class Following(val userId: String) : UserListType()
    data class Followers(val userId: String) : UserListType()
    data class Search(val keyword: String) : UserListType()
    object Blocked : UserListType()
    object Muted : UserListType()

    val categoryName: String
        get() {
            val userId = when (this) {
                is Followers -> userId
                is Following -> userId
                else -> "me"
            }
            return "${this::class.java.simpleName}/$userId"
        }
}