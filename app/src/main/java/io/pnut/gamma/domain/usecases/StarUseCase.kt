package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.StarInputData
import io.pnut.gamma.domain.model.io.StarOutputData
import io.pnut.gamma.domain.repository.IPnutRepository

class StarUseCase(val pnutRepository: IPnutRepository) : UseCase<StarOutputData, StarInputData>() {
    override suspend fun run(params: StarInputData): StarOutputData {
        val res = when (params.newState) {
            true -> pnutRepository.createStarPostSync(params.postId, params.note)
            false -> pnutRepository.deleteStarPostSync(params.postId)
        }
        return StarOutputData(res)
    }

}
