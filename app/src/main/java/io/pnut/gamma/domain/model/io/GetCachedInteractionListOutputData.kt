package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.Interaction
import io.pnut.gamma.domain.model.CachedList

data class GetCachedInteractionListOutputData(
    val interactions: CachedList<Interaction>
)
