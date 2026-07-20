package io.pnut.gamma.util

import android.content.Context
import io.pnut.gamma.domain.entity.ErrorResponse
import io.pnut.gamma.R

sealed class ErrorCollections(val displayErrorMessageRes: Int) : Exception() {
    object CannotLoadFile : ErrorCollections(R.string.cannot_load_file)
    object AccountNotFound : ErrorCollections(R.string.account_not_found)
    object CannotOpenUrl : ErrorCollections(R.string.cannot_open_url)
    data class CommunicationError(val errorResponse: ErrorResponse) :
        ErrorCollections(R.string.communication_error) {
        companion object {
            fun create(json: String): CommunicationError {
                return MoshiSingleton.moshi.adapter(ErrorResponse::class.java).fromJson(json)?.let {
                    CommunicationError(
                        it
                    )
                }
                    ?: throw Constants.unknownErrorException
            }
        }

        fun getMessage(context: Context?): String {
            return errorResponse.meta.getResourceMessage(context)
        }
    }
}