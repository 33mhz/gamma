package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.DeleteMessageInputData
import io.pnut.gamma.domain.model.io.DeleteMessageOutputData
import io.pnut.gamma.domain.repository.IPnutRepository
import javax.inject.Inject

class DeleteMessageUseCase @Inject constructor(private val pnutRepository: IPnutRepository) :
    AsyncUseCase<DeleteMessageOutputData, DeleteMessageInputData>() {
    override suspend fun run(params: DeleteMessageInputData): DeleteMessageOutputData {
        val res = pnutRepository.deleteMessage(params.channelId, params.messageId)
        return DeleteMessageOutputData(res)
    }
}
