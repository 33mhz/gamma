package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.ReportReason

data class ReportPostInputData(
    val postId: String,
    val reason: ReportReason,
    val accountId: String
)
