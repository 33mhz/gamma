package io.pnut.gamma.domain.entity.raw

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class CrossPost(
    @Json(name = "canonical_url") val canonicalUrl: String
) : RawValue, Parcelable {

    companion object {
        const val TYPE = "io.pnut.core.crosspost"
        fun getCrossPost(raw: Map<String, List<RawValue>>?): CrossPost? {
            return raw?.get(TYPE)?.firstOrNull() as? CrossPost
        }
    }
}
