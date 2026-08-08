package io.pnut.gamma.domain.entity

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.pnut.gamma.domain.entity.entities.BaseContent
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@JsonClass(generateAdapter = true)
data class Message(
    val counts: MessageCount,
    @Json(name = "created_at") val createdAt: Date,
    val id: String,
    @Json(name = "is_deleted") val isDeleted: Boolean? = false,
    @Json(name = "is_sticky") val isSticky: Boolean,
    val source: Client,
    @Json(name = "reply_to") val replyTo: String? = null,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "thread_id") val threadId: String,
    val user: User?,
    @Json(name = "user_id") val rawUserId: String? = null,
    val content: BaseContent?,
    @Json(name = "pagination_id") override var paginationId: String? = null

) : Parcelable, UniquePageable {
    @IgnoredOnParcel
    override val uniqueKey by lazy { id }

    @IgnoredOnParcel
    val userId: String? get() = user?.id ?: rawUserId
    @IgnoredOnParcel
    val username: String? get() = user?.username
    @IgnoredOnParcel
    val name: String? get() = user?.name
    @IgnoredOnParcel
    val text: String? get() = content?.text
    @IgnoredOnParcel
    val avatarUrl: String? get() = user?.content?.avatarImage?.url

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class MessageCount(
        val replies: Int,
    ) : Parcelable
}




