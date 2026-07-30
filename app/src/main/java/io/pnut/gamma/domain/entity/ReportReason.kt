package io.pnut.gamma.domain.entity

import com.squareup.moshi.Json

enum class ReportReason(val value: String) {
    @Json(name = "soliciting")
    Soliciting("soliciting"),
    @Json(name = "account_type")
    AccountType("account_type"),
    @Json(name = "nsfw")
    Nsfw("nsfw"),
    @Json(name = "user_abuse")
    UserAbuse("user_abuse")
}
