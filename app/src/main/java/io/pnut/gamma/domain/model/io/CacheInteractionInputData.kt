package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.Interaction
import io.pnut.gamma.domain.model.PageableItemWrapper

data class CacheInteractionInputData(
    val list: List<PageableItemWrapper<Interaction>>
)
