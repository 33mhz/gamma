package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.CreateMessageInputData
import io.pnut.gamma.domain.model.io.CreateMessageOutputData
import io.pnut.gamma.domain.repository.IPnutRepository
import javax.inject.Inject

class CreateMessageUseCase @Inject constructor(private val pnutRepository: IPnutRepository) :
    AsyncUseCase<CreateMessageOutputData, CreateMessageInputData>() {
    override suspend fun run(params: CreateMessageInputData): CreateMessageOutputData {
        val res = pnutRepository.createMessage(params.channelId, params.messageBody)
        return CreateMessageOutputData(res)
    }
}
