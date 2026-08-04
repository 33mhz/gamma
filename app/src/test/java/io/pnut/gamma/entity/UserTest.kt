package io.pnut.gamma.entity

import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.sample.Users
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class UserTest {
  @Test
  fun itIsMe() {
    val me = Users.me
    assertThat(me.me).isTrue()
  }

  @Test
  fun itIsNotMe() {
    val me = Users.others
    assertThat(me.me).isFalse()
  }

  @Test
  fun getCanonicalUrl() {
    val canonicalUrl = User.getCanonicalUrl("abc")
    assertThat(canonicalUrl).isEqualTo("https://pnut.io/@abc")
  }

  @Test
  fun getCoverUrl() {
    val coverUrl = User.getCoverUrl("123")
    assertThat(coverUrl).isEqualTo("https://api.pnut.io/v1/users/123/cover")
  }

  @Test
  fun avatarSize() {
    val defaultSizeAvatarUrl = User.getAvatarUrl("123")
    assertThat(defaultSizeAvatarUrl).isEqualTo("https://api.pnut.io/v1/users/123/avatar?h=64")

    val originalAvatarUrl = User.getAvatarUrl("123", null)
    assertThat(originalAvatarUrl).isEqualTo("https://api.pnut.io/v1/users/123/avatar")

    val size24AvatarUrl = User.getAvatarUrl("123", User.AvatarSize.Mini)
    assertThat(size24AvatarUrl).isEqualTo("https://api.pnut.io/v1/users/123/avatar?h=24")
    val size48AvatarUrl = User.getAvatarUrl("123", User.AvatarSize.Small)
    assertThat(size48AvatarUrl).isEqualTo("https://api.pnut.io/v1/users/123/avatar?h=48")

    val size64AvatarUrl = User.getAvatarUrl("123", User.AvatarSize.Normal)
    assertThat(size64AvatarUrl).isEqualTo("https://api.pnut.io/v1/users/123/avatar?h=64")

    val size96AvatarUrl = User.getAvatarUrl("123", User.AvatarSize.Large)
    assertThat(size96AvatarUrl).isEqualTo("https://api.pnut.io/v1/users/123/avatar?h=96")

    val user = Users.me
    val url = user.content.avatarImage.url
    val originalAvatarUrlOfUser = User.getAvatarUrl(user, null)
    assertThat(originalAvatarUrlOfUser).isEqualTo(url)

    val size24AvatarUrlOfUser = User.getAvatarUrl(user, User.AvatarSize.Mini)
    assertThat(size24AvatarUrlOfUser).isEqualTo("$url?h=24")

    val size48AvatarUrlOfUser = User.getAvatarUrl(user, User.AvatarSize.Small)
    assertThat(size48AvatarUrlOfUser).isEqualTo("$url?h=48")

    val size64AvatarUrlOfUser = User.getAvatarUrl(user, User.AvatarSize.Normal)
    assertThat(size64AvatarUrlOfUser).isEqualTo("$url?h=64")

    val size96AvatarUrlOfUser = User.getAvatarUrl(user, User.AvatarSize.Large)
    assertThat(size96AvatarUrlOfUser).isEqualTo("$url?h=96")

  }
}