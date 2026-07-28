package io.pnut.gamma.domain.entity

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@JsonClass(generateAdapter = true)
data class Marker(
    val id: String,
    val name: String,
    @Json(name = "last_read_id") val lastReadId: String? = null,
    val percentage: Int? = null,
    @Json(name = "updated_at") val updatedAt: Date? = null,
    val version: String? = null
) : Parcelable
