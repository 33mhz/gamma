package io.pnut.gamma.domain.usecases

import kotlinx.coroutines.runBlocking
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.domain.model.io.StarInputData
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.util.TestException
import io.pnut.gamma.sample.Posts
import org.junit.Test
import com.google.common.truth.Truth.assertThat


class StarUseCaseTest {
    private val unStarredPost = Posts.unStarredPost
    private val starredPost = Posts.starredPost

    @Test
    fun succeedToStar() {
        val starUseCase = StarUseCase(object : PnutRepositoryMock() {
            override suspend fun createStarPostSync(postId: String, note: String?): PnutResponse<Post> {
                return PnutResponse(
                    PnutResponse.Meta(200),
                    unStarredPost.copy(youBookmarked = true, note = note)
                )
            }
        })
        val starOutputData = runBlocking { starUseCase.run(StarInputData(unStarredPost.id, true, "test note")) }
        assertThat(starOutputData.res.data).isEqualTo(unStarredPost.copy(youBookmarked = true, note = "test note"))
    }

    @Test(expected = TestException::class)
    fun failToStar() {
        val starUseCase = StarUseCase(object : PnutRepositoryMock() {
            override suspend fun createStarPostSync(postId: String, note: String?): PnutResponse<Post> {
                throw TestException()
            }
        })
        runBlocking { starUseCase.run(StarInputData(starredPost.id, true)) }
    }

    @Test
    fun succeedToUnStar() {
        val starUseCase = StarUseCase(object : PnutRepositoryMock() {
            override fun deleteStarPostSync(postId: String): PnutResponse<Post> {
                return PnutResponse(PnutResponse.Meta(200), starredPost.copy(youBookmarked = false))
            }
        })
        val starOutputData = runBlocking { starUseCase.run(StarInputData(starredPost.id, false)) }
        assertThat(starOutputData.res.data).isEqualTo(starredPost.copy(youBookmarked = false))
    }

    @Test(expected = TestException::class)
    fun failToUnStar() {
        val starUseCase = StarUseCase(object : PnutRepositoryMock() {
            override fun deleteStarPostSync(postId: String): PnutResponse<Post> {
                throw TestException()
            }
        })
        runBlocking { starUseCase.run(StarInputData(unStarredPost.id, false)) }
    }
}