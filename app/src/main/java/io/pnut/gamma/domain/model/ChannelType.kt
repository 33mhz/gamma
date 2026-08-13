package io.pnut.gamma.domain.model

enum class ChannelType(val value: String) {
    PM("io.pnut.core.pm"),
    Chat("io.pnut.core.chat"),
    PublicChat("io.pnut.core.chat"),
    ExploreConversations("io.pnut.core.chat"),
    ExploreNew("io.pnut.core.chat"),
    ExploreTopical("io.pnut.core.chat"),
    ExploreTrending("io.pnut.core.chat"),
    Yours("io.pnut.core.chat")
}