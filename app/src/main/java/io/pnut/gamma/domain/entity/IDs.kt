package io.pnut.gamma.domain.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IDs(val ids: List<String>) {
    override fun toString(): String = ids.joinToString(",")
}
