package io.pnut.gamma.domain.model.params.single

interface BaseParam {
    fun toMap(): Map<String, String>
}