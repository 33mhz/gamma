package net.unsweets.gamma.domain.entity.raw

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Language(override val value: LanguageValue) : Raw<Language.LanguageValue>, PostRaw<Language.LanguageValue>,
    Parcelable {
    @Parcelize
    data class LanguageValue(val language: String) : Raw.RawValue, Parcelable

    @IgnoredOnParcel
    override val type = "io.pnut.core.language"
}