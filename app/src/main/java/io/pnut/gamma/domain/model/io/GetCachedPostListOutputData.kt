package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.domain.model.CachedList

data class GetCachedPostListOutputData(
    val posts: CachedList<Post>
)
