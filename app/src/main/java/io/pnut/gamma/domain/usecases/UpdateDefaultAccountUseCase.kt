package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.UpdateDefaultAccountInputData
import io.pnut.gamma.domain.model.io.UpdateDefaultAccountOutputData
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.repository.IPnutRepository

open class UpdateDefaultAccountUseCase(
    private val accountRepository: IAccountRepository,
    private val pnutRepository: IPnutRepository
) :
    SyncUseCase<UpdateDefaultAccountOutputData, UpdateDefaultAccountInputData>() {
    override fun run(params: UpdateDefaultAccountInputData): UpdateDefaultAccountOutputData {
        accountRepository.updateDefaultAccount(params.id)
        val token = accountRepository.getToken(params.id) ?: return UpdateDefaultAccountOutputData(
            false
        )
        pnutRepository.updateDefaultPnutService(token)
        return UpdateDefaultAccountOutputData(true)
    }
}