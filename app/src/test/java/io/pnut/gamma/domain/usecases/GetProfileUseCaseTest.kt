package io.pnut.gamma.domain.usecases

import kotlinx.coroutines.runBlocking
import io.pnut.gamma.domain.model.io.GetProfileInputData
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.sample.Users
import io.pnut.gamma.util.ErrorCollections
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class GetProfileUseCaseTest {
    private val me = Users.me
    private val mockData = PnutRepositoryMock.PnutMockData(users = listOf(me))
    private val db = PnutRepositoryMock(mockData)
    private val getProfileUseCase = GetProfileUseCase(db)

    @Test
    fun succeed() {
        val input = GetProfileInputData(me.id)
        val output = runBlocking { getProfileUseCase.run(input) }
        assertThat(output.res.data).isEqualTo(me)
    }

    @Test(expected = ErrorCollections.CommunicationError::class)
    fun fail() {
        val input = GetProfileInputData("")
        runBlocking { getProfileUseCase.run(input) }
    }
}