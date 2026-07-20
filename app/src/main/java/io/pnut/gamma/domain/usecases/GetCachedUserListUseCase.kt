package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.GetCachedUserListInputData
import io.pnut.gamma.domain.model.io.GetCachedUserListOutputData
import io.pnut.gamma.domain.repository.IPnutCacheRepository

class GetCachedUserListUseCase(private val pnutCacheRepository: IPnutCacheRepository) :
    AsyncUseCase<GetCachedUserListOutputData, GetCachedUserListInputData>() {
    override suspend fun run(params: GetCachedUserListInputData): GetCachedUserListOutputData {
        val users = pnutCacheRepository.getUsers(params.userListType)
        return GetCachedUserListOutputData(users)
    }
}