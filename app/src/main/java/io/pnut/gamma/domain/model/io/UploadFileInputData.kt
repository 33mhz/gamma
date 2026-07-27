package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.model.UriInfo
import java.io.InputStream

data class UploadFileInputData(
    val uriInfo: UriInfo,
    val inputStream: InputStream?,
    val fileName: String? = null
)
