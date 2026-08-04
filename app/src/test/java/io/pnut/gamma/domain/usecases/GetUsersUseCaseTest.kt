package io.pnut.gamma.domain.usecases

import kotlinx.coroutines.runBlocking
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.model.UserListType
import io.pnut.gamma.domain.model.io.GetUsersInputData
import io.pnut.gamma.domain.model.params.composed.GetUsersParam
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.sample.Users
import org.junit.Test
import com.google.common.truth.Truth.assertThat


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
    assertThat(res.res.meta.code).isEqualTo(200)
    assertThat(res.res.data).hasSize(2)
    assertThat(res.res.data[0].id).isEqualTo(me.id)
    assertThat(res.res.data[1].id).isEqualTo(others.id)
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
    assertThat(res.res.meta.code).isEqualTo(200)
    assertThat(res.res.data).hasSize(2)
    assertThat(res.res.data[0].id).isEqualTo(me.id)
    assertThat(res.res.data[1].id).isEqualTo(others.id)
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
    assertThat(res.res.meta.code).isEqualTo(200)
    assertThat(res.res.data).hasSize(2)
    assertThat(res.res.data[0].id).isEqualTo(me.id)
    assertThat(res.res.data[1].id).isEqualTo(others.id)
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
    assertThat(res.res.meta.code).isEqualTo(200)
    assertThat(res.res.data).hasSize(2)
    assertThat(res.res.data[0].id).isEqualTo(me.id)
    assertThat(res.res.data[1].id).isEqualTo(others.id)
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
    assertThat(res.res.meta.code).isEqualTo(200)
    assertThat(res.res.data).hasSize(2)
    assertThat(res.res.data[0].id).isEqualTo(me.id)
    assertThat(res.res.data[1].id).isEqualTo(others.id)
  }
}