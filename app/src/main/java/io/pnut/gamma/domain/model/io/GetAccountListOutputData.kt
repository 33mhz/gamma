package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.model.Account


data class GetAccountListOutputData(
    val accounts: List<Account>
)
