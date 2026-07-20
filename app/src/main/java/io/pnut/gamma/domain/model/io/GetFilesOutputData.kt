package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.File
import io.pnut.gamma.domain.entity.PnutResponse

data class GetFilesOutputData(
    val res: PnutResponse<List<File>>
)
