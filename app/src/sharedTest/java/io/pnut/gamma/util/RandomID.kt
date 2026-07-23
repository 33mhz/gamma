package io.pnut.gamma.util

import java.util.*

object RandomID {
    val get
        get() = UUID.randomUUID().toString()
}