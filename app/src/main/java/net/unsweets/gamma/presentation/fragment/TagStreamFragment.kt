package net.unsweets.gamma.presentation.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import net.unsweets.gamma.R
import net.unsweets.gamma.databinding.ListWithToolbarBinding
import net.unsweets.gamma.domain.model.StreamType
import net.unsweets.gamma.presentation.util.ShareUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TagStreamFragment : PostItemFragment() {
    private val hashTag: String by lazy { arguments?.getString(BundleKey.Tag.name, "") ?: "" }
    override val streamType: StreamType by lazy {
        StreamType.Tag(hashTag)
    }

    override fun getFragmentLayout(): Int = R.layout.list_with_toolbar
    override fun getRecyclerView(view: View): RecyclerView = ListWithToolbarBinding.bind(view).itemList
    override fun getSwipeRefreshLayout(view: View): SwipeRefreshLayout = ListWithToolbarBinding.bind(view).swipeRefreshLayout

    private val taggedPostsRssUrl by lazy {
        "https://api.pnut.io/v0/feed/rss/posts/tags/$hashTag"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = ListWithToolbarBinding.bind(view)
        binding.toolbar.title = getString(R.string.tag_stream_fragment_title_template, hashTag)
        binding.toolbar.setNavigationOnClickListener {
            backToPrevFragment()
        }
        binding.toolbar.inflateMenu(R.menu.tag_stream)
        binding.toolbar.setOnMenuItemClickListener(menuIemListener)
    }

    private val menuIemListener = Toolbar.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.shareTagStreamRss -> shareRssUrl()
        }
        true
    }

    private fun shareRssUrl() {
        activity?.let { ShareUtil.launchShareUrlIntent(it, taggedPostsRssUrl) }
    }

    private enum class BundleKey { Tag }

    companion object {
        fun newInstance(tag: String) = TagStreamFragment().apply {
            arguments = Bundle().apply {
                putString(BundleKey.Tag.name, tag)
            }
        }
    }

}
