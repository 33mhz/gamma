package io.pnut.gamma.domain.entity

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Client(
  val id: String,
  val name: String,
  val url: String,
) : Parcelable