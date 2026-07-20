package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.CachedList
import io.pnut.gamma.domain.model.StreamType
import io.pnut.gamma.domain.model.io.GetCachedPostListInputData
import io.pnut.gamma.domain.model.io.GetCachedPostListOutputData
import io.pnut.gamma.domain.repository.IPnutCacheRepository

open class GetCachedPostListUseCase(private val pnutCacheRepository: IPnutCacheRepository) :
    AsyncUseCase<GetCachedPostListOutputData, GetCachedPostListInputData>() {
    override suspend fun run(params: GetCachedPostListInputData): GetCachedPostListOutputData {
        if (params.streamType == StreamType.Explore.MissedConversations) return GetCachedPostListOutputData(
            CachedList(emptyList())
        )
        val posts = pnutCacheRepository.getPosts(params.streamType)
        return GetCachedPostListOutputData(posts)
    }
}