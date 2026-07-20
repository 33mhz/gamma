package io.pnut.gamma.domain.model.io

import io.pnut.gamma.domain.Relationship

data class UpdateRelationshipInputData(
    val userId: String,
    val relationship: Relationship
)
