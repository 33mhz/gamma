package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.model.StreamType
import io.pnut.gamma.domain.model.params.composed.GetPostsParam

class GetPostInputData(
    val streamType: StreamType,
    val params: GetPostsParam
)
