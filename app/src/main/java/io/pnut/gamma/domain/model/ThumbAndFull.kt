package io.pnut.gamma.domain.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class ThumbAndFull(val thumb: String, val full: String) : Parcelable
