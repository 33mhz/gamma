package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.PostBody

data class CreateMessageInputData(
    val channelId: String,
    val messageBody: PostBody
)
