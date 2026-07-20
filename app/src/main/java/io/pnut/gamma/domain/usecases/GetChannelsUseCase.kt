package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.GetChannelsInputData
import io.pnut.gamma.domain.model.io.GetChannelsOutputData
import io.pnut.gamma.domain.repository.IPnutRepository

class GetChannelsUseCase(private val pnutRepository: IPnutRepository) :
    AsyncUseCase<GetChannelsOutputData, GetChannelsInputData>() {
    override suspend fun run(params: GetChannelsInputData): GetChannelsOutputData {
        val channels = pnutRepository.getChannels(params.params)
        return GetChannelsOutputData(channels)
    }
}