package io.pnut.gamma.domain.model.io

import androidx.lifecycle.MutableLiveData
import io.pnut.gamma.domain.entity.Token

data class GetAuthenticatedUserInputData(
    val liveData: MutableLiveData<Token>
)
