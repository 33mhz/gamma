package io.pnut.gamma.domain.usecases

import android.util.Log
import io.pnut.gamma.domain.model.io.SetupTokenOutputData
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.repository.IPnutRepository

open class SetupTokenUseCase(
    private val pnutRepository: IPnutRepository,
    private val accountRepository: IAccountRepository
) :
    AsyncUseCase<SetupTokenOutputData, Unit>() {
    override suspend fun run(params: Unit): SetupTokenOutputData {
      Log.e("SetupTokenUseCase", "run")

        val account = accountRepository.getDefaultAccount() ?: return SetupTokenOutputData(
            false
        )
        pnutRepository.updateDefaultPnutService(account.token)
        return SetupTokenOutputData(true)
    }
}