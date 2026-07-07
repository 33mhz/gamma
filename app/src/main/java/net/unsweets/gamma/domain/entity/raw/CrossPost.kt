package net.unsweets.gamma.domain.entity.raw

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class CrossPost(override val value: CrossPostValue) : Raw<CrossPost.CrossPostValue>,
    PostRaw<CrossPost.CrossPostValue>, Parcelable {
    @IgnoredOnParcel
    override val type: String = "io.pnut.core.crosspost"

    @Parcelize
    data class CrossPostValue(@Json(name = "canonical_url") val canonicalUrl: String) : Raw.RawValue, Parcelable
}