package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.User

data class UpdateUserImageOutputData(
    val res: PnutResponse<User>
)
