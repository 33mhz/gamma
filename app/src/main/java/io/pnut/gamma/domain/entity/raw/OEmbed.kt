package io.pnut.gamma.domain.entity.raw

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
open class OEmbed(
    open val type: String,
    open val version: String
) : RawValue, Parcelable {

    companion object {
        const val TYPE = "io.pnut.core.oembed"
        fun getOEmbeds(raw: Map<String, List<RawValue>>?): List<OEmbed> {
            return raw?.get(TYPE)?.filterIsInstance<OEmbed>() ?: emptyList()
        }
    }

    @Parcelize
    data class Photo(
        val width: Int,
        val height: Int,
        val url: String,
        override val version: String,
        @Json(name = "embeddable_url") val embeddableUrl: String?,
        val title: String?,
        @Json(name = "author_name") val authorName: String?,
        @Json(name = "author_url") val authorUrl: String?,
        @Json(name = "provider_name") val providerName: String?,
        @Json(name = "provider_url") val providerUrl: String?,
        @Json(name = "cache_age") val cacheAge: Int?,
        @Json(name = "thumbnail_url") val thumbnailUrl: String?,
        @Json(name = "thumbnail_height") val thumbnailHeight: String?,
        @Json(name = "thumbnail_width") val thumbnailWidth: String?
    ) : OEmbed(TYPE, version), Parcelable {
        companion object {
            const val TYPE = "photo"
            fun getPhotos(raw: Map<String, List<RawValue>>?): List<Photo> {
                return getOEmbeds(raw).filterIsInstance<Photo>()
            }
        }
    }

    @Parcelize
    data class Video(
        override val version: String
    ) : OEmbed(TYPE, version), Parcelable {
        companion object {
            const val TYPE = "video"
            fun getVideos(raw: Map<String, List<RawValue>>?): List<Video> {
                return getOEmbeds(raw).filterIsInstance<Video>()
            }
        }
    }

    interface OptionalField {
        @Json(name = "embeddable_url")
        val embeddableUrl: String?
        val title: String?
        @Json(name = "author_name")
        val authorName: String?
        @Json(name = "author_url")
        val authorUrl: String?
        @Json(name = "provider_name")
        val providerName: String?
        @Json(name = "provider_url")
        val providerUrl: String?
        @Json(name = "cache_age")
        val cacheAge: Int?
        @Json(name = "poster_url")
        val posterUrl: String?
        @Json(name = "thumbnail_url")
        val thumbnailUrl: String?
        @Json(name = "thumbnail_height")
        val thumbnailHeight: String?
        @Json(name = "thumbnail_width") val thumbnailWidth: String?
        val bitrate: Int?
        val release: Int?
        val license: String?
        val genre: String?
        @Json(name = "track_type")
        val trackType: TrackType?

        enum class TrackType(val type: String) {
            ORIGINAL("original"),
            REMIX("remix"),
            LIVE("live"),
            SPOKEN("spoken"),
            PODCAST("podcast"),
            DEMO("demo"),
            LOOP("loop"),
            SOUND_EFFECT("sound_effect"),
            SAMPLE("sample"),
            OTHER("other")
        }
    }
}
