package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.Marker

data class UpdateMarkerOutputData(
    val response: PnutResponse<List<Marker>>
)
