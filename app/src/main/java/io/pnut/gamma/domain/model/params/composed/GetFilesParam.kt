package io.pnut.gamma.domain.model.params.composed

import io.pnut.gamma.domain.model.params.single.PaginationParam

class GetFilesParam(
    val includeIncomplete: Boolean = false
) : BaseComposeParam() {
    fun add(pageParams: PaginationParam) = queryList.add(pageParams)
}
