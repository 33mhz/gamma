package io.pnut.gamma.domain.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StarBody(
    val note: String? = null
)
