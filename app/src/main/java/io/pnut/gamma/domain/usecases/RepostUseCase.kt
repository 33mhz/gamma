package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.RepostInputData
import io.pnut.gamma.domain.model.io.RepostOutputData
import io.pnut.gamma.domain.repository.IPnutRepository

class RepostUseCase(private val pnutRepository: IPnutRepository) :
    UseCase<RepostOutputData, RepostInputData>() {
    override suspend fun run(params: RepostInputData): RepostOutputData {
        val res = when (params.newState) {
            true -> pnutRepository.createRepostSync(params.postId)
            false -> pnutRepository.deleteRepostSync(params.postId)
        }
        return RepostOutputData(res)
    }

}
