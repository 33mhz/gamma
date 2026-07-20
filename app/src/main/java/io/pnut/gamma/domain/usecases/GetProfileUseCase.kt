package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.GetProfileInputData
import io.pnut.gamma.domain.model.io.GetProfileOutputData
import io.pnut.gamma.domain.repository.IPnutRepository

class GetProfileUseCase(val pnutRepository: IPnutRepository) :
    AsyncUseCase<GetProfileOutputData, GetProfileInputData>() {
    override suspend fun run(params: GetProfileInputData): GetProfileOutputData {
        val user = pnutRepository.getUserProfile(params.userId)
        return GetProfileOutputData(user)
    }
}
