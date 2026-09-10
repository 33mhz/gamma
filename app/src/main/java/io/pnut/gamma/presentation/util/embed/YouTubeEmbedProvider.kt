package io.pnut.gamma.presentation.util.embed

import java.net.URL

class YouTubeEmbedProvider : EmbedProvider {
    override fun parse(url: String): Embed? {
        val videoId = getYoutubeId(url) ?: return null
        return Embed.Video(
            url = url,
            thumbnailUrl = "https://img.youtube.com/vi/$videoId/0.jpg",
            videoId = videoId,
            type = Embed.VideoType.YouTube
        )
    }

    private fun getYoutubeId(url: String): String? {
        return try {
            val normalizedUrl = if (!url.startsWith("http")) "https://$url" else url
            val parsedUrl = URL(normalizedUrl)
            val host = parsedUrl.host ?: return null

            if (host == "youtu.be" || host == "www.youtu.be") {
                val id = parsedUrl.path.removePrefix("/").split("/").firstOrNull()
                if (id.isNullOrEmpty()) null else id
            } else if (host == "youtube.com" || host.endsWith(".youtube.com")) {
                val path = parsedUrl.path
                val id = when {
                    path.startsWith("/embed/") -> path.split("/").getOrNull(2)
                    path.startsWith("/shorts/") -> path.split("/").getOrNull(2)
                    path.startsWith("/watch") -> {
                        val query = parsedUrl.query ?: return null
                        query.split("&").find { it.startsWith("v=") }?.removePrefix("v=")
                    }
                    else -> null
                }
                if (id.isNullOrEmpty()) null else id
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
