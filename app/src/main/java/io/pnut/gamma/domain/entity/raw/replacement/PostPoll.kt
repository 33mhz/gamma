package io.pnut.gamma.domain.entity.raw.replacement

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.pnut.gamma.domain.entity.Poll
import kotlinx.parcelize.Parcelize
import io.pnut.gamma.domain.entity.raw.RawValue

@Parcelize
@JsonClass(generateAdapter = true)
data class PostPoll(
    @Json(name = "+io.pnut.core.poll") val value: ReplacementPollValue
) : RawValue, Parcelable {

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class ReplacementPollValue(
        @Json(name = "poll_token") val pollToken: String,
        val id: String
    ) : Parcelable

    companion object {
        fun createFromPoll(poll: Poll) = PostPoll(
            ReplacementPollValue(poll.pollToken, poll.id)
        )
    }
}
