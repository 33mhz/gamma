package io.pnut.gamma.domain.repository

import android.net.Uri
import io.pnut.gamma.domain.entity.Channel
import io.pnut.gamma.domain.entity.File
import io.pnut.gamma.domain.entity.FileBody
import io.pnut.gamma.domain.entity.Interaction
import io.pnut.gamma.domain.entity.Marker
import io.pnut.gamma.domain.entity.Message
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.Poll
import io.pnut.gamma.domain.entity.PollPostBody
import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.domain.entity.PostBody
import io.pnut.gamma.domain.entity.ProfileBody
import io.pnut.gamma.domain.entity.Token
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.entity.VoteBody
import io.pnut.gamma.domain.model.params.composed.GetChannelsParam
import io.pnut.gamma.domain.model.params.composed.GetFilesParam
import io.pnut.gamma.domain.model.params.composed.GetInteractionsParam
import io.pnut.gamma.domain.model.params.composed.GetPostsParam
import io.pnut.gamma.domain.model.params.composed.GetUsersParam
import io.pnut.gamma.domain.model.params.single.PaginationParam
import okhttp3.RequestBody

interface IPnutRepository {
    // posts
    suspend fun getUnifiedStream(getPostsParam: GetPostsParam): PnutResponse<List<Post>>

    suspend fun getPersonalStream(getPostsParam: GetPostsParam): PnutResponse<List<Post>>
    suspend fun getMentionStream(getPostsParam: GetPostsParam): PnutResponse<List<Post>>
    suspend fun getStars(userId: String, getPostsParam: GetPostsParam): PnutResponse<List<Post>>
    suspend fun getUserPosts(userId: String, getPostsParam: GetPostsParam): PnutResponse<List<Post>>
    suspend fun getExplorePosts(slug: String, getPostsParam: GetPostsParam): PnutResponse<List<Post>>
    suspend fun getGlobal(getPostsParam: GetPostsParam): PnutResponse<List<Post>>
    suspend fun getTagStream(tag: String, getPostsParam: GetPostsParam): PnutResponse<List<Post>>
    suspend fun searchPosts(params: GetPostsParam): PnutResponse<List<Post>>
    suspend fun getThread(postId: String, params: GetPostsParam): PnutResponse<List<Post>>
    suspend fun getPosts(ids: io.pnut.gamma.domain.entity.IDs): PnutResponse<List<Post>>

    suspend fun createPost(postBody: PostBody): PnutResponse<Post>
    fun createPostSync(postBody: PostBody, token: String): PnutResponse<Post>
    suspend fun updatePost(postId: String, postBody: PostBody): PnutResponse<Post>
    fun deletePost(postId: String): PnutResponse<Post>

    fun reportPost(postId: String, reason: io.pnut.gamma.domain.entity.ReportReason): PnutResponse<Unit>

    fun createStarPostSync(postId: String): PnutResponse<Post>
    fun deleteStarPostSync(postId: String): PnutResponse<Post>

    fun createRepostSync(postId: String): PnutResponse<Post>
    fun deleteRepostSync(postId: String): PnutResponse<Post>


    // user
    suspend fun getUserProfile(userId: String): PnutResponse<User>
    suspend fun updateMyProfile(profileBody: ProfileBody): PnutResponse<User>
    suspend fun getFollowing(userId: String, getUsersParam: GetUsersParam): PnutResponse<List<User>>
    suspend fun getFollowers(userId: String, getUsersParam: GetUsersParam): PnutResponse<List<User>>
    suspend fun getBlockedUsers(getUsersParam: GetUsersParam): PnutResponse<List<User>>
    suspend fun getMutedUsers(getUsersParam: GetUsersParam): PnutResponse<List<User>>
    suspend fun searchUsers(getSearchUsersParam: GetUsersParam): PnutResponse<List<User>>
    suspend fun follow(userId: String): PnutResponse<User>
    suspend fun unFollow(userId: String): PnutResponse<User>
    suspend fun mute(userId: String): PnutResponse<User>
    suspend fun unMute(userId: String): PnutResponse<User>
    suspend fun block(userId: String): PnutResponse<User>
    suspend fun unBlock(userId: String): PnutResponse<User>
    suspend fun updateCover(uri: Uri): PnutResponse<User>
    suspend fun updateAvatar(uri: Uri): PnutResponse<User>
    suspend fun deleteCover(): PnutResponse<User>
    suspend fun deleteAvatar(): PnutResponse<User>

    // channel and messages
    suspend fun getChannel(channelId: String): PnutResponse<Channel>
    suspend fun getSubscribedChannels(getChannelsParam: GetChannelsParam): PnutResponse<List<Channel>>
    suspend fun getPmChannels(getChannelsParam: GetChannelsParam): PnutResponse<List<Channel>>
    suspend fun getTopicalChannels(getChannelsParam: GetChannelsParam): PnutResponse<List<Channel>>
    suspend fun getExploreChannels(slug: String, getChannelsParam: GetChannelsParam): PnutResponse<List<Channel>>
    suspend fun searchChannels(getChannelsParam: GetChannelsParam): PnutResponse<List<Channel>>
    suspend fun getMessages(channelId: String, paginationParam: PaginationParam): PnutResponse<List<Message>>
    suspend fun deleteMessage(channelId: String, messageId: String): PnutResponse<Message>
    suspend fun getMessageThread(channelId: String, messageId: String): PnutResponse<List<Message>>
    suspend fun createMessage(channelId: String, message: PostBody): PnutResponse<Message>
    suspend fun createPmMessage(message: io.pnut.gamma.domain.entity.PmPostBody): PnutResponse<Message>
    suspend fun getExistingPm(ids: io.pnut.gamma.domain.entity.IDs): PnutResponse<Channel>
    suspend fun subscribe(channelId: String): PnutResponse<Channel>
    suspend fun unsubscribe(channelId: String): PnutResponse<Channel>
    suspend fun muteChannel(channelId: String): PnutResponse<Channel>
    suspend fun unmuteChannel(channelId: String): PnutResponse<Channel>

    // others
    suspend fun getInteractions(getInteractionsParam: GetInteractionsParam): PnutResponse<List<Interaction>>

    suspend fun getFiles(getFilesParam: GetFilesParam): PnutResponse<List<File>>
    suspend fun getToken(): PnutResponse<Token>
    suspend fun verifyToken(token: String): PnutResponse<Token>
    fun createFile(content: RequestBody, fileBody: FileBody): PnutResponse<File>

    fun updateDefaultPnutService(token: String)

    fun createPoll(pollPostBody: PollPostBody): PnutResponse<Poll>
    suspend fun getPoll(pollId: String, pollToken: String): PnutResponse<Poll>
    suspend fun vote(pollId: String, pollToken: String, voteBody: VoteBody): PnutResponse<Poll>

    suspend fun updateMarkers(markers: List<Marker>): PnutResponse<List<Marker>>
}