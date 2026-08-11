package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.MuteChannelInputData
import io.pnut.gamma.domain.model.io.MuteChannelOutputData
import io.pnut.gamma.domain.repository.IPnutRepository

class MuteChannelUseCase(private val pnutRepository: IPnutRepository) : AsyncUseCase<MuteChannelOutputData, MuteChannelInputData>() {
    override suspend fun run(params: MuteChannelInputData): MuteChannelOutputData {
        val res = if (params.isMute) {
            pnutRepository.muteChannel(params.channelId)
        } else {
            pnutRepository.unmuteChannel(params.channelId)
        }
        return MuteChannelOutputData(res)
    }
}
