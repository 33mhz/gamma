package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.User

data class GetUsersOutputData(
    val res: PnutResponse<List<User>>
)
