package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.PostOutputData
import io.pnut.gamma.domain.model.io.UpdatePostInputData
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.repository.IPnutRepository
import io.pnut.gamma.util.ErrorCollections
import javax.inject.Inject

class UpdatePostUseCase @Inject constructor(
    private val pnutRepository: IPnutRepository,
    private val accountRepository: IAccountRepository
) : UseCase<PostOutputData, UpdatePostInputData>() {
    override suspend fun run(params: UpdatePostInputData): PostOutputData {
        val token = accountRepository.getToken(params.accountId) ?: throw ErrorCollections.AccountNotFound()
        val res = pnutRepository.updatePostSync(params.postId, params.postBody, token)
        return PostOutputData(res)
    }
}
