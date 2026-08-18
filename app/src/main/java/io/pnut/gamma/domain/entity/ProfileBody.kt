package io.pnut.gamma.domain.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProfileBody(
    val name: String?,
    val content: Content?,
    val timezone: String?,
    val locale: String?
) {
    @JsonClass(generateAdapter = true)
    data class Content(val text: String)
}
