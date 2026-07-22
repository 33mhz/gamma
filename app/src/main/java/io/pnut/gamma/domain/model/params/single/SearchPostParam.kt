package io.pnut.gamma.domain.model.params.single

data class SearchPostParam(
    val keyword: String,
    val order: String = "id"
) : BaseParam {
    override fun toMap(): Map<String, String> {
        return hashMapOf(
            "q" to keyword,
            "order" to order
        )
    }
}
