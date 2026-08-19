package io.pnut.gamma.domain.usecases

import kotlinx.coroutines.runBlocking
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.domain.model.io.DeletePostInputData
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.sample.Posts
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class DeletePostUseCaseTest {
  @Test
  fun success() {
    val post = Posts.normalPost
    val useCase = DeletePostUseCase(object : PnutRepositoryMock() {
      override suspend fun deletePost(postId: String): PnutResponse<Post> {
        return PnutResponse(
          PnutResponse.Meta(200), post.copy(
            isDeleted = true, content = null
          )
        )
      }
    })
    val res = runBlocking {
      useCase.run(DeletePostInputData(post.id))
    }
    assertThat(res.res.data.isDeleted).isTrue()
    assertThat(res.res.data.content).isNull()
  }
}