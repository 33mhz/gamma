package io.pnut.gamma.domain.entity

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@JsonClass(generateAdapter = true)
data class Channel(
    val acl: Acl,
    val counts: ChannelCount,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "has_sticky_messages") val hasStickyMessages: Boolean,
    @Json(name = "has_unread") val hasUnread: Boolean,
    val id: String,
    @Json(name = "is_active") val isActive: Boolean? = true,
    @Json(name = "recent_message") val recentMessage: Message? = null,
    @Json(name = "recent_message_id") val recentMessageId: String? = null,
    val type: String,
    @Json(name = "you_muted") val youMuted: Boolean,
    @Json(name = "you_subscribed") val youSubscribed: Boolean,
    val user: User? = null,
    @Json(name = "user_id") val userId: String? = null,
    @Json(name = "pagination_id") override var paginationId: String? = null,
    @Json(name = "raw") val raw: Map<String, List<io.pnut.gamma.domain.entity.raw.RawValue>>? = null
) : Parcelable, UniquePageable {
    @IgnoredOnParcel
    override val uniqueKey: String by lazy { id }
    @Parcelize
    @JsonClass(generateAdapter = true)
    data class ChannelCount(
        val messages: Int,
        val subscribers: Int? = null
    ) : Parcelable

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Acl(
        val full: Full,
        val write: Write,
        val read: Read
    ) : Parcelable {
        @Parcelize
        @JsonClass(generateAdapter = true)
        data class Full(
            override val immutable: Boolean,
            override val you: Boolean,
            @Json(name = "user_ids") override val userIds: List<String>
        ) : IAuthority, Parcelable

        @Parcelize
        @JsonClass(generateAdapter = true)
        data class Write(
            override val immutable: Boolean,
            override val you: Boolean,
            @Json(name = "user_ids") override val userIds: List<String>,
            @Json(name = "any_user") val anyUser: Boolean
        ) : IAuthority, Parcelable

        @Parcelize
        @JsonClass(generateAdapter = true)
        data class Read(
            override val immutable: Boolean,
            override val you: Boolean,
            @Json(name = "user_ids") override val userIds: List<String>,
            @Json(name = "any_user") val anyUser: Boolean,
            val public: Boolean
        ) : IAuthority, Parcelable

        private interface IAuthority {
            val immutable: Boolean
            val you: Boolean
            @Json(name = "user_ids")
            val userIds: List<String>
        }
    }


}
