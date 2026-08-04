package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.entity.PostBody
import io.pnut.gamma.domain.model.Account
import io.pnut.gamma.domain.model.io.PostInputData
import io.pnut.gamma.mock.AccountRepositoryMock
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.util.ErrorCollections
import io.pnut.gamma.util.TestException
import io.pnut.gamma.util.RandomID
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class PostUseCaseTest {
    private val pnutRepositoryMockData = PnutRepositoryMock.PnutMockData()
    private val pnutRepository = PnutRepositoryMock(pnutRepositoryMockData)
    private val me = Account(RandomID.getID, "valid token", "foo", "bar")
    private val accountRepository = AccountRepositoryMock(listOf(me))
    private val postUseCase = PostUseCase(pnutRepository, accountRepository)

    @Test
    fun succeed() {
        val postBody = PostBody("body")
        val input = PostInputData(postBody, me.id)
        val output = postUseCase.run(input)
        assertThat(output.res.data.content?.text).isEqualTo("body")
    }

    @Test(expected = TestException::class)
    fun failBecauseBodyIsEmpty() {
        val postBody = PostBody("")
        val input = PostInputData(postBody, me.id)
        postUseCase.run(input)
    }

    @Test(expected = ErrorCollections.AccountNotFound::class)
    fun failBecauseBodyAccountNotFound() {
        val postBody = PostBody("")
        val input = PostInputData(postBody, "")
        postUseCase.run(input)
    }
}