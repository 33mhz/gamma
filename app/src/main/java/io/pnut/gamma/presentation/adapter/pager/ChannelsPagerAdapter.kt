package io.pnut.gamma.presentation.adapter.pager

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.pnut.gamma.presentation.fragment.ChannelListFragment
import io.pnut.gamma.R

class ChannelsPagerAdapter(
    private val context: Context,
    fragment: Fragment
) : FragmentStateAdapter(fragment) {
    data class FragmentInfo(val fragment: () -> Fragment, @StringRes val titleRes: Int)

    private val fragments = listOf(
        FragmentInfo(
            { ChannelListFragment.subscribedChannels() },
            R.string.channels_subscribed
        ),
        FragmentInfo(
            { ChannelListFragment.topicalChannels() },
            R.string.channels_topical
        )
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position].fragment()

    fun getPageTitle(position: Int): CharSequence =
        context.getString(fragments[position].titleRes)
}
