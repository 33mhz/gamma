package io.pnut.gamma.domain.entity.raw

import android.os.Parcelable
import io.pnut.gamma.util.MicroTimestamp
import kotlinx.parcelize.Parcelize

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
