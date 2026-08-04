package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.Account
import io.pnut.gamma.mock.AccountRepositoryMock
import org.junit.Test
import com.google.common.truth.Truth.assertThat

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
    assertThat(useCase.run(Unit).accounts).isEqualTo(listOf(account123, account456))
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
    assertThat(useCase.run(Unit).accounts).isEmpty()
  }
}