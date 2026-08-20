package io.pnut.gamma.util

import android.content.Context
import io.pnut.gamma.domain.entity.ErrorResponse
import io.pnut.gamma.R
import retrofit2.HttpException

sealed class ErrorCollections(val displayErrorMessageRes: Int) : Exception() {
    open fun getErrorMessage(context: Context): String = context.getString(displayErrorMessageRes)

    class CannotLoadFile : ErrorCollections(R.string.cannot_load_file)
    class AccountNotFound : ErrorCollections(R.string.account_not_found)
    data class CommunicationError(val errorResponse: ErrorResponse) :
        ErrorCollections(R.string.communication_error) {
        companion object {
            fun create(json: String): CommunicationError {
                val response = try {
                    if (json.isNotBlank()) {
                        MoshiSingleton.moshi.adapter(ErrorResponse::class.java).fromJson(json)
                    } else {
                        null
                    }
                } catch (_: Exception) {
                    null
                }

                return CommunicationError(
                    response ?: ErrorResponse(ErrorResponse.Meta(0, Constants.UNKNOWN_ERROR))
                )
            }
        }

        fun getMessage(context: Context?): String {
            return errorResponse.meta.getResourceMessage(context)
        }

        override fun getErrorMessage(context: Context): String {
            return getMessage(context)
        }
    }

    companion object {
        fun getErrorMessage(context: Context, throwable: Throwable): String {
            return when (throwable) {
                is ErrorCollections -> throwable.getErrorMessage(context)
                is HttpException -> {
                    val errorBody = throwable.response()?.errorBody()?.string()
                    if (!errorBody.isNullOrBlank()) {
                        CommunicationError.create(errorBody).getErrorMessage(context)
                    } else {
                        throwable.localizedMessage ?: context.getString(R.string.communication_error)
                    }
                }
                else -> throwable.localizedMessage ?: context.getString(R.string.communication_error)
            }
        }
    }
}