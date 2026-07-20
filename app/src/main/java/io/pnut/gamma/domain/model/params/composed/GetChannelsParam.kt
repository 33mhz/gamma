package io.pnut.gamma.domain.model.params.composed

import io.pnut.gamma.domain.model.params.single.GeneralChannelParam
import io.pnut.gamma.domain.model.params.single.PaginationParam

open class GetChannelsParam(existParams: Map<String, String>? = null) :
    BaseComposeParam(existParams) {
    fun add(pagination: PaginationParam) = queryList.add(pagination)
    fun add(generalChannelParam: GeneralChannelParam) = queryList.add(generalChannelParam)

}