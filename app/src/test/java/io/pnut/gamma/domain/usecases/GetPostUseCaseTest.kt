package io.pnut.gamma.domain.usecases

import kotlinx.coroutines.runBlocking
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.domain.entity.entities.BaseContent
import io.pnut.gamma.domain.model.StreamType
import io.pnut.gamma.domain.model.io.GetPostInputData
import io.pnut.gamma.domain.model.params.composed.GetPostsParam
import io.pnut.gamma.mock.PnutRepositoryMock
import io.pnut.gamma.mock.PreferenceRepositoryMock
import io.pnut.gamma.sample.Posts
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class GetPostUseCaseTest {

  private fun generatePosts(prefix: String): List<Post> {
    val post1 = Posts.normalPost.copy(content = BaseContent(text = "${prefix}1"))
    val post2 = Posts.normalPost.copy(content = BaseContent(text = "${prefix}2"))
    val post3 = Posts.normalPost.copy(content = BaseContent(text = "${prefix}3"))
    return listOf(post1, post2, post3)
  }

  @Test
  fun getPersonalStream() {

    val useCase = GetPostUseCase(object : PnutRepositoryMock() {
      override suspend fun getPersonalStream(getPostsParam: GetPostsParam): PnutResponse<List<Post>> {
        return PnutResponse(
          PnutResponse.Meta(200), generatePosts("home")
        )
      }
    }, PreferenceRepositoryMock())
    val res = runBlocking { useCase.run(GetPostInputData(StreamType.Home, GetPostsParam())) }
    assertThat(res.res.data[0].content?.text).isEqualTo("home1")
    assertThat(res.res.data[1].content?.text).isEqualTo("home2")
    assertThat(res.res.data[2].content?.text).isEqualTo("home3")
  }

  @Test
  fun getUnifiedStream() {
    val useCase = GetPostUseCase(object : PnutRepositoryMock() {
      override suspend fun getUnifiedStream(getPostsParam: GetPostsParam): PnutResponse<List<Post>> {
        return PnutResponse(
          PnutResponse.Meta(200), generatePosts("unified")
        )
      }
    }, object : PreferenceRepositoryMock() {
      override val unifiedStream: Boolean = true
    })
    val res = runBlocking { useCase.run(GetPostInputData(StreamType.Home, GetPostsParam())) }
    assertThat(res.res.data[0].content?.text).isEqualTo("unified1")
    assertThat(res.res.data[1].content?.text).isEqualTo("unified2")
    assertThat(res.res.data[2].content?.text).isEqualTo("unified3")
  }

  @Test
  fun getMentionStream() {
    val useCase = GetPostUseCase(object : PnutRepositoryMock() {
      override suspend fun getMentionStream(getPostsParam: GetPostsParam): PnutResponse<List<Post>> {
        return PnutResponse(
          PnutResponse.Meta(200), generatePosts("mention")
        )
      }
    }, PreferenceRepositoryMock())
    val res = runBlocking { useCase.run(GetPostInputData(StreamType.Mentions, GetPostsParam())) }
    assertThat(res.res.data[0].content?.text).isEqualTo("mention1")
    assertThat(res.res.data[1].content?.text).isEqualTo("mention2")
    assertThat(res.res.data[2].content?.text).isEqualTo("mention3")
  }

  @Test
  fun getStarsPosts() {
    val useCase = GetPostUseCase(object : PnutRepositoryMock() {
      override suspend fun getStars(
        userId: String,
        getPostsParam: GetPostsParam
      ): PnutResponse<List<Post>> {
        return PnutResponse(
          PnutResponse.Meta(200), generatePosts("stars")
        )
      }
    }, PreferenceRepositoryMock())
    val res = runBlocking { useCase.run(GetPostInputData(StreamType.Stars("me"), GetPostsParam())) }
    assertThat(res.res.data[0].content?.text).isEqualTo("stars1")
    assertThat(res.res.data[1].content?.text).isEqualTo("stars2")
    assertThat(res.res.data[2].content?.text).isEqualTo("stars3")
  }

  @Test
  fun getTagPosts() {
    val useCase = GetPostUseCase(object : PnutRepositoryMock() {
      override suspend fun getTagStream(
        tag: String,
        getPostsParam: GetPostsParam
      ): PnutResponse<List<Post>> {
        return PnutResponse(
          PnutResponse.Meta(200), generatePosts("tag")
        )
      }
    }, PreferenceRepositoryMock())
    val res = runBlocking { useCase.run(GetPostInputData(StreamType.Tag("tag"), GetPostsParam())) }
    assertThat(res.res.data[0].content?.text).isEqualTo("tag1")
    assertThat(res.res.data[1].content?.text).isEqualTo("tag2")
    assertThat(res.res.data[2].content?.text).isEqualTo("tag3")
  }


  @Test
  fun getUserPosts() {
    val useCase = GetPostUseCase(object : PnutRepositoryMock() {
      override suspend fun getUserPosts(
        userId: String,
        getPostsParam: GetPostsParam
      ): PnutResponse<List<Post>> {
        return PnutResponse(
          PnutResponse.Meta(200), generatePosts("user")
        )
      }
    }, PreferenceRepositoryMock())
    val res = runBlocking { useCase.run(GetPostInputData(StreamType.User("me"), GetPostsParam())) }
    assertThat(res.res.data[0].content?.text).isEqualTo("user1")
    assertThat(res.res.data[1].content?.text).isEqualTo("user2")
    assertThat(res.res.data[2].content?.text).isEqualTo("user3")
  }

  @Test
  fun getThreadPosts() {
    val useCase = GetPostUseCase(object : PnutRepositoryMock() {
      override suspend fun getThread(
        postId: String,
        params: GetPostsParam
      ): PnutResponse<List<Post>> {
        return PnutResponse(
          PnutResponse.Meta(200), generatePosts("thread")
        )
      }
    }, PreferenceRepositoryMock())
    val res = runBlocking { useCase.run(GetPostInputData(StreamType.Thread("1"), GetPostsParam())) }
    assertThat(res.res.data[0].content?.text).isEqualTo("thread1")
    assertThat(res.res.data[1].content?.text).isEqualTo("thread2")
    assertThat(res.res.data[2].content?.text).isEqualTo("thread3")
  }

  @Test
  fun getPostsByIds() {
    val ids = listOf("1", "2", "3")
    val useCase = GetPostUseCase(object : PnutRepositoryMock() {
      override suspend fun getPosts(ids: io.pnut.gamma.domain.entity.IDs): PnutResponse<List<Post>> {
        return PnutResponse(
          PnutResponse.Meta(200), generatePosts("posts")
        )
      }
    }, PreferenceRepositoryMock())
    val res = runBlocking { useCase.run(GetPostInputData(StreamType.Posts(ids), GetPostsParam())) }
    assertThat(res.res.data[0].content?.text).isEqualTo("posts1")
    assertThat(res.res.data[1].content?.text).isEqualTo("posts2")
    assertThat(res.res.data[2].content?.text).isEqualTo("posts3")
  }
}