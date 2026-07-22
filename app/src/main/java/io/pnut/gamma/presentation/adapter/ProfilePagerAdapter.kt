package io.pnut.gamma.presentation.adapter

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.pnut.gamma.presentation.fragment.PostItemFragment
import io.pnut.gamma.R

class ProfilePagerAdapter(val context: Context, fragment: Fragment, userId: String) : FragmentStateAdapter(fragment) {
    private data class FragmentInfo(val fragment: () -> Fragment, @StringRes val titleRes: Int)
    private val fragments = arrayOf(
        FragmentInfo({ PostItemFragment.getUserPostInstance(userId) }, R.string.posts),
        FragmentInfo({ PostItemFragment.getStarInstance(userId) }, R.string.favorites)
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position].fragment()

    fun getPageTitle(position: Int): CharSequence = context.getString(fragments[position].titleRes)
}
