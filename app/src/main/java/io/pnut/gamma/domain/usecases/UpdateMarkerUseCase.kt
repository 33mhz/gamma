package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.entity.Marker
import io.pnut.gamma.domain.model.io.UpdateMarkerInputData
import io.pnut.gamma.domain.model.io.UpdateMarkerOutputData
import io.pnut.gamma.domain.repository.IPnutRepository

class UpdateMarkerUseCase(private val pnutRepository: IPnutRepository) :
    AsyncUseCase<UpdateMarkerOutputData, UpdateMarkerInputData>() {
    override suspend fun run(params: UpdateMarkerInputData): UpdateMarkerOutputData {
        val markers = listOf(Marker(params.messageId, "channel:${params.channelId}"))
        val response = pnutRepository.updateMarkers(markers)
        return UpdateMarkerOutputData(response)
    }
}
