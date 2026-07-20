package io.pnut.gamma.domain.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VoteBody(
    val positions: List<Int>
)
