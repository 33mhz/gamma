package io.pnut.gamma.domain.model.params.single

import io.pnut.gamma.util.toInt

data class MentionParam(
    val includeCopyMentions: Boolean = true
) : BaseParam {
    override fun toMap(): Map<String, String> = hashMapOf<String, String>().also { map ->
        map["include_copy_mentions"] = includeCopyMentions.toInt().toString()
    }
}
