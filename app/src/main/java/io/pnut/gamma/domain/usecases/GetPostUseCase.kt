package io.pnut.gamma.domain.usecases

import io.pnut.gamma.domain.model.StreamType
import io.pnut.gamma.domain.model.io.GetPostInputData
import io.pnut.gamma.domain.model.io.GetPostOutputData
import io.pnut.gamma.domain.model.params.composed.GetPostsParam
import io.pnut.gamma.domain.model.params.single.GeneralPostParam
import io.pnut.gamma.domain.model.params.single.MentionParam
import io.pnut.gamma.domain.model.params.single.PaginationParam
import io.pnut.gamma.domain.model.params.single.SearchPostParam
import io.pnut.gamma.domain.repository.IPnutRepository
import io.pnut.gamma.domain.repository.IPreferenceRepository

open class GetPostUseCase(
    private val pnutRepository: IPnutRepository,
    private val preferenceRepository: IPreferenceRepository
) :
    AsyncUseCase<GetPostOutputData, GetPostInputData>() {
    override suspend fun run(params: GetPostInputData): GetPostOutputData {
        val streamType = params.streamType
        val baseParam = params.params
        val param =
            GetPostsParam(baseParam.toMap()).also {
                it.add(PaginationParam(count = preferenceRepository.loadingSize))
                if (streamType is StreamType.Mentions && preferenceRepository.hideCopyMentions) {
                    it.add(MentionParam(includeCopyMentions = false))
                }
            }
        val res = when (streamType) {
            is StreamType.Home -> {
                when (preferenceRepository.unifiedStream) {
                    true -> pnutRepository.getUnifiedStream(param)
                    else -> pnutRepository.getPersonalStream(param)
                }
            }
            is StreamType.Mentions -> pnutRepository.getMentionStream(param)
            is StreamType.Stars -> pnutRepository.getStars(streamType.userId, param)
            is StreamType.Tag -> pnutRepository.getTagStream(streamType.tag, param)
            is StreamType.User -> pnutRepository.getUserPosts(streamType.userId, param)
            is StreamType.Thread -> pnutRepository.getThread(streamType.postId, param.apply {
                add(
                    GeneralPostParam(
                        includeDeleted = true,
                        includePostRaw = true,
                        includeBookmarkedBy = true,
                        includeRepostedBy = true
                    )
                )
            })
            is StreamType.Explore -> {
                if (streamType is StreamType.Explore.Global) {
                    pnutRepository.getGlobal(param)
                } else {
                    pnutRepository.getExplorePosts(streamType.slug, param)
                }
            }
            is StreamType.Search -> pnutRepository.searchPosts(params.params.apply {
                add(SearchPostParam(streamType.keyword))
            })
            is StreamType.Posts -> pnutRepository.getPosts(io.pnut.gamma.domain.entity.IDs(streamType.ids))
            is StreamType.Revisions -> pnutRepository.getRevisions(streamType.postId, param)
        }
        return GetPostOutputData(res)
    }

}