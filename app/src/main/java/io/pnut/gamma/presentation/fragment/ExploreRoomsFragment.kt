package io.pnut.gamma.presentation.fragment

import com.google.android.material.tabs.TabLayout
import io.pnut.gamma.R
import io.pnut.gamma.domain.model.ChannelType

class ExploreRoomsFragment : BaseTabbedFragment() {
    override val menuItemId = R.id.exploreRooms
    override val titleRes = R.string.explore_rooms
    override val titleTemplateRes = R.string.explore_template
    override val tabMode = TabLayout.MODE_FIXED

    override val pagerItems = listOf(
        PagerItem({ ChannelListFragment.newInstance(ChannelType.ExploreConversations) }, R.string.channels_active, R.drawable.ic_forum_black_24dp),
        PagerItem({ ChannelListFragment.newInstance(ChannelType.ExploreNew) }, R.string.channels_new, R.drawable.psychiatry_24px),
        PagerItem({ ChannelListFragment.newInstance(ChannelType.ExploreTopical) }, R.string.channels_topical, R.drawable.ic_signpost_black_24dp),
        PagerItem({ ChannelListFragment.newInstance(ChannelType.ExploreTrending) }, R.string.trending, R.drawable.ic_trending_up_black_24dp)
    )

    companion object {
        fun newInstance() = ExploreRoomsFragment()
    }
}
