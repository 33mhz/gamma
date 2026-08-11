package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.Relationship
import io.pnut.gamma.domain.model.io.UpdateRelationshipInputData
import io.pnut.gamma.domain.model.io.UpdateRelationshipOutputData
import io.pnut.gamma.domain.repository.IPnutRepository

class UpdateRelationshipUseCase(val pnutRepository: IPnutRepository) :
    AsyncUseCase<UpdateRelationshipOutputData, UpdateRelationshipInputData>() {
    override suspend fun run(params: UpdateRelationshipInputData): UpdateRelationshipOutputData {
        val userId = params.userId
        val res = when (params.relationship) {
            Relationship.Follow -> pnutRepository.follow(userId)
            Relationship.UnFollow -> pnutRepository.unFollow(userId)
            Relationship.Block -> pnutRepository.block(userId)
            Relationship.UnBlock -> pnutRepository.unBlock(userId)
            Relationship.Mute -> pnutRepository.mute(userId)
            Relationship.UnMute -> pnutRepository.unMute(userId)
        }
        return UpdateRelationshipOutputData(res)
    }
}