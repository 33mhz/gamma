package io.pnut.gamma.domain.entity.raw

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.pnut.gamma.domain.entity.entities.BaseContent
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class ChatSettings(
    val name: String?,
    val description: BaseContent?,
    val categories: List<Categories>?
) : RawValue, Parcelable {

    enum class Categories(val value: String) {
        @Json(name = "fun") FUN("fun"),
        @Json(name = "lifestyle") LIFESTYLE("lifestyle"),
        @Json(name = "profession") PROFESSION("profession"),
        @Json(name = "language") LANGUAGE("language"),
        @Json(name = "community") COMMUNITY("community"),
        @Json(name = "tech") TECH("tech"),
        @Json(name = "event") EVENT("event"),
        @Json(name = "general") GENERAL("general")
    }

    companion object {
        const val TYPE = "io.pnut.core.chat-settings"
        fun getChatSettings(raw: Map<String, List<RawValue>>?): ChatSettings? {
            return raw?.get(TYPE)?.firstOrNull() as? ChatSettings
        }
    }
}
