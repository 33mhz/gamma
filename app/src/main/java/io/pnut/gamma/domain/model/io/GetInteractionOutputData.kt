package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.Interaction
import io.pnut.gamma.domain.entity.PnutResponse

data class GetInteractionOutputData(
    val res: PnutResponse<List<Interaction>>
)
