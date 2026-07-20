package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.domain.model.PageableItemWrapper
import io.pnut.gamma.domain.model.StreamType

data class CachePostInputData(
    val list: List<PageableItemWrapper<Post>>,
    val streamType: StreamType
)
