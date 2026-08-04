package io.pnut.gamma.domain.usecases

import kotlinx.coroutines.runBlocking
import io.pnut.gamma.domain.entity.Poll
import io.pnut.gamma.domain.model.io.VoteInputData
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.sample.Polls
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class VoteUseCaseTest {
    private fun getVoteUseCase(vararg polls: Poll): VoteUseCase {
        val mockData = PnutRepositoryMock.PnutMockData(polls = listOf(*polls))
        val db = PnutRepositoryMock(mockData)
        return VoteUseCase(db)
    }

    @Test
    fun succeed() {
        val poll = Polls.poll1
        val voteUseCase = getVoteUseCase(poll)
        val input = VoteInputData(poll.id, poll.pollToken, setOf(0, 1, 2))
        val output = runBlocking { voteUseCase.run(input) }
        assertThat(output.poll.options[0].isYourResponse).isTrue()
        assertThat(output.poll.options[1].isYourResponse).isTrue()
        assertThat(output.poll.options[2].isYourResponse).isTrue()
        assertThat(output.poll.options[4].isYourResponse).isNotEqualTo(true)
        assertThat(output.poll.options[5].isYourResponse).isNotEqualTo(true)
        assertThat(output.poll.options[6].isYourResponse).isNotEqualTo(true)
        assertThat(output.poll.options[7].isYourResponse).isNotEqualTo(true)
        assertThat(output.poll.options[8].isYourResponse).isNotEqualTo(true)
        assertThat(output.poll.options[9].isYourResponse).isNotEqualTo(true)
    }

    @Test(expected = NoSuchElementException::class)
    fun notFound() {
        val voteUseCase = getVoteUseCase()
        val input = VoteInputData("", "", setOf(0))
        runBlocking { voteUseCase.run(input) }
    }
}