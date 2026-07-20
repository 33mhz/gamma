package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.UserListType
import io.pnut.gamma.domain.model.io.GetUsersInputData
import io.pnut.gamma.domain.model.io.GetUsersOutputData
import io.pnut.gamma.domain.model.params.composed.GetUsersParam
import io.pnut.gamma.domain.model.params.single.SearchUserParam
import io.pnut.gamma.domain.repository.IPnutRepository

class GetUsersUseCase(private val pnutRepository: IPnutRepository) :
    AsyncUseCase<GetUsersOutputData, GetUsersInputData>() {
    override suspend fun run(params: GetUsersInputData): GetUsersOutputData {
        val res = when (val userListType = params.userListType) {
            is UserListType.Followers -> pnutRepository.getFollowers(userListType.userId, params.getUsersParam)
            is UserListType.Following -> pnutRepository.getFollowing(userListType.userId, params.getUsersParam)
            is UserListType.Search -> pnutRepository.searchUsers(
                GetUsersParam(params.getUsersParam.toMap()).apply {
                    add(SearchUserParam(userListType.keyword))
                })
            is UserListType.Blocked -> pnutRepository.getBlockedUsers(params.getUsersParam)
            is UserListType.Muted -> pnutRepository.getMutedUsers(params.getUsersParam)
        }
        return GetUsersOutputData(res)
    }

}
