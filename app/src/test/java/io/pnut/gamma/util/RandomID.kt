package io.pnut.gamma.util

import java.util.*

object RandomID {
    val getID
        get() = UUID.randomUUID().toString()
}