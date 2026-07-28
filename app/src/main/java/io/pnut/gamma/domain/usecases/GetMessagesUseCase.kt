package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.GetMessagesInputData
import io.pnut.gamma.domain.model.io.GetMessagesOutputData
import io.pnut.gamma.domain.repository.IPnutRepository

class GetMessagesUseCase(private val pnutRepository: IPnutRepository) :
    AsyncUseCase<GetMessagesOutputData, GetMessagesInputData>() {
    override suspend fun run(params: GetMessagesInputData): GetMessagesOutputData {
        val messages = pnutRepository.getMessages(params.channelId, params.paginationParam)
        return GetMessagesOutputData(messages)
    }
}
