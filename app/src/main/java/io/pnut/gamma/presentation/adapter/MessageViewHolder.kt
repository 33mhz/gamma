package io.pnut.gamma.presentation.adapter

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.pnut.gamma.R
import io.pnut.gamma.presentation.view.LinkableTextView

class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
    val screenNameTextView: TextView = itemView.findViewById(R.id.screenNameTextView)
    val handleNameTextView: TextView = itemView.findViewById(R.id.handleNameTextView)
    val relativeTimeTextView: TextView = itemView.findViewById(R.id.relativeTimeTextView)
    val bodyTextView: LinkableTextView = itemView.findViewById(R.id.bodyTextView)
    val foregroundActionsLayout: LinearLayout = itemView.findViewById(R.id.foregroundActionsLayout)
    val replyButton: ImageButton = itemView.findViewById(R.id.replyButton)
    val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    val threadButton: ImageButton = itemView.findViewById(R.id.threadButton)
}
