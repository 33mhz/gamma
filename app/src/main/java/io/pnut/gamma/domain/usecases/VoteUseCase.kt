package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.entity.VoteBody
import io.pnut.gamma.domain.model.io.VoteInputData
import io.pnut.gamma.domain.model.io.VoteOutputData
import io.pnut.gamma.domain.repository.IPnutRepository

open class VoteUseCase(private val pnutRepository: IPnutRepository) :
    AsyncUseCase<VoteOutputData, VoteInputData>() {
    override suspend fun run(params: VoteInputData): VoteOutputData {
        val voteBody =
            VoteBody(params.positions.toList().map { it + 1 })
        val res = pnutRepository.vote(params.pollId, params.pollToken, voteBody)
        return VoteOutputData(res.data)
    }
}
