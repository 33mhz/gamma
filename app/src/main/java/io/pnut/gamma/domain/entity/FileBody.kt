package io.pnut.gamma.domain.entity

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class FileBody(
  val kind: File.FileKind,
  val name: String,
  val isPublic: Boolean = true
) : Serializable {
    val type = "io.pnut.delta"
}