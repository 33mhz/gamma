package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.Message
import io.pnut.gamma.domain.entity.PnutResponse

data class DeleteMessageOutputData(
    val res: PnutResponse<Message>
)
