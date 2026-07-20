package io.pnut.gamma.domain.model.params.composed

import io.pnut.gamma.domain.model.params.single.BaseParam
import io.pnut.gamma.util.LogUtil

abstract class BaseComposeParam(private val map: Map<String, String>? = emptyMap()) : BaseParam {
    protected val queryList: MutableList<BaseParam> = mutableListOf()
    override fun toMap(): Map<String, String> {
        val nonNullMap = map ?: emptyMap()
        return queryList.fold(nonNullMap.toMutableMap(), { res, current ->
            val currentMap = current.toMap()
            currentMap.entries.forEach { entry ->
                res[entry.key] = entry.value
            }
            LogUtil.e(res.toString())
            res
        })
    }
}