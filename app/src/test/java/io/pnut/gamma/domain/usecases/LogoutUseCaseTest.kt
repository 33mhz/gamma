package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.Account
import io.pnut.gamma.mock.AccountRepositoryMock
import io.pnut.gamma.mock.PnutRepositoryMock
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert
import org.junit.Test

class LogoutUseCaseTest {
  private val pnutRepository = PnutRepositoryMock()

  @Test
  fun switchAnotherAccount() {
    val willBeDeletedAccount =
      Account("123", "deletedAccountToken", "deletedAccount", "deletedAccountName")
    val anotherAccount =
      Account("456", "anotherAccountToken", "anotherAccount", "anotherAccountName")
    val useCase = LogoutUseCase(object : AccountRepositoryMock() {
      override fun getDefaultAccount(): Account? {
        return willBeDeletedAccount
      }

      override fun deleteAccount(id: String) {
        Assert.assertThat(id, `is`(willBeDeletedAccount.id))
      }

      override fun getStoredIds(): List<String> {
        return listOf(willBeDeletedAccount.id, anotherAccount.id)
      }

      override fun getAccount(id: String): Account? {
        return anotherAccount
      }
    }, pnutRepository)
    val res = useCase.run(Unit)
    Assert.assertThat(res.anotherAccountId, `is`(anotherAccount.id))
  }

  @Test
  fun anotherAccountDoesNotExists() {
    val willBeDeletedAccount =
      Account("123", "deletedAccountToken", "deletedAccount", "deletedAccountName")
    val useCase = LogoutUseCase(object : AccountRepositoryMock() {
      override fun getDefaultAccount(): Account? {
        return willBeDeletedAccount
      }

      override fun deleteAccount(id: String) {
        Assert.assertThat(id, `is`(willBeDeletedAccount.id))
      }

      override fun getStoredIds(): List<String> {
        return emptyList()
      }

      override fun getAccount(id: String): Account? {
        return null
      }
    }, pnutRepository)
    val res = useCase.run(Unit)
    Assert.assertThat(res.anotherAccountId, `is`(nullValue()))
  }

  @Test
  fun getNullBecauseCannotGetDefaultAccount() {
    val useCase = LogoutUseCase(object : AccountRepositoryMock() {
      override fun getDefaultAccount(): Account? {
        return null
      }
    }, pnutRepository)
    val res = useCase.run(Unit)
    Assert.assertThat(res.anotherAccountId, `is`(nullValue()))
  }
}