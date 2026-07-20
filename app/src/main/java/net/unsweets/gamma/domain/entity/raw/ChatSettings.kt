package net.unsweets.gamma.domain.entity.raw

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatSettings(
    val name: String,
    val description: String,
    val categories: List<Categories>?
) : RawValue, Parcelable {

    enum class Categories(val value: String) {
        FUN("fun"),
        LIFESTYLE("lifestyle"),
        PROFESSION("profession"),
        LANGUAGE("language"),
        COMMUNITY("community"),
        TECH("tech"),
        EVENT("event"),
        GENERAL("general")
    }

    companion object {
        const val TYPE = "io.pnut.core.chat-settings"
        fun getChatSettings(raw: Map<String, List<RawValue>>?): ChatSettings? {
            return raw?.get(TYPE)?.firstOrNull() as? ChatSettings
        }
    }
}
