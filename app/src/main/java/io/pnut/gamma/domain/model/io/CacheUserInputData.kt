package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.model.PageableItemWrapper
import io.pnut.gamma.domain.model.UserListType

data class CacheUserInputData(
    val list: List<PageableItemWrapper<User>>,
    val userListType: UserListType
)
