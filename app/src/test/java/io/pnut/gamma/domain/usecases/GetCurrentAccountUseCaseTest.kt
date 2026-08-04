package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.Account
import io.pnut.gamma.mock.AccountRepositoryMock
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class GetCurrentAccountUseCaseTest {
  @Test
  fun success() {
    val account = Account("test", "testToken", "screenName", "name")
    val useCase = GetCurrentAccountUseCase(object : AccountRepositoryMock() {
      override fun getDefaultAccount(): Account {
        return account
      }
    })
    val res = useCase.run(Unit)
    assertThat(res.account?.id).isEqualTo(account.id)
    assertThat(res.account?.name).isEqualTo(account.name)
    assertThat(res.account?.screenName).isEqualTo(account.screenName)
  }
}