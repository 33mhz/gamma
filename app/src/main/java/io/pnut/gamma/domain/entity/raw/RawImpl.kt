package io.pnut.gamma.domain.entity.raw

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class RawImpl(val unused: String? = null) : RawValue, Parcelable
