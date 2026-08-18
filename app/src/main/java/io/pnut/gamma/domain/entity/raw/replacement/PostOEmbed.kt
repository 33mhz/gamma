package io.pnut.gamma.domain.entity.raw.replacement

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import io.pnut.gamma.domain.entity.raw.RawValue

@Parcelize
@JsonClass(generateAdapter = true)
data class PostOEmbed(
    @Json(name = "+io.pnut.core.file") val replacementFileValue: FileValue
) : RawValue, Parcelable {

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class FileValue(
        @Json(name = "file_id") val fileId: String,
        @Json(name = "file_token") val fileToken: String,
        val format: String = "oembed"
    ) : Parcelable
}
