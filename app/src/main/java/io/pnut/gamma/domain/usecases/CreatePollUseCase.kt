package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.CreatePollInputData
import io.pnut.gamma.domain.model.io.CreatePollOutputData
import io.pnut.gamma.domain.repository.IPnutRepository

class CreatePollUseCase(private val pnutRepository: IPnutRepository) :
    UseCase<CreatePollOutputData, CreatePollInputData>() {
    override fun run(params: CreatePollInputData): CreatePollOutputData {
        val pollPostBody = params.pollPostBody
        val res = pnutRepository.createPoll(pollPostBody)
        return CreatePollOutputData(res.data)
    }
}