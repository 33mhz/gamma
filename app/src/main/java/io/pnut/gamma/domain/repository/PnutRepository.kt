package io.pnut.gamma.domain.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import io.pnut.gamma.data.PnutService
import io.pnut.gamma.domain.entity.Channel
import io.pnut.gamma.domain.entity.FileBody
import io.pnut.gamma.domain.entity.IDs
import io.pnut.gamma.domain.entity.Interaction
import io.pnut.gamma.domain.entity.Marker
import io.pnut.gamma.domain.entity.Message
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.Poll
import io.pnut.gamma.domain.entity.PollPostBody
import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.domain.entity.StarBody
import io.pnut.gamma.domain.entity.PostBody
import io.pnut.gamma.domain.entity.PmPostBody
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
import io.pnut.gamma.util.Constants
import io.pnut.gamma.util.MoshiSingleton
import io.pnut.gamma.BuildConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.URLConnection
import java.util.Locale

class PnutRepository(private val context: Context, defaultAccountToken: String? = null) :
    IPnutRepository {
    override suspend fun deleteCover(): PnutResponse<User> {
        return defaultPnutService.deleteCover()
    }

    override suspend fun deleteAvatar(): PnutResponse<User> {
        return defaultPnutService.deleteAvatar()
    }

    override suspend fun searchUsers(getSearchUsersParam: GetUsersParam): PnutResponse<List<User>> {
        return defaultPnutService.searchUsers(getSearchUsersParam.toMap())
    }

    override suspend fun updateCover(uri: Uri): PnutResponse<User> {
        return defaultPnutService.updateCover(createUserImageRequestBody(uri, UserImageKey.Cover))
    }

    override suspend fun updateAvatar(uri: Uri): PnutResponse<User> {
        return defaultPnutService.updateAvatar(createUserImageRequestBody(uri, UserImageKey.Avatar))
    }

    private enum class UserImageKey { Avatar, Cover }

    private fun createUserImageRequestBody(uri: Uri, key: UserImageKey): MultipartBody.Part {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: URLConnection.guessContentTypeFromName(uri.toString())
        val fileName = getFileName(uri) ?: "image"

        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("Could not open input stream for URI: $uri")

        val content = bytes.toRequestBody(mimeType?.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(
            key.name.lowercase(Locale.ROOT),
            fileName,
            content
        )
    }

    private fun getFileName(uri: Uri): String? {
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        return cursor.getString(index)
                    }
                }
            }
        }
        return uri.lastPathSegment
    }

    override suspend fun createFile(content: RequestBody, fileBody: FileBody): PnutResponse<io.pnut.gamma.domain.entity.File> {
        return defaultPnutService.createFile(
            MultipartBody.Part.createFormData("content", fileBody.name, content),
            fileBody.name.toRequestBody("text/plain".toMediaTypeOrNull()),
            fileBody.kind.name.lowercase(Locale.ROOT).toRequestBody("text/plain".toMediaTypeOrNull()),
            fileBody.type.toRequestBody("text/plain".toMediaTypeOrNull()),
            "0".toRequestBody("text/plain".toMediaTypeOrNull())
        )
    }

    override suspend fun getThread(
        postId: String,
        params: GetPostsParam
    ): PnutResponse<List<Post>> {
        return defaultPnutService.getThread(postId, params.toMap())
    }

    override suspend fun getPosts(ids: IDs): PnutResponse<List<Post>> {
        val params = GetPostsParam(mapOf("ids" to ids.toString()))
        params.add(io.pnut.gamma.domain.model.params.single.GeneralPostParam())
        return defaultPnutService.getPosts(params.toMap())
    }

    override suspend fun createRepostSync(postId: String): PnutResponse<Post> {
        return defaultPnutService.createRepost(postId)
    }

    override suspend fun deleteRepostSync(postId: String): PnutResponse<Post> {
        return defaultPnutService.deleteRepost(postId)
    }

    override suspend fun createStarPostSync(postId: String, note: String?): PnutResponse<Post> {
        return defaultPnutService.createStar(postId, StarBody(note))
    }

    override suspend fun deleteStarPostSync(postId: String): PnutResponse<Post> {
        return defaultPnutService.deleteStar(postId)
    }

    override suspend fun getToken(): PnutResponse<Token> {
        return defaultPnutService.token()
    }

    override suspend fun searchPosts(params: GetPostsParam): PnutResponse<List<Post>> {
        return defaultPnutService.searchPosts(params.toMap())
    }

    override suspend fun getTagStream(
        tag: String,
        getPostsParam: GetPostsParam
    ): PnutResponse<List<Post>> {
        return defaultPnutService.getTaggedPosts(tag, getPostsParam.toMap())
    }

    override suspend fun verifyToken(token: String): PnutResponse<Token> {
        return createPnutService(token).token()
    }

    override suspend fun getFollowing(
        userId: String,
        getUsersParam: GetUsersParam
    ): PnutResponse<List<User>> {
        return defaultPnutService.getFollowing(userId, getUsersParam.toMap())
    }

    override suspend fun getBlockedUsers(getUsersParam: GetUsersParam): PnutResponse<List<User>> {
        return defaultPnutService.getBlockedUsers(getUsersParam.toMap())
    }

    override suspend fun getMutedUsers(getUsersParam: GetUsersParam): PnutResponse<List<User>> {
        return defaultPnutService.getMutedUsers(getUsersParam.toMap())
    }

    override suspend fun getSuggestedUsers(getUsersParam: GetUsersParam): PnutResponse<List<User>> {
        return defaultPnutService.getSuggestedUsers(getUsersParam.toMap())
    }

    override suspend fun getUnifiedStream(getPostsParam: GetPostsParam): PnutResponse<List<Post>> {
        return defaultPnutService.getUnifiedStream(getPostsParam.toMap())

    }

    override suspend fun getPersonalStream(getPostsParam: GetPostsParam): PnutResponse<List<Post>> {
        return defaultPnutService.getPersonalStream(getPostsParam.toMap())
    }

    override suspend fun getMentionStream(getPostsParam: GetPostsParam): PnutResponse<List<Post>> {
        return defaultPnutService.getMentions(getPostsParam.toMap())
    }

    override suspend fun getStars(
        userId: String,
        getPostsParam: GetPostsParam
    ): PnutResponse<List<Post>> {
        return defaultPnutService.getStars(userId, getPostsParam.toMap())
    }

    override suspend fun getUserPosts(
        userId: String,
        getPostsParam: GetPostsParam
    ): PnutResponse<List<Post>> {
        return defaultPnutService.getUserPosts(userId, getPostsParam.toMap())
    }

    override suspend fun getExplorePosts(
        slug: String,
        getPostsParam: GetPostsParam
    ): PnutResponse<List<Post>> {
        return defaultPnutService.getExplore(slug, getPostsParam.toMap())
    }

    override suspend fun getGlobal(getPostsParam: GetPostsParam): PnutResponse<List<Post>> {
        return defaultPnutService.getGlobal(getPostsParam.toMap())
    }

    override suspend fun createPost(postBody: PostBody): PnutResponse<Post> {
        return defaultPnutService.createPost(postBody)
    }

    override suspend fun createPostSync(postBody: PostBody, token: String): PnutResponse<Post> {
        return createPnutService(token).createPost(postBody)
    }

    override suspend fun updatePost(postId: String, postBody: PostBody): PnutResponse<Post> {
        return defaultPnutService.editPost(postId, postBody)
    }

    override suspend fun deletePost(postId: String): PnutResponse<Post> {
        return defaultPnutService.deletePost(postId)
    }

    override suspend fun reportPost(postId: String, reason: io.pnut.gamma.domain.entity.ReportReason): PnutResponse<Unit> {
        defaultPnutService.reportPost(postId, reason.value)
        return PnutResponse(PnutResponse.Meta(200), Unit)
    }

    override suspend fun getUserProfile(userId: String): PnutResponse<User> {
        return defaultPnutService.getUser(userId)
    }

    override suspend fun updateMyProfile(profileBody: ProfileBody): PnutResponse<User> {
        return defaultPnutService.putMyProfile(profileBody)
    }

    override suspend fun getFollowers(
        userId: String,
        getUsersParam: GetUsersParam
    ): PnutResponse<List<User>> {
        return defaultPnutService.getFollowers(userId, getUsersParam.toMap())
    }

    override suspend fun follow(userId: String): PnutResponse<User> {
        return defaultPnutService.follow(userId)
    }

    override suspend fun unFollow(userId: String): PnutResponse<User> {
        return defaultPnutService.unFollow(userId)
    }

    override suspend fun mute(userId: String): PnutResponse<User> {
        return defaultPnutService.mute(userId)
    }

    override suspend fun unMute(userId: String): PnutResponse<User> {
        return defaultPnutService.unMute(userId)
    }

    override suspend fun block(userId: String): PnutResponse<User> {
        return defaultPnutService.block(userId)
    }

    override suspend fun unBlock(userId: String): PnutResponse<User> {
        return defaultPnutService.unBlock(userId)
    }

    override suspend fun getChannel(channelId: String): PnutResponse<Channel> {
        return defaultPnutService.getChannel(channelId)
    }

    override suspend fun getSubscribers(
        channelId: String,
        getUsersParam: GetUsersParam
    ): PnutResponse<List<User>> {
        return defaultPnutService.getSubscribers(channelId, getUsersParam.toMap())
    }

    override suspend fun getSubscribedChannels(getChannelsParam: GetChannelsParam): PnutResponse<List<Channel>> {
        return defaultPnutService.getSubscribedChannels(getChannelsParam.toMap())
    }

    override suspend fun getPmChannels(getChannelsParam: GetChannelsParam): PnutResponse<List<Channel>> {
        return defaultPnutService.getPmChannels(getChannelsParam.toMap())
    }

    override suspend fun getTopicalChannels(getChannelsParam: GetChannelsParam): PnutResponse<List<Channel>> {
        return defaultPnutService.getTopicalChannels(getChannelsParam.toMap())
    }

    override suspend fun getExploreChannels(
        slug: String,
        getChannelsParam: GetChannelsParam
    ): PnutResponse<List<Channel>> {
        return defaultPnutService.getExploreChannels(slug, getChannelsParam.toMap())
    }

    override suspend fun searchChannels(getChannelsParam: GetChannelsParam): PnutResponse<List<Channel>> {
        return defaultPnutService.searchChannels(getChannelsParam.toMap())
    }

    override suspend fun searchMessages(getChannelsParam: GetChannelsParam): PnutResponse<List<Message>> {
        return defaultPnutService.searchMessages(getChannelsParam.toMap())
    }

    override suspend fun getMessages(
        channelId: String,
        paginationParam: PaginationParam
    ): PnutResponse<List<Message>> {
        return defaultPnutService.getChannelMessages(channelId, paginationParam.toMap())
    }

    override suspend fun deleteMessage(channelId: String, messageId: String): PnutResponse<Message> {
        return defaultPnutService.deleteMessage(channelId, messageId)
    }

    override suspend fun getMessageThread(channelId: String, messageId: String): PnutResponse<List<Message>> {
        return defaultPnutService.getMessageThread(channelId, messageId)
    }

    override suspend fun createMessage(channelId: String, message: PostBody): PnutResponse<Message> {
        return defaultPnutService.createMessage(channelId, message)
    }

    override suspend fun createPmMessage(message: PmPostBody): PnutResponse<Message> {
        return defaultPnutService.createPmMessage(message)
    }

    override suspend fun getExistingPm(ids: IDs): PnutResponse<Channel> {
        return defaultPnutService.getExistingPm(ids)
    }

    override suspend fun subscribe(channelId: String): PnutResponse<Channel> {
        return defaultPnutService.subscribeChannel(channelId)
    }

    override suspend fun unsubscribe(channelId: String): PnutResponse<Channel> {
        return defaultPnutService.unsubscribeChannel(channelId)
    }

    override suspend fun muteChannel(channelId: String): PnutResponse<Channel> {
        return defaultPnutService.muteChannel(channelId)
    }

    override suspend fun unmuteChannel(channelId: String): PnutResponse<Channel> {
        return defaultPnutService.unmuteChannel(channelId)
    }

    override suspend fun getInteractions(getInteractionsParam: GetInteractionsParam): PnutResponse<List<Interaction>> {
        return defaultPnutService.getInteractions(getInteractionsParam.toMap())
    }

    override suspend fun getFiles(getFilesParam: GetFilesParam): PnutResponse<List<io.pnut.gamma.domain.entity.File>> {
        return defaultPnutService.getFiles(getFilesParam.toMap())
    }

    override suspend fun updateMarkers(markers: List<Marker>): PnutResponse<List<Marker>> {
        return defaultPnutService.updateMarkers(markers)
    }

    override suspend fun createPoll(pollPostBody: PollPostBody): PnutResponse<Poll> {
        return defaultPnutService.createPoll(pollPostBody)
    }

    override suspend fun getPoll(pollId: String, pollToken: String): PnutResponse<Poll> {
        return defaultPnutService.getPoll(pollId, pollToken)
    }

    override suspend fun vote(
        pollId: String,
        pollToken: String,
        voteBody: VoteBody
    ): PnutResponse<Poll> {
        return defaultPnutService.vote(pollId, pollToken, voteBody)
    }

    private val cacheSize: Long = 1024 * 1024 * 10


    private var defaultPnutService = createPnutService(defaultAccountToken)

    // Call this function when change account
    override fun updateDefaultPnutService(token: String) {
        defaultPnutService = createPnutService(token)
    }

    private fun createPnutService(token: String? = null): PnutService {
        val client = OkHttpClient.Builder()
        token?.let { client.addInterceptor((getAuthorizationHeaderInterceptor(it))) }

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.HEADERS
            client.addInterceptor(logging)
        }
        val cache = Cache(context.cacheDir, cacheSize)
        client.cache(cache)
        return Retrofit.Builder()
            .baseUrl(Constants.API_BASE_URL)
            .client(client.build())
            .addConverterFactory(MoshiConverterFactory.create(MoshiSingleton.moshi))
            .build()
            .create(PnutService::class.java)
    }

    private fun getAuthorizationHeaderInterceptor(token: String): Interceptor =
        Interceptor {
            val request =
                it.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
            it.proceed(request)
        }


}
