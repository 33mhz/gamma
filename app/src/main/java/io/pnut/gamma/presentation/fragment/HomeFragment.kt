package io.pnut.gamma.presentation.fragment


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.presentation.util.Util
import io.pnut.gamma.R
import io.pnut.gamma.databinding.FragmentHomeBinding


@AndroidEntryPoint
class HomeFragment : Fragment(), Util.DrawerContentFragment {

    interface Scrollable {
        fun scrollToTop()
    }

    private val tabListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabSelected(tab: TabLayout.Tab?) {
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
            if (tab == null) return
            val fragmentTag = "f${tab.position}"
            val fragment =
                childFragmentManager.findFragmentByTag(fragmentTag) as? Scrollable ?: return
            fragment.scrollToTop()
        }
    }

    override val menuItemId = R.id.home

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = StreamViewPagerAdapter(this, context)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
        binding.tabLayout.addOnTabSelectedListener(tabListener)
    }


    data class Item(val fragment: () -> BaseListFragment<*, *>, @StringRes val title: Int)

    class StreamViewPagerAdapter(fragment: Fragment, val context: Context?) :
        FragmentStateAdapter(fragment) {
        private val items: List<Item> = listOf(
            Item({ PostItemFragment.getHomeStreamInstance() }, R.string.home),
            Item({ PostItemFragment.getMentionStreamInstance() }, R.string.mentions),
            Item({ InteractionFragment.newInstance() }, R.string.interactions),
            Item({ PostItemFragment.getStarInstance() }, R.string.stars)
        )

        override fun getItemCount(): Int {
            return items.size
        }

        override fun createFragment(position: Int): Fragment {
            return items[position].fragment()
        }

        fun getPageTitle(position: Int): CharSequence? {
            return context?.getString(items[position].title)
        }
    }

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}
