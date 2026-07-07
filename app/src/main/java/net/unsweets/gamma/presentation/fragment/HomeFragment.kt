package net.unsweets.gamma.presentation.fragment


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import net.unsweets.gamma.R
import net.unsweets.gamma.databinding.FragmentHomeBinding
import net.unsweets.gamma.presentation.util.Util


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
            val fragmentTag = "android:switcher:${binding.viewPager.id}:${adapter.getItemId(tab.position)}"
            val fragment =
                childFragmentManager.findFragmentByTag(fragmentTag) as? Scrollable ?: return
            fragment.scrollToTop()
        }
    }

    val adapter by lazy {
        StreamViewPagerAdapter(childFragmentManager, context)
    }


    override val menuItemId = R.id.home

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        binding.tabLayout.addOnTabSelectedListener(tabListener)
    }


    data class Item(val fragment: BaseListFragment<*, *>, @StringRes val title: Int)

    class StreamViewPagerAdapter(fm: FragmentManager, val context: Context?) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val items: List<Item> = listOf(
            Item(PostItemFragment.getHomeStreamInstance(), R.string.home),
            Item(PostItemFragment.getMentionStreamInstance(), R.string.mentions),
            Item(InteractionFragment.newInstance(), R.string.interactions),
            Item(PostItemFragment.getStarInstance(), R.string.stars)
        )
        override fun getItem(position: Int): Fragment {
            return items[position].fragment
        }

        override fun getCount(): Int {
            return items.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return context?.getString(items[position].title)
        }
    }

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}
