package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.UpdateDefaultAccountInputData
import io.pnut.gamma.mock.AccountRepositoryMock
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.sample.Accounts
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class UpdateDefaultAccountUseCaseTest {
  @Test
  fun success() {
    val account = Accounts.account
    val useCase = UpdateDefaultAccountUseCase(object : AccountRepositoryMock() {
      override fun updateDefaultAccount(id: String) {
        assertThat(id).isEqualTo(account.id)
      }

      override fun getToken(id: String): String {
        return account.token
      }
    }, object : PnutRepositoryMock() {
      override fun updateDefaultPnutService(token: String) {
        assertThat(token).isEqualTo(account.token)
      }
    })
    val res = useCase.run(UpdateDefaultAccountInputData(account.id))
    assertThat(res.result).isTrue()
  }

  @Test
  fun failed() {
    val account = Accounts.account
    val useCase = UpdateDefaultAccountUseCase(object : AccountRepositoryMock() {
      override fun updateDefaultAccount(id: String) {
        assertThat(id).isEqualTo(account.id)
      }

      override fun getToken(id: String): String? {
        return null
      }
    }, object : PnutRepositoryMock() {
      override fun updateDefaultPnutService(token: String) {
        assertThat(token).isEqualTo(account.token)
      }
    })
    val res = useCase.run(UpdateDefaultAccountInputData(account.id))
    assertThat(res.result).isFalse()
  }
}