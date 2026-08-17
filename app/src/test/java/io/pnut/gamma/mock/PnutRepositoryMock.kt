package io.pnut.gamma.mock

import android.net.Uri
import io.pnut.gamma.domain.entity.Channel
import io.pnut.gamma.domain.entity.ErrorResponse
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
import io.pnut.gamma.domain.entity.Unique
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.entity.VoteBody
import io.pnut.gamma.domain.model.params.composed.GetChannelsParam
import io.pnut.gamma.domain.model.params.composed.GetFilesParam
import io.pnut.gamma.domain.model.params.composed.GetInteractionsParam
import io.pnut.gamma.domain.model.params.composed.GetPostsParam
import io.pnut.gamma.domain.model.params.composed.GetUsersParam
import io.pnut.gamma.domain.model.params.single.PaginationParam
import io.pnut.gamma.domain.entity.entities.BaseContent
import io.pnut.gamma.sample.Clients
import io.pnut.gamma.domain.repository.IPnutRepository
import io.pnut.gamma.sample.Users
import io.pnut.gamma.util.ErrorCollections
import io.pnut.gamma.util.TestException
import okhttp3.RequestBody
import java.util.*

open class PnutRepositoryMock(private val pnutMockData: PnutMockData = PnutMockData()) :
    IPnutRepository {
    data class PnutMockData(
        val posts: List<Post> = emptyList(),
        val users: List<User> = emptyList(),
        val channels: List<Channel> = emptyList(),
        val messages: List<Message> = emptyList(),
        val polls: List<Poll> = emptyList(),
        val files: List<File> = emptyList()
    )

    private data class PnutMemoryDB(
        val posts: Map<String, Post> = emptyMap(),
        val users: Map<String, User> = emptyMap(),
        val channels: Map<String, Channel> = emptyMap(),
        val messages: Map<String, Message> = emptyMap(),
        val polls: Map<String, Poll> = emptyMap(),
        val files: Map<String, File> = emptyMap()
    )

    private fun <T : Unique> List<T>.toMap() = this.associateBy { it.uniqueKey }

    private val pnutMemoryDb by lazy {
        PnutMemoryDB(
            posts = pnutMockData.posts.toMap(),
            users = pnutMockData.users.toMap(),
            channels = pnutMockData.channels.toMap(),
            messages = pnutMockData.messages.toMap(),
            polls = pnutMockData.polls.toMap(),
            files = pnutMockData.files.toMap()
        )
    }

    private fun <T> success(code: Int = 200, data: () -> T): PnutResponse<T> {
        return PnutResponse(PnutResponse.Meta(code), data())
    }

    private fun <T> failure(code: Int = 400, data: () -> T): PnutResponse<T> {
        return PnutResponse(PnutResponse.Meta(code), data())
    }

    private val errorResponse = ErrorResponse(ErrorResponse.Meta(400, ""))

    override suspend fun deleteCover(): PnutResponse<User> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun deleteAvatar(): PnutResponse<User> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getUnifiedStream(getPostsParam: GetPostsParam): PnutResponse<List<Post>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getPersonalStream(getPostsParam: GetPostsParam): PnutResponse<List<Post>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getMentionStream(getPostsParam: GetPostsParam): PnutResponse<List<Post>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getStars(
        userId: String,
        getPostsParam: GetPostsParam
    ): PnutResponse<List<Post>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getUserPosts(
        userId: String,
        getPostsParam: GetPostsParam
    ): PnutResponse<List<Post>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getExplorePosts(
        slug: String,
        getPostsParam: GetPostsParam
    ): PnutResponse<List<Post>> {
        TODO("not implemented")
    }

    override suspend fun getGlobal(getPostsParam: GetPostsParam): PnutResponse<List<Post>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getTagStream(
        tag: String,
        getPostsParam: GetPostsParam
    ): PnutResponse<List<Post>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun searchPosts(params: GetPostsParam): PnutResponse<List<Post>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getThread(
        postId: String,
        params: GetPostsParam
    ): PnutResponse<List<Post>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getPosts(ids: io.pnut.gamma.domain.entity.IDs): PnutResponse<List<Post>> {
        return success {
            ids.ids.mapNotNull { pnutMemoryDb.posts[it] }
        }
    }

    override suspend fun createPost(postBody: PostBody): PnutResponse<Post> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createPostSync(postBody: PostBody, token: String): PnutResponse<Post> {
        return when {
            postBody.text.isEmpty() -> throw TestException()
            token.isEmpty() -> throw TestException()
            else -> success {
                Post(
                    createdAt = Date(),
                    id = "1",
                    source = Clients.testClient,
                    threadId = "1",
                    counts = Post.PostCount(0, 0, 0, 0),
                    content = BaseContent(text = postBody.text)
                )
            }
        }
    }

    override suspend fun updatePost(postId: String, postBody: PostBody): PnutResponse<Post> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deletePost(postId: String): PnutResponse<Post> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun reportPost(postId: String, reason: io.pnut.gamma.domain.entity.ReportReason): PnutResponse<Unit> {
        TODO("not implemented")
    }

    override fun createStarPostSync(postId: String): PnutResponse<Post> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteStarPostSync(postId: String): PnutResponse<Post> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createRepostSync(postId: String): PnutResponse<Post> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteRepostSync(postId: String): PnutResponse<Post> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getUserProfile(userId: String): PnutResponse<User> {
        return when {
            userId.isEmpty() -> throw ErrorCollections.CommunicationError(errorResponse)
            else -> success { pnutMemoryDb.users.getValue(userId) }
        }
    }

    override suspend fun updateMyProfile(profileBody: ProfileBody): PnutResponse<User> {
        // TODO: ???
        val user = Users.me
        val content = user.content.copy(text = profileBody.content?.text)
        return success {
            user.copy(
                name = profileBody.name,
                locale = profileBody.locale.orEmpty(),
                timezone = profileBody.timezone.orEmpty(),
                content = content
            )
        }

    }

    override suspend fun getFollowing(
        userId: String,
        getUsersParam: GetUsersParam
    ): PnutResponse<List<User>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getFollowers(
        userId: String,
        getUsersParam: GetUsersParam
    ): PnutResponse<List<User>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getBlockedUsers(getUsersParam: GetUsersParam): PnutResponse<List<User>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getMutedUsers(getUsersParam: GetUsersParam): PnutResponse<List<User>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getSuggestedUsers(getUsersParam: GetUsersParam): PnutResponse<List<User>> {
        TODO("not implemented")
    }

    override suspend fun searchUsers(getSearchUsersParam: GetUsersParam): PnutResponse<List<User>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun follow(userId: String): PnutResponse<User> {
        return success {
            val user = pnutMemoryDb.users.getValue(userId)
            if (user.youFollow) throw TestException()
            user.copy(youFollow = true)
        }
    }

    override suspend fun unFollow(userId: String): PnutResponse<User> {
        return success {
            val user = pnutMemoryDb.users.getValue(userId)
            if (!user.youFollow) throw TestException()
            user.copy(youFollow = false)
        }
    }

    override suspend fun mute(userId: String): PnutResponse<User> {
        return success {
            val user = pnutMemoryDb.users.getValue(userId)
            if (user.youMuted) throw TestException()
            user.copy(youMuted = true)
        }
    }

    override suspend fun unMute(userId: String): PnutResponse<User> {
        return success {
            val user = pnutMemoryDb.users.getValue(userId)
            if (!user.youMuted) throw TestException()
            user.copy(youMuted = false)
        }
    }

    override suspend fun block(userId: String): PnutResponse<User> {
        return success {
            val user = pnutMemoryDb.users.getValue(userId)
            if (user.youBlocked) throw TestException()
            user.copy(youBlocked = true, youFollow = false)
        }
    }

    override suspend fun unBlock(userId: String): PnutResponse<User> {
        return success {
            val user = pnutMemoryDb.users.getValue(userId)
            if (!user.youBlocked) throw TestException()
            user.copy(youBlocked = false, youFollow = false)
        }
    }

    override suspend fun updateCover(uri: Uri): PnutResponse<User> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun updateAvatar(uri: Uri): PnutResponse<User> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getChannel(channelId: String): PnutResponse<Channel> {
        TODO("not implemented")
    }

    override suspend fun getSubscribedChannels(getChannelsParam: GetChannelsParam): PnutResponse<List<Channel>> {
        TODO("not implemented")
    }

    override suspend fun getPmChannels(getChannelsParam: GetChannelsParam): PnutResponse<List<Channel>> {
        TODO("not implemented")
    }

    override suspend fun getTopicalChannels(getChannelsParam: GetChannelsParam): PnutResponse<List<Channel>> {
        TODO("not implemented")
    }

    override suspend fun getExploreChannels(
        slug: String,
        getChannelsParam: GetChannelsParam
    ): PnutResponse<List<Channel>> {
        TODO("not implemented")
    }

    override suspend fun searchChannels(getChannelsParam: GetChannelsParam): PnutResponse<List<Channel>> {
        TODO("not implemented")
    }

    override suspend fun searchMessages(getChannelsParam: GetChannelsParam): PnutResponse<List<Message>> {
        TODO("not implemented")
    }

    override suspend fun getMessages(
        channelId: String,
        paginationParam: PaginationParam
    ): PnutResponse<List<Message>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun deleteMessage(channelId: String, messageId: String): PnutResponse<Message> {
        TODO("not implemented")
    }

    override suspend fun getMessageThread(channelId: String, messageId: String): PnutResponse<List<Message>> {
        TODO("not implemented")
    }

    override suspend fun createMessage(channelId: String, message: PostBody): PnutResponse<Message> {
        TODO("not implemented")
    }

    override suspend fun createPmMessage(message: io.pnut.gamma.domain.entity.PmPostBody): PnutResponse<Message> {
        TODO("not implemented")
    }

    override suspend fun getExistingPm(ids: io.pnut.gamma.domain.entity.IDs): PnutResponse<Channel> {
        TODO("not implemented")
    }

    override suspend fun subscribe(channelId: String): PnutResponse<Channel> {
        TODO("not implemented")
    }

    override suspend fun unsubscribe(channelId: String): PnutResponse<Channel> {
        TODO("not implemented")
    }

    override suspend fun muteChannel(channelId: String): PnutResponse<Channel> {
        TODO("not implemented")
    }

    override suspend fun unmuteChannel(channelId: String): PnutResponse<Channel> {
        TODO("not implemented")
    }

    override suspend fun getInteractions(getInteractionsParam: GetInteractionsParam): PnutResponse<List<Interaction>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getFiles(getFilesParam: GetFilesParam): PnutResponse<List<File>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getToken(): PnutResponse<Token> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun verifyToken(token: String): PnutResponse<Token> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createFile(content: RequestBody, fileBody: FileBody): PnutResponse<File> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun updateDefaultPnutService(token: String) {
        // TODO: service mock?
    }

    override fun createPoll(pollPostBody: PollPostBody): PnutResponse<Poll> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getPoll(pollId: String, pollToken: String): PnutResponse<Poll> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun vote(
        pollId: String,
        pollToken: String,
        voteBody: VoteBody
    ): PnutResponse<Poll> {
        val poll = pnutMemoryDb.polls.getValue(pollId)
        val newPoll = voteBody.positions.fold(poll) { accPoll, position ->
            val indexedValue = accPoll.options.withIndex().find { it.value.position == position }
                ?: return@fold accPoll
            val newOptionValue = indexedValue.value.copy(isYourResponse = true)
            val newOptions = accPoll.options.toMutableList().also {
                it[indexedValue.index] = newOptionValue
            }
            accPoll.copy(options = newOptions)
        }
        return success { newPoll }
    }

    override suspend fun updateMarkers(markers: List<Marker>): PnutResponse<List<Marker>> {
        TODO("not implemented")
    }
}