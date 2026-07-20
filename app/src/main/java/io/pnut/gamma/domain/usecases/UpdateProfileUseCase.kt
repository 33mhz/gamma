package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.entity.ProfileBody
import io.pnut.gamma.domain.model.io.UpdateProfileInputData
import io.pnut.gamma.domain.model.io.UpdateProfileOutputData
import io.pnut.gamma.domain.repository.IPnutRepository

class UpdateProfileUseCase(val pnutRepository: IPnutRepository) :
    AsyncUseCase<UpdateProfileOutputData, UpdateProfileInputData>() {
    override suspend fun run(params: UpdateProfileInputData): UpdateProfileOutputData {
        val profileBody =
            ProfileBody(
                params.name,
                ProfileBody.Content(params.description),
                params.timezone,
                params.locale
            )
        val user = pnutRepository.updateMyProfile(profileBody)
        return UpdateProfileOutputData(user.data)
    }

}
