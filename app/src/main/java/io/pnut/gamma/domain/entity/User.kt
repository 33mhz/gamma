package io.pnut.gamma.domain.entity

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.pnut.gamma.domain.entity.entities.UserContent
import io.pnut.gamma.util.Constants
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import io.pnut.gamma.R
import java.util.Date

@Parcelize
@JsonClass(generateAdapter = true)
data class User(
    val badge: Badge? = null,
    val content: UserContent,
    val counts: UserCount,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "follows_you") val followsYou: Boolean,
    val id: String,
    val locale: String,
    val name: String? = null,
    val timezone: String,
    val type: AccountType,
    val username: String,
    @Json(name = "you_blocked") var youBlocked: Boolean,
    @Json(name = "you_can_follow") val youCanFollow: Boolean,
    @Json(name = "you_follow") var youFollow: Boolean,
    @Json(name = "you_muted") var youMuted: Boolean,
    val verified: VerifiedDomain? = null,
    @Json(name = "pagination_id") override val paginationId: String? = null
) : UniquePageable, Parcelable {
    @IgnoredOnParcel
    override val uniqueKey: String by lazy { id }

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class UserCount(
        val bookmarks: Int,
        val clients: Int,
        val followers: Int,
        val following: Int,
        val posts: Int
    ) : Parcelable

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Badge(
        val id: String,
        val name: String
    ) : Parcelable

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class VerifiedDomain(
        val domain: String,
        val url: String
    ) : Parcelable

    enum class AccountType {
        @Json(name = "human")
        HUMAN,
        @Json(name = "feed")
        FEED,
        @Json(name = "bot")
        BOT
    }

    @IgnoredOnParcel
    val me = followsYou && youFollow && !youCanFollow

    @IgnoredOnParcel
    val relationshipTextRes: Int
        get() = me.takeIf { !it }.let {
            when {
                youBlocked -> R.string.blocked
                youMuted -> R.string.muted
                youFollow -> R.string.following
                else -> R.string.follow
            }
        }

    enum class AvatarSize(val size: Int) { Mini(24), Small(48), Normal(64), Large(96), ExtraLarge(128) }

    fun getAvatarUrl(avatarSize: AvatarSize? = AvatarSize.Normal) = getAvatarUrl(this, avatarSize)

    companion object {
        fun getAvatarUrl(user: User, avatarSize: AvatarSize? = AvatarSize.Normal) = when {
            avatarSize != null -> "${user.content.avatarImage.url}?h=${avatarSize.size}"
            else -> user.content.avatarImage.url
        }
        fun getAvatarUrl(id: String, avatarSize: AvatarSize? = AvatarSize.Normal) = when {
            avatarSize != null -> Constants.API_BASE_URL + "users/$id/avatar?h=${avatarSize.size}"
            else -> Constants.API_BASE_URL + "users/$id/avatar"
        }

        fun getCoverUrl(id: String) = Constants.API_BASE_URL + "users/$id/cover"

        fun getCanonicalUrl(username: String) = "https://pnut.io/@$username"
    }
}
