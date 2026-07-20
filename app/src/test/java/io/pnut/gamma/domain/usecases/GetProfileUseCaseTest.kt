package io.pnut.gamma.domain.usecases

import kotlinx.coroutines.runBlocking
import io.pnut.gamma.domain.model.io.GetProfileInputData
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.sample.Users
import io.pnut.gamma.util.ErrorCollections
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Test

class GetProfileUseCaseTest {
    private val me = Users.me
    private val mockData = PnutRepositoryMock.PnutMockData(users = listOf(me))
    private val db = PnutRepositoryMock(mockData)
    private val getProfileUseCase = GetProfileUseCase(db)

    @Test
    fun succeed() {
        val input = GetProfileInputData(me.id)
        val output = runBlocking { getProfileUseCase.run(input) }
        Assert.assertThat(output.res.data, `is`(me))
    }

    @Test(expected = ErrorCollections.CommunicationError::class)
    fun fail() {
        val input = GetProfileInputData("")
        runBlocking { getProfileUseCase.run(input) }
    }
}