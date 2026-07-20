package io.pnut.gamma.domain.usecases

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.runBlocking
import io.pnut.gamma.domain.entity.Client
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.Token
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.model.io.GetAuthenticatedUserInputData
import io.pnut.gamma.mock.PnutCacheRepositoryMock
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.sample.Users
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito


class GetAuthenticatedUserUseCaseTest {
  @Test
  fun success() {
    val me = Users.me
    val cachedToken = Token(Client("testClient", "", ""), emptyList(), me, Token.Storage(0, 0))
    val latestToken = cachedToken.copy(user = cachedToken.user.copy(type = User.AccountType.BOT))
    val pnutRepository = object : PnutRepositoryMock() {
      override suspend fun getToken(): PnutResponse<Token> {
        return PnutResponse(PnutResponse.Meta(200), latestToken)
      }
    }
    val pnutCacheRepository = object : PnutCacheRepositoryMock() {
      override suspend fun getToken(): Token? {
        return cachedToken
      }

      override suspend fun storeToken(token: Token) {
        Assert.assertThat(token, `is`(latestToken))
      }
    }
    val liveData = Mockito.mock(MutableLiveData<Token>()::class.java)
    val useCase = GetAuthenticatedUserUseCase(pnutRepository, pnutCacheRepository)
    runBlocking { useCase.run(GetAuthenticatedUserInputData(liveData)) }
    Mockito.verify(liveData, Mockito.times(2)).postValue(Mockito.any())
  }
}