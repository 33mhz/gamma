package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.PmPostBody

data class CreatePmMessageInputData(
    val messageBody: PmPostBody
)
