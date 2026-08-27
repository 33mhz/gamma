package io.pnut.gamma.domain.entity

import io.pnut.gamma.sample.Clients
import org.junit.Test
import com.google.common.truth.Truth.assertThat
import java.util.*

class PostRevisedTest {
    @Test
    fun testIsRevisedMapping() {
        val post = Post(
            id = "1",
            createdAt = Date(),
            source = Clients.testClient,
            threadId = "1",
            counts = Post.PostCount(0, 0, 0, 0),
            isRevised = true
        )
        assertThat(post.isRevised).isTrue()
    }
}
