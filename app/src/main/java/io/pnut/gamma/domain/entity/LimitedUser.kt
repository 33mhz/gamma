package io.pnut.gamma.domain.entity

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class LimitedUser(
    @Json(name = "avatar_image") val avatarImage: String,
    val id: String,
    val name: String? = null,
    val username: String,
    @Json(name = "pagination_id") override val paginationId: String? = null
) : UniquePageable, Parcelable {
    @IgnoredOnParcel
    override val uniqueKey: String by lazy { id }
}
