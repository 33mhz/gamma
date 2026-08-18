package io.pnut.gamma.domain.entity

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.pnut.gamma.domain.entity.raw.RawValue
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class PmPostBody(
    val text: String,
    val destinations: List<String>,
    @Json(name = "is_nsfw") val isNsfw: Boolean? = null,
    val raw: Map<String, List<RawValue>>? = null
) : Parcelable
