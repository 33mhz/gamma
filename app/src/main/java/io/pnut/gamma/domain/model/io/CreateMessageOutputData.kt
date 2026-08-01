package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.Message
import io.pnut.gamma.domain.entity.PnutResponse

data class CreateMessageOutputData(
    val res: PnutResponse<Message>
)
