package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.SubscribeChannelInputData
import io.pnut.gamma.domain.model.io.SubscribeChannelOutputData
import io.pnut.gamma.domain.repository.IPnutRepository

class SubscribeChannelUseCase(private val pnutRepository: IPnutRepository) : AsyncUseCase<SubscribeChannelOutputData, SubscribeChannelInputData>() {
    override suspend fun run(params: SubscribeChannelInputData): SubscribeChannelOutputData {
        val res = if (params.isSubscribe) {
            pnutRepository.subscribe(params.channelId)
        } else {
            pnutRepository.unsubscribe(params.channelId)
        }
        return SubscribeChannelOutputData(res)
    }
}
