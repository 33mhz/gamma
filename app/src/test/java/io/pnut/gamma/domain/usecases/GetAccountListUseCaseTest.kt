package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.Account
import io.pnut.gamma.mock.AccountRepositoryMock
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Test

class GetAccountListUseCaseTest {
  @Test
  fun getAccountList() {
    val account123 = Account("123", "", "", "")
    val account456 = Account("456", "", "", "")
    val useCase = GetAccountListUseCase(object : AccountRepositoryMock() {
      override fun getStoredIds(): List<String> {
        return listOf("123", "456")
      }

      override fun getAccount(id: String): Account {
        return Account(id, "", "", "")
      }
    })
    Assert.assertThat(useCase.run(Unit).accounts, `is`(listOf(account123, account456)))
  }

  @Test
  fun empty() {
    val useCase = GetAccountListUseCase(object : AccountRepositoryMock() {
      override fun getStoredIds(): List<String> {
        return emptyList()
      }

      override fun getAccount(id: String): Account? {
        return null
      }
    })
    Assert.assertThat(useCase.run(Unit).accounts, `is`(emptyList()))
  }
}