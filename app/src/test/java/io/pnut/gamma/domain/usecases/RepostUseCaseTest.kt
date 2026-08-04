package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.domain.model.io.RepostInputData
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.util.Response
import io.pnut.gamma.util.TestException
import io.pnut.gamma.sample.Posts
import org.junit.Test
import com.google.common.truth.Truth.assertThat


class RepostUseCaseTest {
    private val post = Posts.normalPost.copy(id = "1")

    @Test
    fun succeedToRepost() {
        val pnutRepositoryMock = object : PnutRepositoryMock() {
            override fun createRepostSync(postId: String): PnutResponse<Post> {
                val hasRepostOfPost = Posts.normalPost.copy(repostOf = post, youReposted = true)
                return Response.success(hasRepostOfPost)
            }
        }
        val repostUseCase = RepostUseCase(pnutRepositoryMock)
        val res = repostUseCase.run(RepostInputData("1", true))
        assertThat(res.res.data.youReposted).isTrue()
        assertThat(res.res.data.repostOf?.id).isEqualTo("1")
    }

    @Test(expected = TestException::class)
    fun failToRepost() {
        val pnutRepositoryMock = object : PnutRepositoryMock() {
            override fun createRepostSync(postId: String): PnutResponse<Post> {
                throw TestException()
            }
        }
        val repostUseCase = RepostUseCase(pnutRepositoryMock)
        repostUseCase.run(RepostInputData("1", true))
    }


    @Test
    fun succeedToDeleteRepost() {
        val pnutRepositoryMock = object : PnutRepositoryMock() {
            override fun deleteRepostSync(postId: String): PnutResponse<Post> {
                return Response.success(Posts.normalPost)
            }
        }
        val repostUseCase = RepostUseCase(pnutRepositoryMock)
        val res = repostUseCase.run(RepostInputData("1", false))
        assertThat(res.res.data.youReposted).isFalse()
        assertThat(res.res.data.repostOf).isNull()
    }

    @Test(expected = TestException::class)
    fun failToDeleteRepost() {
        val pnutRepositoryMock = object : PnutRepositoryMock() {
            override fun deleteRepostSync(postId: String): PnutResponse<Post> {
                throw TestException()
            }
        }
        val repostUseCase = RepostUseCase(pnutRepositoryMock)
        repostUseCase.run(RepostInputData("1", false))
    }
}
