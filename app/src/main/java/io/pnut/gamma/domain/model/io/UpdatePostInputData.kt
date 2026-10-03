package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.PostBody

data class UpdatePostInputData(
    val postId: String,
    val postBody: PostBody,
    val accountId: String
)
