package io.pnut.gamma.domain.usecases

import kotlinx.coroutines.runBlocking
import io.pnut.gamma.domain.entity.Client
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.Token
import io.pnut.gamma.domain.model.Account
import io.pnut.gamma.domain.model.io.VerifyTokenInputData
import io.pnut.gamma.mock.AccountRepositoryMock
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.util.TestException
import io.pnut.gamma.sample.Users
import io.pnut.gamma.util.RandomID
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Test

class VerifyTokenUseCaseTest {
    private val me = Users.me
    private val validToken = "valid"
    private val pnutRepository = object : PnutRepositoryMock() {
        override suspend fun verifyToken(token: String): PnutResponse<Token> {
            return when (token) {
                validToken -> {
                    val resToken = Token(
                        Client("", "", ""),
                        emptyList(),
                        me,
                        Token.Storage(0, 0)
                    )
                    PnutResponse(PnutResponse.Meta(200), resToken)
                }
                else -> throw TestException()
            }
        }
    }
    private val myAccount = Account(RandomID.getID, "access token", "foo", "bar")
    private val accountRepository = AccountRepositoryMock(listOf(myAccount))
    private val verifyTokenUseCase = VerifyTokenUseCase(accountRepository, pnutRepository)

    @Test
    fun succeed() {
        val input = VerifyTokenInputData(validToken)
        val output = runBlocking { verifyTokenUseCase.run(input) }
        Assert.assertThat(output.userData.user, `is`(me))
    }

    @Test(expected = Exception::class)
    fun fail() {
        val input = VerifyTokenInputData("")
        runBlocking { verifyTokenUseCase.run(input) }
    }
}