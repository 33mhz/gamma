package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.DeletePostInputData
import io.pnut.gamma.domain.model.io.DeletePostOutputData
import io.pnut.gamma.domain.repository.IPnutRepository

class DeletePostUseCase(private val pnutRepository: IPnutRepository) :
    UseCase<DeletePostOutputData, DeletePostInputData>() {
    override suspend fun run(params: DeletePostInputData): DeletePostOutputData {
        val res = pnutRepository.deletePost(params.postId)
        return DeletePostOutputData(res)
    }
}