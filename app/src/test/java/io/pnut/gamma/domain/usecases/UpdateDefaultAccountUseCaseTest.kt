package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.UpdateDefaultAccountInputData
import io.pnut.gamma.mock.AccountRepositoryMock
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.sample.Accounts
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Test

class UpdateDefaultAccountUseCaseTest {
  @Test
  fun success() {
    val account = Accounts.account
    val useCase = UpdateDefaultAccountUseCase(object : AccountRepositoryMock() {
      override fun updateDefaultAccount(id: String) {
        Assert.assertThat(id, `is`(account.id))
      }

      override fun getToken(id: String): String? {
        return account.token
      }
    }, object : PnutRepositoryMock() {
      override fun updateDefaultPnutService(token: String) {
        Assert.assertThat(token, `is`(account.token))
      }
    })
    val res = useCase.run(UpdateDefaultAccountInputData(account.id))
    Assert.assertThat(res.result, `is`(true))
  }

  @Test
  fun failed() {
    val account = Accounts.account
    val useCase = UpdateDefaultAccountUseCase(object : AccountRepositoryMock() {
      override fun updateDefaultAccount(id: String) {
        Assert.assertThat(id, `is`(account.id))
      }

      override fun getToken(id: String): String? {
        return null
      }
    }, object : PnutRepositoryMock() {
      override fun updateDefaultPnutService(token: String) {
        Assert.assertThat(token, `is`(account.token))
      }
    })
    val res = useCase.run(UpdateDefaultAccountInputData(account.id))
    Assert.assertThat(res.result, `is`(false))
  }
}