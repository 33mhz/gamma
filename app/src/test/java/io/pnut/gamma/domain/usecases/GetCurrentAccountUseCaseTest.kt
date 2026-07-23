package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.Account
import io.pnut.gamma.mock.AccountRepositoryMock
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Test

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
    Assert.assertThat(res.account?.id, `is`(account.id))
    Assert.assertThat(res.account?.name, `is`(account.name))
    Assert.assertThat(res.account?.screenName, `is`(account.screenName))
    Assert.assertThat(res.account?.screenName, `is`(account.screenName))
  }
}