package io.pnut.gamma.domain.model.io

data class DeleteMessageInputData(
    val channelId: String,
    val messageId: String
)
