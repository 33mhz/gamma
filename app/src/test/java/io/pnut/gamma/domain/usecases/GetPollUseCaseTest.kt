package io.pnut.gamma.domain.usecases

import kotlinx.coroutines.runBlocking
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.Poll
import io.pnut.gamma.domain.model.io.GetPollInputData
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.util.Response
import io.pnut.gamma.sample.Polls
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Test

class GetPollUseCaseTest {
  @Test
  fun success() {
    val poll = Polls.poll1
    val useCase = GetPollUseCase(object : PnutRepositoryMock() {
      override suspend fun getPoll(pollId: String, pollToken: String): PnutResponse<Poll> {
        return Response.success(poll)
      }
    })
    val res = runBlocking { useCase.run(GetPollInputData("1", "pollToken")) }
    Assert.assertThat(res.poll, `is`(poll))
  }
}