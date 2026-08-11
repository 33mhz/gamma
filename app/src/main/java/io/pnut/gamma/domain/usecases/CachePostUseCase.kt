package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.StreamType
import io.pnut.gamma.domain.model.io.CachePostInputData
import io.pnut.gamma.domain.repository.IPnutCacheRepository
import io.pnut.gamma.domain.repository.IPreferenceRepository
import io.pnut.gamma.util.LogUtil

open class CachePostUseCase(
    private val pnutCacheRepository: IPnutCacheRepository,
    private val preferenceRepository: IPreferenceRepository
) :
    AsyncUseCase<Unit, CachePostInputData>() {
    override suspend fun run(params: CachePostInputData) {
        LogUtil.d("CachePostUseCase")
        if (params.streamType == StreamType.Explore.MissedConversations) return
        pnutCacheRepository.storePosts(
            params.list,
            params.streamType,
            preferenceRepository.cacheSize
        )
    }
}
