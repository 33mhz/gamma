package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.Account
import io.pnut.gamma.mock.AccountRepositoryMock
import io.pnut.gamma.mock.PnutRepositoryMock
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class LogoutUseCaseTest {
  private val pnutRepository = PnutRepositoryMock()

  @Test
  fun switchAnotherAccount() {
    val willBeDeletedAccount =
      Account("123", "deletedAccountToken", "deletedAccount", "deletedAccountName")
    val anotherAccount =
      Account("456", "anotherAccountToken", "anotherAccount", "anotherAccountName")
    val useCase = LogoutUseCase(object : AccountRepositoryMock() {
      override fun getDefaultAccount(): Account {
        return willBeDeletedAccount
      }

      override fun deleteAccount(id: String) {
        assertThat(id).isEqualTo(willBeDeletedAccount.id)
      }

      override fun getStoredIds(): List<String> {
        return listOf(willBeDeletedAccount.id, anotherAccount.id)
      }

      override fun getAccount(id: String): Account {
        return anotherAccount
      }
    }, pnutRepository)
    val res = useCase.run(Unit)
    assertThat(res.anotherAccountId).isEqualTo(anotherAccount.id)
  }

  @Test
  fun anotherAccountDoesNotExists() {
    val willBeDeletedAccount =
      Account("123", "deletedAccountToken", "deletedAccount", "deletedAccountName")
    val useCase = LogoutUseCase(object : AccountRepositoryMock() {
      override fun getDefaultAccount(): Account {
        return willBeDeletedAccount
      }

      override fun deleteAccount(id: String) {
        assertThat(id).isEqualTo(willBeDeletedAccount.id)
      }

      override fun getStoredIds(): List<String> {
        return emptyList()
      }

      override fun getAccount(id: String): Account? {
        return null
      }
    }, pnutRepository)
    val res = useCase.run(Unit)
    assertThat(res.anotherAccountId).isNull()
  }

  @Test
  fun getNullBecauseCannotGetDefaultAccount() {
    val useCase = LogoutUseCase(object : AccountRepositoryMock() {
      override fun getDefaultAccount(): Account? {
        return null
      }
    }, pnutRepository)
    val res = useCase.run(Unit)
    assertThat(res.anotherAccountId).isNull()
  }
}