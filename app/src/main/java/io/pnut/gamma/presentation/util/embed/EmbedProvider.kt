package io.pnut.gamma.presentation.util.embed

interface EmbedProvider {
    fun parse(url: String): Embed?
}
