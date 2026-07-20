package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.Channel
import io.pnut.gamma.domain.entity.PnutResponse

data class GetChannelsOutputData(
    val channels: PnutResponse<List<Channel>>
)
