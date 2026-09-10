package io.pnut.gamma.presentation.util.embed

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import io.pnut.gamma.R
import io.pnut.gamma.presentation.util.BindingUtil
import io.pnut.gamma.presentation.util.Util

object EmbedManager {
    private val providers = listOf(
        YouTubeEmbedProvider()
    )

    fun parse(urls: List<String>): List<Embed> {
        return urls.mapNotNull { url ->
            providers.firstNotNullOfOrNull { it.parse(url) }
        }
    }

    fun bindEmbeds(container: LinearLayout, embeds: List<Embed>, isNsfw: Boolean) {
        container.removeAllViews()
        if (embeds.isEmpty()) {
            container.visibility = View.GONE
            return
        }

        container.visibility = View.VISIBLE
        val inflater = LayoutInflater.from(container.context)
        embeds.forEach { embed ->
            val view = inflater.inflate(R.layout.embed_item, container, false)
            val thumbnailImageView = view.findViewById<ImageView>(R.id.thumbnailImageView)
            val playIcon = view.findViewById<ImageView>(R.id.playIcon)
            val nsfwMask = view.findViewById<View>(R.id.nsfwMask)

            if (isNsfw) {
                nsfwMask.visibility = View.VISIBLE
                playIcon.visibility = View.GONE
            } else {
                nsfwMask.visibility = View.GONE
                BindingUtil.glideSrc(thumbnailImageView, embed.thumbnailUrl)
                when (embed) {
                    is Embed.Video -> {
                        playIcon.visibility = View.VISIBLE
                        view.setOnClickListener {
                            Util.openCustomTabUrl(container.context, embed.url)
                        }
                    }
                }
            }
            container.addView(view)
        }
    }
}

