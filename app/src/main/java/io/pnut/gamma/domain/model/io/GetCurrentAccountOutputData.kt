package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.model.Account

data class GetCurrentAccountOutputData(
    val account: Account?
)