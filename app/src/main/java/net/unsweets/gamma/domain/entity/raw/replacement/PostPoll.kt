package net.unsweets.gamma.domain.entity.raw.replacement

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize
import net.unsweets.gamma.domain.entity.Poll
import net.unsweets.gamma.domain.entity.raw.RawValue

@Parcelize
data class PostPoll(
    @Json(name = "+io.pnut.core.poll") val value: ReplacementPollValue
) : RawValue, Parcelable {

    @Parcelize
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
