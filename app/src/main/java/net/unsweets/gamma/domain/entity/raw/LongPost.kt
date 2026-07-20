package net.unsweets.gamma.domain.entity.raw

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import net.unsweets.gamma.util.MicroTimestamp

@Parcelize
data class LongPost(
    val body: String,
    val title: String?,
    @MicroTimestamp val tstamp: Long = 0L
) : RawValue, Parcelable {

    companion object {
        fun findLongPost(raw: Map<String, List<RawValue>>?): LongPost? =
            raw?.get(TYPE)?.firstOrNull() as? LongPost
        const val TYPE = "nl.chimpnut.blog.post"
    }
}
