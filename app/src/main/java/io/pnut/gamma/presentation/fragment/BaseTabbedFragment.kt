package io.pnut.gamma.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.pnut.gamma.databinding.FragmentChannelsBinding
import io.pnut.gamma.presentation.util.Util

abstract class BaseTabbedFragment : BaseFragment(), Util.DrawerContentFragment {
    private var _binding: FragmentChannelsBinding? = null
    protected val binding get() = _binding!!

    abstract val titleRes: Int
    open val titleTemplateRes: Int? = null
    abstract val pagerItems: List<PagerItem>
    open val tabMode: Int = TabLayout.MODE_SCROLLABLE

    data class PagerItem(
        val fragmentProvider: () -> Fragment,
        @StringRes val titleRes: Int,
        @DrawableRes val iconRes: Int? = null
    )

    private val tabListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabSelected(tab: TabLayout.Tab?) {
            tab?.let { updateToolbarTitle(it.position) }
        }
        override fun onTabReselected(tab: TabLayout.Tab?) {
            if (tab == null) return
            val fragmentTag = "f${tab.position}"
            val fragment = childFragmentManager.findFragmentByTag(fragmentTag) as? Util.Scrollable ?: return
            fragment.scrollToTop()
        }
    }

    private fun updateToolbarTitle(position: Int) {
        val item = pagerItems[position]
        val subTitle = getString(item.titleRes)
        val fullTitle = titleTemplateRes?.let { getString(it, subTitle) } ?: getString(titleRes)
        binding.toolbar.title = fullTitle
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChannelsBinding.inflate(inflater, container, false)
        binding.toolbar.setNavigationOnClickListener { backToPrevFragment() }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = GenericPagerAdapter(this, pagerItems)
        binding.channelsViewPager.adapter = adapter
        binding.channelsViewPager.offscreenPageLimit = 1
        binding.channelsTabLayout.tabMode = tabMode
        TabLayoutMediator(binding.channelsTabLayout, binding.channelsViewPager) { tab, position ->
            val item = pagerItems[position]
            if (item.iconRes != null) {
                tab.setIcon(item.iconRes)
            } else {
                tab.text = getString(item.titleRes)
            }
        }.attach()
        binding.channelsTabLayout.addOnTabSelectedListener(tabListener)
        updateToolbarTitle(binding.channelsTabLayout.selectedTabPosition)
    }

    class GenericPagerAdapter(fragment: Fragment, private val items: List<PagerItem>) :
        FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = items.size
        override fun createFragment(position: Int): Fragment = items[position].fragmentProvider()
    }
}
