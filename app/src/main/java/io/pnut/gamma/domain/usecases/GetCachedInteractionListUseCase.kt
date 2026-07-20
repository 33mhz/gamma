package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.GetCachedInteractionListOutputData
import io.pnut.gamma.domain.repository.IPnutCacheRepository

class GetCachedInteractionListUseCase(private val pnutCacheRepository: IPnutCacheRepository) :
    AsyncUseCase<GetCachedInteractionListOutputData, Unit>() {

    override suspend fun run(params: Unit): GetCachedInteractionListOutputData {
        val interactions = pnutCacheRepository.getInteractions()
        return GetCachedInteractionListOutputData(interactions)
    }
}