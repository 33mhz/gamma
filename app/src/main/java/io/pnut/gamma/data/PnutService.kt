package io.pnut.gamma.data

import io.pnut.gamma.domain.entity.Channel
import io.pnut.gamma.domain.entity.Explore
import io.pnut.gamma.domain.entity.File
import io.pnut.gamma.domain.entity.IDs
import io.pnut.gamma.domain.entity.Interaction
import io.pnut.gamma.domain.entity.InteractionFilter
import io.pnut.gamma.domain.entity.Marker
import io.pnut.gamma.domain.entity.Message
import io.pnut.gamma.domain.entity.PostBody
import io.pnut.gamma.domain.entity.PmPostBody
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.Poll
import io.pnut.gamma.domain.entity.PollPostBody
import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.domain.entity.StarBody
import io.pnut.gamma.domain.entity.ProfileBody
import io.pnut.gamma.domain.entity.Token
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.entity.VoteBody
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface PnutService {
    @GET("token")
    suspend fun token(): PnutResponse<Token>

    // Post resources
    @POST("posts?include_post_raw=1")
    suspend fun createPost(@Body postBody: PostBody): PnutResponse<Post>

    @PUT("posts/{postId}?include_post_raw=1")
    suspend fun editPost(@Path("postId") postId: String, @Body postBody: PostBody): PnutResponse<Post>

    @DELETE("posts/{postId}")
    suspend fun deletePost(@Path("postId") postId: String): PnutResponse<Post>

    @GET("posts/streams/me")
    suspend fun getPersonalStream(@QueryMap params: Map<String, String>): PnutResponse<List<Post>>

    @GET("posts/streams/unified")
    suspend fun getUnifiedStream(@QueryMap params: Map<String, String>): PnutResponse<List<Post>>

    @GET("posts/search")
    suspend fun searchPosts(@QueryMap queries: Map<String, String>): PnutResponse<List<Post>>

    @GET("posts/streams/global")
    suspend fun getGlobal(@QueryMap pagination: Map<String, String>): PnutResponse<List<Post>>

    @GET("posts/tags/{tag}")
    suspend fun getTaggedPosts(@Path("tag") tag: String, @QueryMap pagination: Map<String, String>): PnutResponse<List<Post>>

    @GET("posts/streams/explore")
    suspend fun getExploreList(): PnutResponse<List<Explore>>

    @GET("posts/streams/explore/{slug}")
    suspend fun getExplore(@Path("slug") slug: String, @QueryMap pagination: Map<String, String>): PnutResponse<List<Post>>

    @GET("posts/{postId}/thread")
    suspend fun getThread(@Path("postId") postId: String, @QueryMap params: Map<String, String>): PnutResponse<List<Post>>

    @PUT("posts/{postId}/bookmark?include_post_raw=1")
    suspend fun createStar(@Path("postId") postId: String, @Body body: StarBody = StarBody()): PnutResponse<Post>

    @DELETE("posts/{postId}/bookmark?include_post_raw=1")
    suspend fun deleteStar(@Path("postId") postId: String): PnutResponse<Post>

    @PUT("posts/{postId}/repost?include_post_raw=1")
    suspend fun createRepost(@Path("postId") postId: String): PnutResponse<Post>

    @DELETE("posts/{postId}/repost?include_post_raw=1")
    suspend fun deleteRepost(@Path("postId") postId: String): PnutResponse<Post>

    @FormUrlEncoded
    @POST("posts/{postId}/report")
    suspend fun reportPost(@Path("postId") postId: String, @Field("reason") reason: String)

    @GET("posts/{postId}/interactions")
    suspend fun getPostInteractions(
        @Path("postId") postId: String, @Query("filters") filters: InteractionFilter?, @Query(
            "exclude"
        ) exclude: InteractionFilter?
    ): PnutResponse<List<Interaction>>

    @GET("posts")
    suspend fun getPosts(@QueryMap params: Map<String, String>): PnutResponse<List<Post>>

    @GET("posts/{postId}/revisions")
    suspend fun getRevisions(@Path("postId") postId: String): PnutResponse<List<Post>>

    // user/post resources

    @GET("users/{userId}/posts")
    suspend fun getUserPosts(@Path("userId") userId: String, @QueryMap pagination: Map<String, String>): PnutResponse<List<Post>>

    @GET("users/{userId}/mentions")
    suspend fun getUserMentions(@Path("userId") userId: String): PnutResponse<List<Post>>

    @GET("users/me/mentions")
    suspend fun getMentions(@QueryMap pagination: Map<String, String>): PnutResponse<List<Post>>

    @GET("users/{userId}/bookmarks")
    suspend fun getStars(@Path("userId") userId: String, @QueryMap pagination: Map<String, String>): PnutResponse<List<Post>>

    @GET("users/me/interactions")
    suspend fun getInteractions(@QueryMap pagination: Map<String, String>): PnutResponse<List<Interaction>>

    @GET("users/me/blocked")
    suspend fun getBlockedUsers(@QueryMap pagination: Map<String, String>): PnutResponse<List<User>>

    @GET("users/me/muted")
    suspend fun getMutedUsers(@QueryMap pagination: Map<String, String>): PnutResponse<List<User>>

    // User(s) resources
    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): PnutResponse<User>

    @PUT("users/me")
    suspend fun putMyProfile(@Body profileBody: ProfileBody): PnutResponse<User>

    @POST("users/me/avatar")
    @Multipart
    suspend fun updateAvatar(@Part avatar: MultipartBody.Part): PnutResponse<User>

    @POST("users/me/cover")
    @Multipart
    suspend fun updateCover(@Part cover: MultipartBody.Part): PnutResponse<User>

    @GET("users/{userId}/following")
    suspend fun getFollowing(@Path("userId") userId: String, @QueryMap pagination: Map<String, String>): PnutResponse<List<User>>

    @GET("users/{userId}/followers")
    suspend fun getFollowers(@Path("userId") userId: String, @QueryMap pagination: Map<String, String>): PnutResponse<List<User>>

    @PUT("users/{userId}/follow")
    suspend fun follow(@Path("userId") userId: String): PnutResponse<User>

    @DELETE("users/{userId}/follow")
    suspend fun unFollow(@Path("userId") userId: String): PnutResponse<User>

    @PUT("users/{userId}/mute")
    suspend fun mute(@Path("userId") userId: String): PnutResponse<User>

    @DELETE("users/{userId}/mute")
    suspend fun unMute(@Path("userId") userId: String): PnutResponse<User>

    @PUT("users/{userId}/block")
    suspend fun block(@Path("userId") userId: String): PnutResponse<User>

    @DELETE("users/{userId}/block")
    suspend fun unBlock(@Path("userId") userId: String): PnutResponse<User>

    @GET("users/suggested")
    suspend fun getSuggestedUsers(@QueryMap pagination: Map<String, String>): PnutResponse<List<User>>

    @GET("users/search")
    suspend fun searchUsers(@QueryMap queries: Map<String, String>): PnutResponse<List<User>>

    // Channel resources
    @GET("channels/{channelId}?include_channel_raw=1")
    suspend fun getChannel(@Path("channelId") channelId: String): PnutResponse<Channel>

    @GET("channels/{channelId}/subscribers")
    suspend fun getSubscribers(@Path("channelId") channelId: String, @QueryMap pagination: Map<String, String>): PnutResponse<List<User>>

    @GET("channels/{channelId}/messages?include_deleted=0&include_message_raw=1")
    suspend fun getChannelMessages(@Path("channelId") channelId: String, @QueryMap paging: Map<String, String>): PnutResponse<List<Message>>

    @GET("channels/{channelId}/messages/{messageId}/thread?include_deleted=0&include_message_raw=1")
    suspend fun getMessageThread(@Path("channelId") channelId: String, @Path("messageId") messageId: String): PnutResponse<List<Message>>

    @POST("channels/{channelId}/messages?include_message_raw=1&update_marker=1")
    suspend fun createMessage(@Path("channelId") channelId: String, @Body message: PostBody): PnutResponse<Message>

    @POST("channels/pm/messages?include_message_raw=1&update_marker=1")
    suspend fun createPmMessage(@Body message: PmPostBody): PnutResponse<Message>

    @DELETE("channels/{channelId}/messages/{messageId}")
    suspend fun deleteMessage(@Path("channelId") channelId: String, @Path("messageId") messageId: String): PnutResponse<Message>



    @GET("users/me/channels/existing_pm")
    suspend fun getExistingPm(@Query("ids") ids: IDs): PnutResponse<Channel>

    // TODO: "returns {"io.pnut.core.pm": 0}"
    @GET("users/me/channels/num_unread?channel_types=io.pnut.core.pm")
    suspend fun getUnreadPmCount(): PnutResponse<Int>

    @GET("users/me/channels/subscribed")
    suspend fun getSubscribedChannels(@QueryMap paging: Map<String, String>): PnutResponse<List<Channel>>

    @GET("users/me/channels/subscribed")
    suspend fun getPmChannels(@QueryMap paging: Map<String, String>): PnutResponse<List<Channel>>

    @PUT("channels/{channelId}/subscribe")
    suspend fun subscribeChannel(@Path("channelId") channelId: String): PnutResponse<Channel>

    @DELETE("channels/{channelId}/subscribe")
    suspend fun unsubscribeChannel(@Path("channelId") channelId: String): PnutResponse<Channel>

    @PUT("channels/{channelId}/mute")
    suspend fun muteChannel(@Path("channelId") channelId: String): PnutResponse<Channel>

    @DELETE("channels/{channelId}/mute")
    suspend fun unmuteChannel(@Path("channelId") channelId: String): PnutResponse<Channel>

    @GET("channels/streams/explore/topical")
    suspend fun getTopicalChannels(@QueryMap paging: Map<String, String>): PnutResponse<List<Channel>>

    @GET("channels/streams/explore/{slug}")
    suspend fun getExploreChannels(@Path("slug") slug: String, @QueryMap paging: Map<String, String>): PnutResponse<List<Channel>>

    @GET("channels/search")
    suspend fun searchChannels(@QueryMap params: Map<String, String>): PnutResponse<List<Channel>>

    @GET("channels/messages/search")
    suspend fun searchMessages(@QueryMap params: Map<String, String>): PnutResponse<List<Message>>

    @GET("users/me/channels")
    suspend fun getChannels(@QueryMap paging: Map<String, String>): PnutResponse<List<Channel>>

    @GET("users/me/files")
    suspend fun getFiles(@QueryMap paging: Map<String, String>): PnutResponse<List<File>>

    @Multipart
    @POST("files")
    suspend fun createFile(
        @Part content: MultipartBody.Part,
        @Part("name") name: RequestBody,
        @Part("kind") kind: RequestBody,
        @Part("type") type: RequestBody,
        @Part("is_public") isPublic: RequestBody
    ): PnutResponse<File>

    @POST("polls")
    suspend fun createPoll(@Body pollPostBody: PollPostBody): PnutResponse<Poll>

    @GET("polls/{pollId}")
    suspend fun getPoll(@Path("pollId") pollId: String, @Query("poll_token") pollToken: String): PnutResponse<Poll>

    @PUT("polls/{pollId}/response")
    suspend fun vote(@Path("pollId") pollId: String, @Query("poll_token") pollToken: String, @Body voteBody: VoteBody): PnutResponse<Poll>

    @DELETE("users/me/avatar")
    suspend fun deleteAvatar(): PnutResponse<User>

    @DELETE("users/me/cover")
    suspend fun deleteCover(): PnutResponse<User>

    @POST("markers")
    suspend fun updateMarkers(@Body markers: List<Marker>): PnutResponse<List<Marker>>

}
