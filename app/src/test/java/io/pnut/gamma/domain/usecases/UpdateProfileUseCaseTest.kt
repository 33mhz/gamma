package io.pnut.gamma.domain.usecases

import kotlinx.coroutines.runBlocking
import io.pnut.gamma.domain.model.io.UpdateProfileInputData
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.sample.Users
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class UpdateProfileUseCaseTest {
    private val me = Users.me
    private val mockData = PnutRepositoryMock.PnutMockData(users = listOf(me))
    private val db = PnutRepositoryMock(mockData)
    private val updateProfileUseCase = UpdateProfileUseCase(db)

    @Test
    fun succeed() {
        val input = UpdateProfileInputData("foo", "bar", "Asia/Tokyo", "ja_JP")
        val output = runBlocking { updateProfileUseCase.run(input) }
        val user = output.user
        assertThat(user.name).isEqualTo("foo")
        assertThat(user.content.text).isEqualTo("bar")
        assertThat(user.timezone).isEqualTo("Asia/Tokyo")
        assertThat(user.locale).isEqualTo("ja_JP")
    }

    @Test
    fun clearAll() {
        val input = UpdateProfileInputData("", "", "", "")
        val output = runBlocking { updateProfileUseCase.run(input) }
        val user = output.user
        assertThat(user.name).isEmpty()
        assertThat(user.content.text).isEmpty()
        assertThat(user.timezone).isEmpty()
        assertThat(user.locale).isEmpty()
    }
}