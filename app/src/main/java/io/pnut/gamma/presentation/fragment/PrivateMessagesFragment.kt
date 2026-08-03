package io.pnut.gamma.presentation.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.R
import io.pnut.gamma.databinding.ListWithToolbarBinding
import io.pnut.gamma.domain.model.ChannelType
import io.pnut.gamma.presentation.util.FragmentHelper
import io.pnut.gamma.presentation.util.SmoothScroller
import io.pnut.gamma.presentation.util.Util

@AndroidEntryPoint
class PrivateMessagesFragment : ChannelListFragment(), Util.DrawerContentFragment {
    override val menuItemId = R.id.privateMessages

    override fun getFragmentLayout(): Int = R.layout.list_with_toolbar
    override fun getRecyclerView(view: View): RecyclerView = ListWithToolbarBinding.bind(view).itemList
    override fun getSwipeRefreshLayout(view: View): SwipeRefreshLayout = ListWithToolbarBinding.bind(view).swipeRefreshLayout

    override fun getItemLayout(): Int = R.layout.fragment_channel_post_item

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
        toolbar.setTitle(R.string.private_messages)
    }

    companion object {
        fun newInstance() = PrivateMessagesFragment().apply {
            arguments = Bundle().apply {
                putSerializable(BundleKey.ChannelType.name, ChannelType.PM)
            }
        }
    }
}
