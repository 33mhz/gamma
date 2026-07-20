package io.pnut.gamma.domain.model.params.composed

import io.pnut.gamma.domain.model.params.single.GeneralPostParam
import io.pnut.gamma.domain.model.params.single.PaginationParam
import io.pnut.gamma.domain.model.params.single.SearchPostParam

open class GetPostsParam(existParams: Map<String, String>? = null) : BaseComposeParam(existParams) {
    fun add(pagination: PaginationParam) = queryList.add(pagination)
    fun add(generalPostParamParam: GeneralPostParam) = queryList.add(generalPostParamParam)
    fun add(searchPostParam: SearchPostParam) = queryList.add(searchPostParam)

}