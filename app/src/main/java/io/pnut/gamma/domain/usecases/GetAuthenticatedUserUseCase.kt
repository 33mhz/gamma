package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.GetAuthenticatedUserInputData
import io.pnut.gamma.domain.repository.IPnutCacheRepository
import io.pnut.gamma.domain.repository.IPnutRepository

open class GetAuthenticatedUserUseCase(
    private val pnutRepository: IPnutRepository,
    private val pnutCacheRepository: IPnutCacheRepository
) : AsyncUseCase<Unit, GetAuthenticatedUserInputData>() {

    override suspend fun run(params: GetAuthenticatedUserInputData) {
        pnutCacheRepository.getToken()?.let {
            params.liveData.postValue(it)
        }
        val tokenRes = pnutRepository.getToken()
        pnutCacheRepository.storeToken(tokenRes.data)
        params.liveData.postValue(tokenRes.data)
    }

}
