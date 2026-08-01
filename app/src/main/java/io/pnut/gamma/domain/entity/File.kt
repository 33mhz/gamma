package io.pnut.gamma.domain.entity

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class File(
  @Json(name = "audio_info") val audioInfo: AudioInfo? = null,
  @Json(name = "created_at") val createdAt: Date,
  @Json(name = "file_token") val fileToken: String? = null,
  @Json(name = "file_token_read") val fileTokenRead: String? = null,
  val id: String,
  @Json(name = "image_info") val imageInfo: ImageInfo? = null,
  @Json(name = "is_complete") val isComplete: Boolean,
  @Json(name = "is_public") val isPublic: Boolean,
  val kind: FileKind,
  val url: String? = null,
  @Json(name = "url_expires_at") val linkExpiresAt: Date? = null,
  @Json(name = "url_short") val linkShort: String? = null,
  @Json(name = "mime_type") val mimeType: String? = null,
  val name: String,
  val sha256: String,
  val size: Int,
  val source: Client,
  val type: String,
  @Json(name = "upload_parameters") val uploadParameters: UploadParameters? = null,
  @Json(name = "derived_files") val derivativeFiles: DerivativeFiles? = null,
  val user: User? = null,
  @Json(name = "pagination_id") override val paginationId: String? = null
) : UniquePageable, Parcelable {
    @IgnoredOnParcel
    override val uniqueKey: String by lazy { id }

    @Parcelize
    data class UploadParameters(val method: String, val url: String) : Parcelable

    enum class FileKind {
        @Json(name = "audio")
        AUDIO, @Json(name = "image")
        IMAGE, @Json(name = "other")
        OTHER
    }

    @Parcelize
    data class ImageInfo(val height: Int, val width: Int) : Parcelable

    @Parcelize
    data class AudioInfo(
        val duration: Int,
        val bitrate: Int
    ) : Parcelable

    @Parcelize
    data class DerivedFile(
        @Json(name = "audio_info") val audioInfo: AudioInfo? = null,
        @Json(name = "image_info") val imageInfo: ImageInfo? = null,
        @Json(name = "mime_type") val mimeType: String,
        val name: String,
        val sha256: String,
        val size: Int,
        val url: String,
        @Json(name = "url_expires_at") val linkExpiresAt: Date,
    ) : Parcelable

    @Parcelize
    data class DerivativeFiles(
        @Json(name = "core_image_200s") val coreImage200s: DerivedFile? = null,
        @Json(name = "core_image_600s") val coreimage600s: DerivedFile? = null,
        @Json(name = "core_image_960r") val coreImage960r: DerivedFile? = null
    ) : Parcelable
}
