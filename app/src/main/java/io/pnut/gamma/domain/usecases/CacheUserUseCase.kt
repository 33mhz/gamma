package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.CacheUserInputData
import io.pnut.gamma.domain.repository.IPnutCacheRepository
import io.pnut.gamma.domain.repository.IPreferenceRepository

class CacheUserUseCase(
    private val pnutCacheRepository: IPnutCacheRepository,
    private val preferenceRepository: IPreferenceRepository
) :
    AsyncUseCase<Unit, CacheUserInputData>() {
    override suspend fun run(params: CacheUserInputData) {
        pnutCacheRepository.storeUsers(
            params.list,
            params.userListType,
            preferenceRepository.cacheSize
        )
    }
}
