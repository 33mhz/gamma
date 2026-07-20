package io.pnut.gamma.domain.model.io

data class UpdateProfileInputData(
    val name: String,
    val description: String,
    val timezone: String,
    val locale: String
)
