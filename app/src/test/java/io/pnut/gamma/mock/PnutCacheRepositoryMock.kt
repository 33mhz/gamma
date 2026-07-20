package io.pnut.gamma.mock

import io.pnut.gamma.domain.entity.Interaction
import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.domain.entity.Token
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.model.CachedList
import io.pnut.gamma.domain.model.PageableItemWrapper
import io.pnut.gamma.domain.model.StreamType
import io.pnut.gamma.domain.model.UserListType
import io.pnut.gamma.domain.repository.IPnutCacheRepository

open class PnutCacheRepositoryMock : IPnutCacheRepository {
  override suspend fun getToken(): Token? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun storeToken(token: Token) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun getPosts(streamType: StreamType): CachedList<Post> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun storePosts(
    posts: List<PageableItemWrapper<Post>>,
    streamType: StreamType,
    cacheSize: Int
  ) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun getInteractions(): CachedList<Interaction> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun storeInteractions(
    interactions: List<PageableItemWrapper<Interaction>>,
    cacheSize: Int
  ) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun getUsers(userListType: UserListType): CachedList<User> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun storeUsers(
    users: List<PageableItemWrapper<User>>,
    userListType: UserListType,
    cacheSize: Int
  ) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}