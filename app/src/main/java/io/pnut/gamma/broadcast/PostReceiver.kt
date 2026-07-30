package io.pnut.gamma.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.service.PostWorker

class PostReceiver(private val listener: Callback) : BroadcastReceiver() {

    interface Callback {
        fun onPostReceive(post: Post)
        fun onStarReceive(post: Post)
        fun onRepostReceive(post: Post)
        fun onDeletePostReceive(post: Post)
        fun onReportPostReceive()
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val postAction = PostWorker.Actions.getAction(action) ?: return
        when (postAction) {
            PostWorker.Actions.SendPost -> listener.onPostReceive(PostWorker.getPost(intent) ?: return)
            PostWorker.Actions.Star -> listener.onStarReceive(PostWorker.getPost(intent) ?: return)
            PostWorker.Actions.Repost -> listener.onRepostReceive(PostWorker.getPost(intent) ?: return)
            PostWorker.Actions.DeletePost -> listener.onDeletePostReceive(PostWorker.getPost(intent) ?: return)
            PostWorker.Actions.ReportPost -> listener.onReportPostReceive()
        }

    }
}
