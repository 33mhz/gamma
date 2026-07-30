package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.ReportPostInputData
import io.pnut.gamma.domain.model.io.ReportPostOutputData
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.repository.IPnutRepository
import io.pnut.gamma.util.ErrorCollections

class ReportPostUseCase(
    private val pnutRepository: IPnutRepository,
    private val accountRepository: IAccountRepository
) : UseCase<ReportPostOutputData, ReportPostInputData>() {
    override fun run(params: ReportPostInputData): ReportPostOutputData {
        val token = accountRepository.getToken(params.accountId) ?: throw ErrorCollections.AccountNotFound()
        pnutRepository.updateDefaultPnutService(token)
        val res = pnutRepository.reportPost(params.postId, params.reason)
        return ReportPostOutputData(res)
    }
}
