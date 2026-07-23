package io.pnut.gamma.sample

import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.domain.entity.entities.Entities
import io.pnut.gamma.util.RandomID
import java.util.*

object Posts {
    val normalPost
        get() = Post(
            id = RandomID.get,
            createdAt = Date(),
            youBookmarked = false,
          youReposted = false,
          content = Post.PostContent(
            text = "post",
            html = "<span>post</span>",
            entities = Entities(emptyList(), emptyList(), emptyList()),
            linksNotParsed = false
          )
        )
    val unStarredPost
        get() = Post(
            id = RandomID.get,
            createdAt = Date(),
            youBookmarked = false
        )

    val starredPost
        get() = Post(
            id = RandomID.get,
            createdAt = Date(),
            youBookmarked = true
        )
}