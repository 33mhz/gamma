package io.pnut.gamma.presentation.fragment

import io.pnut.gamma.R

class ChannelsFragment : BaseTabbedFragment() {
    override val menuItemId = R.id.channels
    override val titleRes = R.string.chat_rooms

    override val pagerItems = listOf(
        PagerItem({ ChannelListFragment.subscribedChannels() }, R.string.channels_subscribed),
        PagerItem({ ChannelListFragment.yoursChannels() }, R.string.channels_yours)
    )

    companion object {
        fun newInstance() = ChannelsFragment()
    }
}
