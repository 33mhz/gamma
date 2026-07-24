package io.pnut.gamma.sample

import io.pnut.gamma.domain.entity.Token

object Tokens {
  val token
    get() = Token(Clients.testClient, emptyList(), Users.me, Token.Storage(0, 0))
}