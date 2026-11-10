package io.pnut.gamma.broadcast

import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.service.PostWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PostReceiver(private val listener: Callback) {

    interface Callback {
        fun onPostReceive(post: Post)
        fun onStarReceive(post: Post)
        fun onRepostReceive(post: Post)
        fun onDeletePostReceive(post: Post)
        fun onReportPostReceive()
    }

    private var job: Job? = null

    fun register(coroutineScope: CoroutineScope) {
        unregister()
        job = coroutineScope.launch {
            AppEventBus.postEvents.collect { event ->
                when (event) {
                    is AppEventBus.PostEvent.SendPost -> listener.onPostReceive(event.post)
                    is AppEventBus.PostEvent.Star -> listener.onStarReceive(event.post)
                    is AppEventBus.PostEvent.Repost -> listener.onRepostReceive(event.post)
                    is AppEventBus.PostEvent.DeletePost -> listener.onDeletePostReceive(event.post)
                    is AppEventBus.PostEvent.ReportPost -> listener.onReportPostReceive()
                }
            }
        }
    }

    fun unregister() {
        job?.cancel()
        job = null
    }

    companion object {
        fun broadcast(action: PostWorker.Actions, post: Post? = null) {
            val event = when (action) {
                PostWorker.Actions.SendPost -> post?.let { AppEventBus.PostEvent.SendPost(it) }
                PostWorker.Actions.Star -> post?.let { AppEventBus.PostEvent.Star(it) }
                PostWorker.Actions.Repost -> post?.let { AppEventBus.PostEvent.Repost(it) }
                PostWorker.Actions.DeletePost -> post?.let { AppEventBus.PostEvent.DeletePost(it) }
                PostWorker.Actions.ReportPost -> AppEventBus.PostEvent.ReportPost
            }
            event?.let { AppEventBus.send(it) }
        }
    }
}
