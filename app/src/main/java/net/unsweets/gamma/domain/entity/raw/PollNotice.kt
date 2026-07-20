package net.unsweets.gamma.domain.entity.raw

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize
import net.unsweets.gamma.domain.entity.Poll
import net.unsweets.gamma.domain.entity.PollLikeValue
import java.util.Date

@Parcelize
data class PollNotice(
    override val prompt: String,
    @Json(name = "poll_token") override val pollToken: String,
    @Json(name = "closed_at") override val closedAt: Date,
    override val id: String,
    override val options: List<Poll.PollOption>
) : RawValue, Parcelable, PollLikeValue {

    companion object {
        const val TYPE = "io.pnut.core.poll-notice"
        fun findPollNotice(raw: Map<String, List<RawValue>>?): PollNotice? =
            raw?.get(TYPE)?.firstOrNull() as? PollNotice
    }
}
