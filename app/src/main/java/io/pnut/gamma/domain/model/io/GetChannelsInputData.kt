package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.model.ChannelType
import io.pnut.gamma.domain.model.params.composed.GetChannelsParam

data class GetChannelsInputData(
    val channelType: ChannelType,
    val params: GetChannelsParam
)
