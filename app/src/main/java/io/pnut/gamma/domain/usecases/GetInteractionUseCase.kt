package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.GetInteractionInputData
import io.pnut.gamma.domain.model.io.GetInteractionOutputData
import io.pnut.gamma.domain.repository.IPnutRepository

class GetInteractionUseCase(private val pnutRepository: IPnutRepository) :
    AsyncUseCase<GetInteractionOutputData, GetInteractionInputData>() {
    override suspend fun run(params: GetInteractionInputData): GetInteractionOutputData {
        val res = pnutRepository.getInteractions(params.getInteractionParam)
        return GetInteractionOutputData(res)
    }

}
