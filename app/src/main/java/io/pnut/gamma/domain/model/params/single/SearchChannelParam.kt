package io.pnut.gamma.domain.model.params.single

data class SearchChannelParam(
    val keyword: String,
    val order: String? = null,
    val channelIds: String? = null,
) : BaseParam {
    override fun toMap(): Map<String, String> = hashMapOf<String, String>().also { map ->
        map["q"] = keyword
        if (order != null) map["order"] = order
        if (channelIds != null) map["channel_ids"] = channelIds
    }
}
