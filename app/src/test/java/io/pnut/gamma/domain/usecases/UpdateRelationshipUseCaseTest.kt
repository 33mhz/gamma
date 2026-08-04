package io.pnut.gamma.domain.usecases

import kotlinx.coroutines.runBlocking
import io.pnut.gamma.domain.Relationship
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.model.io.UpdateRelationshipInputData
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.util.TestException
import io.pnut.gamma.sample.Users
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class UpdateRelationshipUseCaseTest {
    private fun getFollowUseCase(vararg users: User): UpdateRelationshipUseCase {
        val mockData = PnutRepositoryMock.PnutMockData(users = listOf(*users))
        val db = PnutRepositoryMock(mockData)
        return UpdateRelationshipUseCase(db)
    }

    @Test
    fun succeedToFollow() {
        val others = Users.others
        val followUseCase = getFollowUseCase(others)
        val input = UpdateRelationshipInputData(others.id, Relationship.Follow)
        val output = runBlocking { followUseCase.run(input) }
        assertThat(output.res.data.youFollow).isTrue()
    }

    @Test(expected = TestException::class)
    fun failToFollow() {
        val alreadyFollowingUser = Users.others.copy(youFollow = true)
        val followUseCase = getFollowUseCase(alreadyFollowingUser)
        val input = UpdateRelationshipInputData(alreadyFollowingUser.id, Relationship.Follow)
        runBlocking { followUseCase.run(input) }
    }

    @Test
    fun succeedToUnFollow() {
        val alreadyFollowingUser = Users.others.copy(youFollow = true)
        val followUseCase = getFollowUseCase(alreadyFollowingUser)
        val input = UpdateRelationshipInputData(alreadyFollowingUser.id, Relationship.UnFollow)
        val output = runBlocking { followUseCase.run(input) }
        assertThat(output.res.data.youFollow).isFalse()
    }

    @Test(expected = TestException::class)
    fun failToUnFollow() {
        val alreadyFollowingUser = Users.others
        val followUseCase = getFollowUseCase(alreadyFollowingUser)
        val input = UpdateRelationshipInputData(alreadyFollowingUser.id, Relationship.UnFollow)
        runBlocking { followUseCase.run(input) }
    }

    @Test
    fun succeedToBlock() {
        val others = Users.others
        val followUseCase = getFollowUseCase(others)
        val input = UpdateRelationshipInputData(others.id, Relationship.Block)
        val output = runBlocking { followUseCase.run(input) }
        assertThat(output.res.data.youBlocked).isTrue()
        assertThat(output.res.data.youFollow).isFalse()
    }

    @Test(expected = TestException::class)
    fun failToBlock() {
        val alreadyBlockedUser = Users.others.copy(youBlocked = true)
        val followUseCase = getFollowUseCase(alreadyBlockedUser)
        val input = UpdateRelationshipInputData(alreadyBlockedUser.id, Relationship.Block)
        runBlocking { followUseCase.run(input) }
    }

    @Test
    fun succeedToUnBlock() {
        val alreadyBlockedUser = Users.others.copy(youBlocked = true)
        val followUseCase = getFollowUseCase(alreadyBlockedUser)
        val input = UpdateRelationshipInputData(alreadyBlockedUser.id, Relationship.UnBlock)
        val output = runBlocking { followUseCase.run(input) }
        assertThat(output.res.data.youFollow).isFalse()
    }

    @Test(expected = TestException::class)
    fun failToUnBlock() {
        val others = Users.others
        val followUseCase = getFollowUseCase(others)
        val input = UpdateRelationshipInputData(others.id, Relationship.UnBlock)
        runBlocking { followUseCase.run(input) }
    }
}