package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.GetPollInputData
import io.pnut.gamma.domain.model.io.GetPollOutputData
import io.pnut.gamma.domain.repository.IPnutRepository

open class GetPollUseCase(private val pnutRepository: IPnutRepository) :
    AsyncUseCase<GetPollOutputData, GetPollInputData>() {
    override suspend fun run(params: GetPollInputData): GetPollOutputData {
        val (pollId, pollToken) = params
        val res = pnutRepository.getPoll(pollId, pollToken)
        return GetPollOutputData(res.data)
    }
}