package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.entity.Message
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.model.params.composed.GetChannelsParam
import io.pnut.gamma.domain.repository.IPnutRepository

class SearchMessagesUseCase(private val pnutRepository: IPnutRepository) :
    AsyncUseCase<SearchMessagesUseCase.OutputData, SearchMessagesUseCase.InputData>() {
    override suspend fun run(params: InputData): OutputData {
        val messages = pnutRepository.searchMessages(params.params)
        return OutputData(messages)
    }

    data class InputData(val params: GetChannelsParam)
    data class OutputData(val messages: PnutResponse<List<Message>>)
}
