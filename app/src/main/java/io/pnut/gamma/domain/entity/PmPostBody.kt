package io.pnut.gamma.domain.entity

import android.os.Parcelable
import com.squareup.moshi.Json
import io.pnut.gamma.domain.entity.raw.RawValue
import kotlinx.parcelize.Parcelize

@Parcelize
data class PmPostBody(
    val text: String,
    val destinations: List<String>,
    @Json(name = "is_nsfw") val isNsfw: Boolean? = null,
    val raw: Map<String, List<RawValue>>? = null
) : Parcelable
