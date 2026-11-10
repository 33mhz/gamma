package io.pnut.gamma.broadcast

import io.pnut.gamma.domain.Relationship
import io.pnut.gamma.domain.entity.Message
import io.pnut.gamma.domain.entity.Post
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AppEventBus {

    sealed class PostEvent {
        data class SendPost(val post: Post) : PostEvent()
        data class Star(val post: Post) : PostEvent()
        data class Repost(val post: Post) : PostEvent()
        data class DeletePost(val post: Post) : PostEvent()
        object ReportPost : PostEvent()
    }

    data class RelationshipEvent(
        val userId: String?,
        val relationship: Relationship?
    )

    sealed class ChannelEvent {
        data class ChannelRead(val channelId: String) : ChannelEvent()
    }

    private val _messageEvents = MutableSharedFlow<Message>(extraBufferCapacity = 64)
    val messageEvents: SharedFlow<Message> = _messageEvents.asSharedFlow()

    private val _relationshipEvents = MutableSharedFlow<RelationshipEvent>(extraBufferCapacity = 64)
    val relationshipEvents: SharedFlow<RelationshipEvent> = _relationshipEvents.asSharedFlow()

    private val _postEvents = MutableSharedFlow<PostEvent>(extraBufferCapacity = 64)
    val postEvents: SharedFlow<PostEvent> = _postEvents.asSharedFlow()

    private val _channelEvents = MutableSharedFlow<ChannelEvent>(extraBufferCapacity = 64)
    val channelEvents: SharedFlow<ChannelEvent> = _channelEvents.asSharedFlow()

    private val _errorEvents = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val errorEvents: SharedFlow<String> = _errorEvents.asSharedFlow()

    fun send(message: Message) {
        _messageEvents.tryEmit(message)
    }

    fun send(userId: String?, relationship: Relationship?) {
        _relationshipEvents.tryEmit(RelationshipEvent(userId, relationship))
    }

    fun send(postEvent: PostEvent) {
        _postEvents.tryEmit(postEvent)
    }

    fun send(channelEvent: ChannelEvent) {
        _channelEvents.tryEmit(channelEvent)
    }

    fun sendError(errorMessage: String) {
        _errorEvents.tryEmit(errorMessage)
    }
}
