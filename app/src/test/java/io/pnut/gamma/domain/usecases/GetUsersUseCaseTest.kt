package io.pnut.gamma.domain.usecases

import kotlinx.coroutines.runBlocking
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.model.UserListType
import io.pnut.gamma.domain.model.io.GetUsersInputData
import io.pnut.gamma.domain.model.params.composed.GetUsersParam
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.sample.Users
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Test


class GetUsersUseCaseTest {

  @Test
  fun getFollowers() {
    val me = Users.me
    val others = Users.others
    val useCase = GetUsersUseCase(object : PnutRepositoryMock() {
      override suspend fun getFollowers(
        userId: String,
        getUsersParam: GetUsersParam
      ): PnutResponse<List<User>> {
        return PnutResponse(
          PnutResponse.Meta(200),
          listOf(me, others)
        )
      }
    })
    val res = runBlocking {
      useCase.run(GetUsersInputData(UserListType.Followers("me"), GetUsersParam()))
    }
    Assert.assertThat(res.res.meta.code, `is`(200))
    Assert.assertThat(res.res.data.size, `is`(2))
    Assert.assertThat(res.res.data[0].id, `is`(me.id))
    Assert.assertThat(res.res.data[1].id, `is`(others.id))
  }

  @Test
  fun getFollowing() {
    val me = Users.me
    val others = Users.others
    val useCase = GetUsersUseCase(object : PnutRepositoryMock() {
      override suspend fun getFollowing(
        userId: String,
        getUsersParam: GetUsersParam
      ): PnutResponse<List<User>> {
        return PnutResponse(
          PnutResponse.Meta(200),
          listOf(me, others)
        )
      }
    })
    val res = runBlocking {
      useCase.run(GetUsersInputData(UserListType.Following("me"), GetUsersParam()))
    }
    Assert.assertThat(res.res.meta.code, `is`(200))
    Assert.assertThat(res.res.data.size, `is`(2))
    Assert.assertThat(res.res.data[0].id, `is`(me.id))
    Assert.assertThat(res.res.data[1].id, `is`(others.id))
  }

  @Test
  fun searchUsers() {
    val me = Users.me
    val others = Users.others
    val useCase = GetUsersUseCase(object : PnutRepositoryMock() {
      override suspend fun searchUsers(getSearchUsersParam: GetUsersParam): PnutResponse<List<User>> {
        return PnutResponse(
          PnutResponse.Meta(200),
          listOf(me, others)
        )
      }
    })
    val res = runBlocking {
      useCase.run(GetUsersInputData(UserListType.Search("foo"), GetUsersParam()))
    }
    Assert.assertThat(res.res.meta.code, `is`(200))
    Assert.assertThat(res.res.data.size, `is`(2))
    Assert.assertThat(res.res.data[0].id, `is`(me.id))
    Assert.assertThat(res.res.data[1].id, `is`(others.id))
  }

  @Test
  fun getBlockedUsers() {
    val me = Users.me
    val others = Users.others
    val useCase = GetUsersUseCase(object : PnutRepositoryMock() {
      override suspend fun getBlockedUsers(getUsersParam: GetUsersParam): PnutResponse<List<User>> {
        return PnutResponse(
          PnutResponse.Meta(200),
          listOf(me, others)
        )
      }
    })
    val res = runBlocking {
      useCase.run(GetUsersInputData(UserListType.Blocked, GetUsersParam()))
    }
    Assert.assertThat(res.res.meta.code, `is`(200))
    Assert.assertThat(res.res.data.size, `is`(2))
    Assert.assertThat(res.res.data[0].id, `is`(me.id))
    Assert.assertThat(res.res.data[1].id, `is`(others.id))
  }

  @Test
  fun getMutedUsers() {
    val me = Users.me
    val others = Users.others
    val useCase = GetUsersUseCase(object : PnutRepositoryMock() {
      override suspend fun getMutedUsers(getUsersParam: GetUsersParam): PnutResponse<List<User>> {
        return PnutResponse(
          PnutResponse.Meta(200),
          listOf(me, others)
        )
      }
    })
    val res = runBlocking {
      useCase.run(GetUsersInputData(UserListType.Muted, GetUsersParam()))
    }
    Assert.assertThat(res.res.meta.code, `is`(200))
    Assert.assertThat(res.res.data.size, `is`(2))
    Assert.assertThat(res.res.data[0].id, `is`(me.id))
    Assert.assertThat(res.res.data[1].id, `is`(others.id))
  }
}