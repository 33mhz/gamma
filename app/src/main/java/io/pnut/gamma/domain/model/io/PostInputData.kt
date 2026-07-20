package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.PostBody

data class PostInputData(
    val postBody: PostBody,
    val accountId: String
)
