package io.pnut.gamma.util

object Constants {
    const val GAMMA = "Gamma"
    const val MAX_POST_TEXT_LENGTH = 256
    const val UNKNOWN_ERROR = "Unknown error"
    val unknownErrorException = Exception(UNKNOWN_ERROR)
    const val API_BASE_URL = "https://api.pnut.io/v1/"
    const val PLAY_STORE_URL: String = "market://details?id=io.pnut.gamma"
}