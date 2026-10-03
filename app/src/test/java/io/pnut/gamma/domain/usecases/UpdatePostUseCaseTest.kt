package io.pnut.gamma.domain.usecases

import com.google.common.truth.Truth.assertThat
import io.pnut.gamma.domain.entity.PostBody
import io.pnut.gamma.domain.model.Account
import io.pnut.gamma.domain.model.io.UpdatePostInputData
import io.pnut.gamma.mock.AccountRepositoryMock
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.util.ErrorCollections
import io.pnut.gamma.util.RandomID
import io.pnut.gamma.util.TestException
import kotlinx.coroutines.runBlocking
import org.junit.Test

class UpdatePostUseCaseTest {
    private val pnutRepositoryMockData = PnutRepositoryMock.PnutMockData()
    private val pnutRepository = PnutRepositoryMock(pnutRepositoryMockData)
    private val me = Account(RandomID.getID, "valid token", "foo", "bar")
    private val accountRepository = AccountRepositoryMock(listOf(me))
    private val updatePostUseCase = UpdatePostUseCase(pnutRepository, accountRepository)

    @Test
    fun succeed() {
        val postBody = PostBody("updated body")
        val input = UpdatePostInputData("123", postBody, me.id)
        val output = runBlocking { updatePostUseCase.run(input) }
        assertThat(output.res.data.id).isEqualTo("123")
        assertThat(output.res.data.content?.text).isEqualTo("updated body")
    }

    @Test(expected = TestException::class)
    fun failBecauseBodyIsEmpty() {
        val postBody = PostBody("")
        val input = UpdatePostInputData("123", postBody, me.id)
        runBlocking { updatePostUseCase.run(input) }
    }

    @Test(expected = ErrorCollections.AccountNotFound::class)
    fun failBecauseAccountNotFound() {
        val postBody = PostBody("updated body")
        val input = UpdatePostInputData("123", postBody, "")
        runBlocking { updatePostUseCase.run(input) }
    }
}
