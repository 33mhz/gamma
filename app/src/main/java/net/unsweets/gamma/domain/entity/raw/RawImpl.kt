package net.unsweets.gamma.domain.entity.raw

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class RawImpl : Raw<Raw.RawValue>, Parcelable {
    @IgnoredOnParcel
    override val type = RawImpl.type
    @IgnoredOnParcel
    override val value: Raw.RawValue = object : Raw.RawValue {}

    companion object {
        const val type = ""
    }
}