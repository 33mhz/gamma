package net.unsweets.gamma.domain.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PnutResponse<T>(val meta: Meta, val data: T) {
    @JsonClass(generateAdapter = true)
    data class Meta(
        val code: Int,
        @Json(name = "min_id") val minId: String? = null,
        @Json(name = "max_id") val maxId: String? = null,
        val more: Boolean? = null,
        @Json(name = "deleted_ids") val deletedIds: List<String>? = null,
        @Json(name = "revised_ids") val revisedIds: List<String>? = null
    )
}
