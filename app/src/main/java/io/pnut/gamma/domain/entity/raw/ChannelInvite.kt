package io.pnut.gamma.domain.entity.raw

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChannelInvite(
    @Json(name = "channel_id") val channelId: String,
    val name: String?,
) : RawValue, Parcelable {

    companion object {
        const val TYPE = "io.pnut.core.channel.invite"
        fun getChannelInvite(raw: Map<String, List<RawValue>>?): ChannelInvite? {
            return raw?.get(TYPE)?.firstOrNull() as? ChannelInvite
        }
    }
}
