package net.unsweets.gamma.domain.entity.raw

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Spoiler(
    val topic: String,
    @Json(name = "expired_at") val expiredAt: Date?
) : RawValue, Parcelable {
    companion object {
        const val TYPE = "shawn.spoiler"
        fun getSpoiler(raw: Map<String, List<RawValue>>?): Spoiler? {
            return raw?.get(TYPE)?.firstOrNull() as? Spoiler
        }
    }
}
