package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.model.CachedList

data class GetCachedUserListOutputData(
    val users: CachedList<User>
)
