package io.pnut.gamma.domain.repository

import io.pnut.gamma.domain.entity.Interaction
import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.domain.entity.Token
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.model.CachedList
import io.pnut.gamma.domain.model.PageableItemWrapper
import io.pnut.gamma.domain.model.StreamType
import io.pnut.gamma.domain.model.UserListType

interface IPnutCacheRepository {
    suspend fun getToken(): Token?
    suspend fun storeToken(token: Token)
    suspend fun getPosts(streamType: StreamType): CachedList<Post>
    suspend fun storePosts(
        posts: List<PageableItemWrapper<Post>>,
        streamType: StreamType,
        cacheSize: Int
    )
    suspend fun getInteractions(): CachedList<Interaction>
    suspend fun storeInteractions(
        interactions: List<PageableItemWrapper<Interaction>>,
        cacheSize: Int
    )
    suspend fun getUsers(userListType: UserListType): CachedList<User>
    suspend fun storeUsers(
        users: List<PageableItemWrapper<User>>,
        userListType: UserListType,
        cacheSize: Int
    )
    suspend fun clearAll()

}