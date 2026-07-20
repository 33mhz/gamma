package io.pnut.gamma.domain.model.io

data class VoteInputData(
    val pollId: String,
    val pollToken: String,
    val positions: Set<Int>
)
