package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.Post

data class GetPostOutputData(
    val res: PnutResponse<List<Post>>
)
