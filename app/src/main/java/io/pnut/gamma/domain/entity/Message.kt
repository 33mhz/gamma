package io.pnut.gamma.domain.entity

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.pnut.gamma.domain.entity.entities.BaseContent
import io.pnut.gamma.domain.entity.raw.RawValue
import io.pnut.gamma.domain.entity.raw.Spoiler
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Calendar
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
    @Json(name = "is_nsfw") val isNsfw: Boolean? = false,
    val content: BaseContent?,
    @Json(name = "pagination_id") override var paginationId: String? = null,
    @Json(name = "raw") var raw: Map<String, List<RawValue>>? = null

) : Parcelable, UniquePageable {
    @IgnoredOnParcel
    override val uniqueKey by lazy { id }

    @IgnoredOnParcel
    var nsfwMask = isNsfw ?: false
    @IgnoredOnParcel
    var spoilerMask = false
    @IgnoredOnParcel
    val showContents: Boolean
        get() = !nsfwMask && !spoilerMask
    @IgnoredOnParcel
    val spoiler = Spoiler.getSpoiler(raw)

    init {
        spoilerMask = spoiler?.let {
            val spoilerDate = it.expiredAt ?: return@let true
            spoilerDate.time > Calendar.getInstance().time.time
        } ?: false
    }

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




