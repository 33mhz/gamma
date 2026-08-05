package io.pnut.gamma.domain.entity.entities

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.pnut.gamma.domain.entity.image.Avatar
import io.pnut.gamma.domain.entity.image.Cover
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class BaseContent(
    override var text: String? = null,
    override var html: String? = null,
    override var entities: Entities? = null,
) : HaveEntities, Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class UserContent(
    @Json(name = "avatar_image") val avatarImage: Avatar,
    @Json(name = "cover_image") val coverImage: Cover,
    override val entities: Entities?,
    override val html: String?,
    @Json(name = "markdown_text") val markdownText: String?,
    override val text: String?
) : HaveEntities, Parcelable
