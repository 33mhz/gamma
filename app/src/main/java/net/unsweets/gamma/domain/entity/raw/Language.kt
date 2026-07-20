package net.unsweets.gamma.domain.entity.raw

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Language(
    val language: String
) : RawValue, Parcelable {

    companion object {
        const val TYPE = "io.pnut.core.language"
        fun getLanguage(raw: Map<String, List<RawValue>>?): Language? {
            return raw?.get(TYPE)?.firstOrNull() as? Language
        }
    }
}
