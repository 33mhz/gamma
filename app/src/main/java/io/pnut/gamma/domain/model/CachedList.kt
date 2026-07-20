package io.pnut.gamma.domain.model

import com.squareup.moshi.JsonClass
import io.pnut.gamma.domain.entity.UniquePageable

@JsonClass(generateAdapter = true)
data class CachedList<T : UniquePageable>(
    val data: List<PageableItemWrapper<T>>
)