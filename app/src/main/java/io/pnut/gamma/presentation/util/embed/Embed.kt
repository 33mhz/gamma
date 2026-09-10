package io.pnut.gamma.presentation.util.embed

sealed class Embed {
    abstract val url: String
    abstract val thumbnailUrl: String?

    data class Video(
        override val url: String,
        override val thumbnailUrl: String?,
        val videoId: String,
        val type: VideoType
    ) : Embed()

    enum class VideoType {
        YouTube
    }
}
