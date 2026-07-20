package io.pnut.gamma.domain.model.params.composed

import io.pnut.gamma.domain.model.params.single.InteractionParam
import io.pnut.gamma.domain.model.params.single.PaginationParam

class GetInteractionsParam : BaseComposeParam() {
    init {
        add(InteractionParam())
    }

    fun add(pagination: PaginationParam) = queryList.add(pagination)
    fun add(interactionParam: InteractionParam) = queryList.add(interactionParam)
}