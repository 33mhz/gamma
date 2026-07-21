package io.pnut.gamma.domain.entity

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Token (
    val app: Client,
    val scopes: List<Scope>,
    val user: User,
    val storage: Storage
) : Parcelable {
    enum class Scope(val value: String) {
        @Json(name = "basic") BASIC("basic"),
        @Json(name = "stream") STREAM("stream"),
        @Json(name = "write_post") WRITE_POST("write_post"),
        @Json(name = "follow") FOLLOW("follow"),
        @Json(name = "update_profile") UPDATE_PROFILE("update_profile"),
        @Json(name = "presence") PRESENCE("presence"),
        @Json(name = "messages:io.pnut.core.chat") MESSAGES_CHAT("messages:io.pnut.core.chat"),
        @Json(name = "messages:io.pnut.core.pm") MESSAGES_PM("messages:io.pnut.core.pm"),
//        @Json(name = "public_messages") PUBLIC_MESSAGES("public_messages"),
        @Json(name = "files:io.pnut.delta") FILES_DELTA("files:io.pnut.delta"),
//        @Json(name = "polls") POLLS("polls"),
//        @Json(name = "email") EMAIL("email")
        ;

        override fun toString(): String = value
    }

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Storage(val available: Long, val total: Long) : Parcelable
}


