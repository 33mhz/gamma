package io.pnut.gamma.presentation.fragment

import com.google.android.material.tabs.TabLayout
import io.pnut.gamma.R

class ExplorePostsFragment : BaseTabbedFragment() {
    override val menuItemId = R.id.explorePosts
    override val titleRes = R.string.explore_posts
    override val titleTemplateRes = R.string.explore_template
    override val tabMode = TabLayout.MODE_FIXED

    override val pagerItems = listOf(
        PagerItem(ExploreFragment.ConversationsFragment::newInstance, R.string.conversations, R.drawable.ic_forum_black_24dp),
        PagerItem(ExploreFragment.MissedConversationsFragment::newInstance, R.string.missed_conversations, R.drawable.ic_chat_bubble_black_24dp),
        PagerItem(ExploreFragment.NewcomersFragment::newInstance, R.string.newcomers, R.drawable.ic_person_add_black_24dp),
        PagerItem(ExploreFragment.PhotosFragment::newInstance, R.string.photos, R.drawable.ic_photo_camera_24dp),
        PagerItem(ExploreFragment.TrendingFragment::newInstance, R.string.trending, R.drawable.ic_trending_up_black_24dp)
    )

    companion object {
        fun newInstance() = ExplorePostsFragment()
    }
}
