package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.GetMessageThreadInputData
import io.pnut.gamma.domain.model.io.GetMessageThreadOutputData
import io.pnut.gamma.domain.repository.IPnutRepository

class GetMessageThreadUseCase(private val pnutRepository: IPnutRepository) :
    AsyncUseCase<GetMessageThreadOutputData, GetMessageThreadInputData>() {
    override suspend fun run(params: GetMessageThreadInputData): GetMessageThreadOutputData {
        val res = pnutRepository.getMessageThread(params.channelId, params.messageId)
        return GetMessageThreadOutputData(res)
    }
}
