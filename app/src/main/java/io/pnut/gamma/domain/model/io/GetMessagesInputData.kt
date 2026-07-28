package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.model.params.single.PaginationParam

data class GetMessagesInputData(
    val channelId: String,
    val paginationParam: PaginationParam
)
