package io.pnut.gamma.domain.entity.raw

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Broadcast(
    val id: String,
    val url: String
) : RawValue, Parcelable {
    companion object {
        const val TYPE = "net.patter-app.broadcast"
    }
}
