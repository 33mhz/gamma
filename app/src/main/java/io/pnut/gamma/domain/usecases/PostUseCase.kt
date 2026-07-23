package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.PostInputData
import io.pnut.gamma.domain.model.io.PostOutputData
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.repository.IPnutRepository
import io.pnut.gamma.util.ErrorCollections

class PostUseCase(
    private val pnutRepository: IPnutRepository,
    private val accountRepository: IAccountRepository
) :
    UseCase<PostOutputData, PostInputData>() {
    override fun run(params: PostInputData): PostOutputData {
        val token = accountRepository.getToken(params.accountId) ?: throw ErrorCollections.AccountNotFound()
        val res = pnutRepository.createPostSync(params.postBody, token)
        return PostOutputData(res)
    }

}
