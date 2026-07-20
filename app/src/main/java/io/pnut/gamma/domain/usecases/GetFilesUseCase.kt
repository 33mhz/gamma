package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.io.GetFilesInputData
import io.pnut.gamma.domain.model.io.GetFilesOutputData
import io.pnut.gamma.domain.repository.IPnutRepository

class GetFilesUseCase(val pnutRepository: IPnutRepository) : AsyncUseCase<GetFilesOutputData, GetFilesInputData>() {
    override suspend fun run(params: GetFilesInputData): GetFilesOutputData {
        val filesRes = pnutRepository.getFiles(params.getFilesParam)
        return GetFilesOutputData(filesRes)
    }

}
