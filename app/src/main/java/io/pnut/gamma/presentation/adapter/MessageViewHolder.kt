package io.pnut.gamma.presentation.adapter

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import io.pnut.gamma.R
import io.pnut.gamma.presentation.view.LinkableTextView

class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
    val userTypeIconImageView: ImageView = itemView.findViewById(R.id.userTypeIconImageView)
    val screenNameTextView: TextView = itemView.findViewById(R.id.screenNameTextView)
    val handleNameTextView: TextView = itemView.findViewById(R.id.handleNameTextView)
    val relativeTimeTextView: TextView = itemView.findViewById(R.id.relativeTimeTextView)
    val bodyTextView: LinkableTextView = itemView.findViewById(R.id.bodyTextView)
    val goToChannelButton: MaterialButton = itemView.findViewById(R.id.goToChannelButton)
    val foregroundActionsLayout: LinearLayout = itemView.findViewById(R.id.foregroundActionsLayout)
    val replyButton: ImageButton = itemView.findViewById(R.id.replyButton)
    val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    val threadButton: ImageButton = itemView.findViewById(R.id.threadButton)
    val broadcastButton: ImageButton = itemView.findViewById(R.id.broadcastButton)
    val moreButton: ImageButton = itemView.findViewById(R.id.moreButton)

    val nsfwMaskLayout: FrameLayout = itemView.findViewById(R.id.nsfwMaskLayout)
    val showNsfwButton: MaterialButton = itemView.findViewById(R.id.showNsfwButton)
    val spoilerMaskLayout: FrameLayout = itemView.findViewById(R.id.spoilerMaskLayout)
    val showSpoilerButton: MaterialButton = itemView.findViewById(R.id.showSpoilerButton)
    val contentsWrapperLayout: LinearLayout = itemView.findViewById(R.id.contentsWrapperLayout)
    val thumbnailViewPager: ViewPager2 = itemView.findViewById(R.id.thumbnailViewPager)
    val thumbnailViewPagerFrameLayout: FrameLayout = itemView.findViewById(R.id.thumbnailViewPagerFrameLayout)
    val thumbnailTabLayout: TabLayout = itemView.findViewById(R.id.thumbnailTabLayout)
}
