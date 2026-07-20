package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.CacheInteractionInputData
import io.pnut.gamma.domain.repository.IPnutCacheRepository
import io.pnut.gamma.domain.repository.IPreferenceRepository

class CacheInteractionUseCase(
    private val pnutCacheRepository: IPnutCacheRepository,
    private val preferenceRepository: IPreferenceRepository
) :
    AsyncUseCase<Unit, CacheInteractionInputData>() {
    override suspend fun run(params: CacheInteractionInputData) {
        pnutCacheRepository.storeInteractions(params.list, preferenceRepository.cacheSize)
    }
}
