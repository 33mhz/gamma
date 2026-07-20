package io.pnut.gamma.domain.entity

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.pnut.gamma.domain.entity.entities.Entities
import io.pnut.gamma.domain.entity.entities.HaveEntities
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
    @Json(name = "thread_id") val threadId: String,
    val user: User?,
    @Json(name = "user_id") val userId: String? = null,
    val content: MessageContent?,
    @Json(name = "pagination_id") override var paginationId: String? = null

) : Parcelable, UniquePageable {
    @IgnoredOnParcel
    override val uniqueKey by lazy { id }

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class MessageCount(
        val replies: Int,
    ) : Parcelable

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class MessageContent(
        override val entities: Entities?,
        override val html: String?,
        override val text: String?
    ) : HaveEntities, Parcelable
}




