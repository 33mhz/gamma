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


abstract class ExploreFragment : PostItemFragment(), Util.DrawerContentFragment {

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

    }

    private fun setTitleToToolbar(toolbar: Toolbar) {
        (streamType as? StreamType.Explore)?.let {
            toolbar.setTitle(it.titleRes)
        }
    }

    @AndroidEntryPoint
    class ConversationsFragment : ExploreFragment() {
        override val streamType = StreamType.Explore.Conversations
        override val menuItemId = R.id.conversations

        companion object {
            fun newInstance() = ConversationsFragment()
        }
    }

    @AndroidEntryPoint
    class MissedConversationsFragment : ExploreFragment() {
        override val streamType = StreamType.Explore.MissedConversations
        override val menuItemId = R.id.missedConversations

        companion object {
            fun newInstance() = MissedConversationsFragment()
        }
    }

    @AndroidEntryPoint
    class NewcomersFragment : ExploreFragment() {
        override val streamType = StreamType.Explore.Newcomers
        override val menuItemId = R.id.newcomers

        companion object {
            fun newInstance() = NewcomersFragment()
        }
    }

    @AndroidEntryPoint
    class PhotosFragment : ExploreFragment() {
        override val streamType = StreamType.Explore.Photos
        override val menuItemId = R.id.photos

        companion object {
            fun newInstance() = PhotosFragment()
        }
    }

    @AndroidEntryPoint
    class TrendingFragment : ExploreFragment() {
        override val streamType = StreamType.Explore.Trending
        override val menuItemId = R.id.trending

        companion object {
            fun newInstance() = TrendingFragment()
        }
    }

    @AndroidEntryPoint
    class GlobalFragment : ExploreFragment() {
        override val streamType = StreamType.Explore.Global
        override val menuItemId = R.id.global

        companion object {
            fun newInstance() = GlobalFragment()
        }
    }
}
