package io.pnut.gamma.domain.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Explore(
    val description: String,
    val link: String,
    val slug: String,
    val title: String
)
