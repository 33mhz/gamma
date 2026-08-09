package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.entity.Channel
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.repository.IPnutRepository

class GetChannelUseCase(private val pnutRepository: IPnutRepository) :
    AsyncUseCase<PnutResponse<Channel>, String>() {
    override suspend fun run(params: String): PnutResponse<Channel> {
        return pnutRepository.getChannel(params)
    }
}
