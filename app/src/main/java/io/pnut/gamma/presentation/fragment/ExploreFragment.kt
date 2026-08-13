package io.pnut.gamma.presentation.fragment


import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.domain.model.StreamType
import io.pnut.gamma.presentation.util.FragmentHelper
import io.pnut.gamma.presentation.util.SmoothScroller
import io.pnut.gamma.presentation.util.Util
import io.pnut.gamma.R
import io.pnut.gamma.databinding.ListWithToolbarBinding


abstract class ExploreFragment : PostItemFragment() {

    override fun getFragmentLayout(): Int = R.layout.list_with_toolbar
    override fun getRecyclerView(view: View): RecyclerView = ListWithToolbarBinding.bind(view).itemList

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = ListWithToolbarBinding.bind(view)
        setupToolbar(binding.toolbar)
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.setNavigationOnClickListener { FragmentHelper.backFragment(parentFragmentManager) }
        toolbar.setOnClickListener {
            val ctx = context ?: return@setOnClickListener
            getRecyclerView(requireView()).layoutManager?.startSmoothScroll(SmoothScroller(ctx))
        }
        setTitleToToolbar(toolbar)
        // Hide toolbar if it's in ExplorePostsFragment (parent is a Fragment)
        if (parentFragment is ExplorePostsFragment) {
            toolbar.visibility = View.GONE
        }
    }

    private fun setTitleToToolbar(toolbar: Toolbar) {
        (streamType as? StreamType.Explore)?.let {
            toolbar.setTitle(it.titleRes)
        }
    }

    @AndroidEntryPoint
    class ConversationsFragment : ExploreFragment() {
        override val streamType = StreamType.Explore.Conversations

        companion object {
            fun newInstance() = ConversationsFragment()
        }
    }

    @AndroidEntryPoint
    class MissedConversationsFragment : ExploreFragment() {
        override val streamType = StreamType.Explore.MissedConversations

        companion object {
            fun newInstance() = MissedConversationsFragment()
        }
    }

    @AndroidEntryPoint
    class NewcomersFragment : ExploreFragment() {
        override val streamType = StreamType.Explore.Newcomers

        companion object {
            fun newInstance() = NewcomersFragment()
        }
    }

    @AndroidEntryPoint
    class PhotosFragment : ExploreFragment() {
        override val streamType = StreamType.Explore.Photos

        companion object {
            fun newInstance() = PhotosFragment()
        }
    }

    @AndroidEntryPoint
    class TrendingFragment : ExploreFragment() {
        override val streamType = StreamType.Explore.Trending

        companion object {
            fun newInstance() = TrendingFragment()
        }
    }

    @AndroidEntryPoint
    class GlobalFragment : ExploreFragment(), Util.DrawerContentFragment {
        override val streamType = StreamType.Explore.Global
        override val menuItemId = R.id.global

        companion object {
            fun newInstance() = GlobalFragment()
        }
    }
}
