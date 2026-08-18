package io.pnut.gamma.domain.entity

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import io.pnut.gamma.domain.model.UriInfo
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class PostBodyOuter(
    val accountId: String,
    val postBody: PostBody,
    val files: List<UriInfo> = emptyList(),
    val pollPostBody: PollPostBody? = null
) : Parcelable