package io.pnut.gamma.domain.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UriInfo(
    val uri: Uri
) : Parcelable
