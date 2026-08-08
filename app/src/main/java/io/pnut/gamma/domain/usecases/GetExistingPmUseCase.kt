package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.GetExistingPmInputData
import io.pnut.gamma.domain.model.io.GetExistingPmOutputData
import io.pnut.gamma.domain.repository.IPnutRepository
import javax.inject.Inject

class GetExistingPmUseCase @Inject constructor(private val pnutRepository: IPnutRepository) :
    AsyncUseCase<GetExistingPmOutputData, GetExistingPmInputData>() {
    override suspend fun run(params: GetExistingPmInputData): GetExistingPmOutputData {
        val res = pnutRepository.getExistingPm(params.ids)
        return GetExistingPmOutputData(res)
    }
}
