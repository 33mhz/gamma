package io.pnut.gamma.domain.model

import androidx.annotation.StringRes
import io.pnut.gamma.R

sealed class StreamType {
    object Home: StreamType()
    object Mentions: StreamType()
    data class Stars(val userId: String): StreamType()
    data class Tag(val tag: String): StreamType()
    data class User(val userId: String): StreamType()
    data class Thread(val postId: String) : StreamType()
    sealed class Explore(@StringRes val titleRes: Int, val slug: String): StreamType() {
        object Conversations : Explore(R.string.conversations, "conversations")
        object MissedConversations : Explore(R.string.missed_conversations, "missed_conversations")
        object Newcomers : Explore(R.string.newcomers, "newcomers")
        object Photos : Explore(R.string.photos, "photos")
        object Trending : Explore(R.string.trending, "trending")
        object Global : Explore(R.string.global, "global")
    }

    data class Search(val keyword: String) : StreamType()
    data class Posts(val ids: List<String>) : StreamType()

    val categoryName: String
        get() = when (this) {
            is Explore -> this::class.java.simpleName
            is Home -> this::class.java.simpleName
            is Mentions -> this::class.java.simpleName
            is Stars -> "${this::class.java.simpleName}/$userId"
            is User -> "${this::class.java.simpleName}/$userId"
            is Tag -> "${this::class.java.simpleName}/$tag"
            is Thread -> "${this::class.java.simpleName}/$postId"
            is Search -> "${this::class.java.simpleName}/$keyword"
            is Posts -> "${this::class.java.simpleName}/${ids.hashCode()}"
        }
}