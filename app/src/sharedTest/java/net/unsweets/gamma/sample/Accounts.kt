package io.pnut.gamma.sample

import io.pnut.gamma.domain.model.Account
import io.pnut.gamma.util.RandomID

object Accounts {
  val account
    get() = Account(RandomID.get, "token", "screenName", "name")

}