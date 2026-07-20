package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.domain.model.io.StarInputData
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.util.TestException
import io.pnut.gamma.sample.Posts
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Test


class StarUseCaseTest {
    private val unStarredPost = Posts.unStarredPost
    private val starredPost = Posts.starredPost

    @Test
    fun succeedToStar() {
        val starUseCase = StarUseCase(object : PnutRepositoryMock() {
            override fun createStarPostSync(postId: String): PnutResponse<Post> {
                return PnutResponse(
                    PnutResponse.Meta(200),
                    unStarredPost.copy(youBookmarked = true)
                )
            }
        })
        val starOutputData = starUseCase.run(StarInputData(unStarredPost.id, true))
        Assert.assertThat(starOutputData.res.data, `is`(unStarredPost.copy(youBookmarked = true)))
    }

    @Test(expected = TestException::class)
    fun failToStar() {
        val starUseCase = StarUseCase(object : PnutRepositoryMock() {
            override fun createStarPostSync(postId: String): PnutResponse<Post> {
                throw TestException()
            }
        })
        starUseCase.run(StarInputData(starredPost.id, true))
    }

    @Test
    fun succeedToUnStar() {
        val starUseCase = StarUseCase(object : PnutRepositoryMock() {
            override fun deleteStarPostSync(postId: String): PnutResponse<Post> {
                return PnutResponse(PnutResponse.Meta(200), starredPost.copy(youBookmarked = false))
            }
        })
        val starOutputData = starUseCase.run(StarInputData(starredPost.id, false))
        Assert.assertThat(starOutputData.res.data, `is`(starredPost.copy(youBookmarked = false)))
    }

    @Test(expected = TestException::class)
    fun failToUnStar() {
        val starUseCase = StarUseCase(object : PnutRepositoryMock() {
            override fun deleteStarPostSync(postId: String): PnutResponse<Post> {
                throw TestException()
            }
        })
        starUseCase.run(StarInputData(unStarredPost.id, false))
    }
}