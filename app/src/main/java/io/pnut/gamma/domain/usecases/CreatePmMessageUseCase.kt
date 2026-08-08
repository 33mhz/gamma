package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.CreatePmMessageInputData
import io.pnut.gamma.domain.model.io.CreateMessageOutputData
import io.pnut.gamma.domain.repository.IPnutRepository
import javax.inject.Inject

class CreatePmMessageUseCase @Inject constructor(private val pnutRepository: IPnutRepository) :
    AsyncUseCase<CreateMessageOutputData, CreatePmMessageInputData>() {
    override suspend fun run(params: CreatePmMessageInputData): CreateMessageOutputData {
        val res = pnutRepository.createPmMessage(params.messageBody)
        return CreateMessageOutputData(res)
    }
}
