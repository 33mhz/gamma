package io.pnut.gamma.domain.model.params.single

data class SearchUserParam(val keyword: String) : BaseParam {
    override fun toMap(): Map<String, String> {
        return hashMapOf(
            "q" to keyword
        )
    }
}