package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.PnutResponse

data class ReportPostOutputData(
    val res: PnutResponse<Unit>
)
