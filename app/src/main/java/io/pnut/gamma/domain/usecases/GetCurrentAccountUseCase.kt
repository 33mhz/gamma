package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.GetCurrentAccountOutputData
import io.pnut.gamma.domain.repository.IAccountRepository

open class GetCurrentAccountUseCase(private val accountRepository: IAccountRepository) :
    SyncUseCase<GetCurrentAccountOutputData, Unit>() {
    override fun run(params: Unit): GetCurrentAccountOutputData {
        val account = accountRepository.getDefaultAccount()
        return GetCurrentAccountOutputData(account)
    }
}