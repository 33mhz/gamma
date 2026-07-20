package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.model.UserListType
import io.pnut.gamma.domain.model.params.composed.GetUsersParam

data class GetUsersInputData(
    val userListType: UserListType,
    val getUsersParam: GetUsersParam
)
