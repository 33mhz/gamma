package io.pnut.gamma.domain.entity

import android.os.Parcelable
import com.squareup.moshi.Json
import io.pnut.gamma.domain.entity.raw.RawValue
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostBody(
    val text: String,
    @Json(name = "reply_to") val replyTo: String? = null,
    @Json(name = "is_nsfw") val isNsfw: Boolean? = null,
    @Json(name = "entities.parse_links") val parseLinks: Boolean? = null,
    @Json(name = "entities.parse_markdown_links") val parseMarkdownLinks: Boolean? = null,
    val raw: Map<String, List<RawValue>>? = null
) : Parcelable
