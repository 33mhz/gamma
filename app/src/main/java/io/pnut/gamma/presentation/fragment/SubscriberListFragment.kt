package io.pnut.gamma.presentation.fragment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.R
import io.pnut.gamma.databinding.ListWithToolbarBinding
import io.pnut.gamma.domain.model.UserListType

@AndroidEntryPoint
class SubscriberListFragment : UserListFragment() {
    override fun getFragmentLayout(): Int = R.layout.list_with_toolbar
    override fun getRecyclerView(view: View): RecyclerView = ListWithToolbarBinding.bind(view).itemList
    
    private val channelId by lazy { arguments?.getString(BundleKey.ChannelId.name) ?: throw IllegalArgumentException("Must set channelId") }

    override val userListType by lazy {
        UserListType.Subscribers(channelId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = ListWithToolbarBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener { backToPrevFragment() }
        
        binding.toolbar.title = getString(R.string.subscribers_with_id, channelId)
        if (arguments?.containsKey(BundleKey.Count.name) == true) {
            val subscriberCount = arguments?.getInt(BundleKey.Count.name) ?: 0
            binding.toolbar.subtitle = resources.getQuantityString(R.plurals.user, subscriberCount, subscriberCount)
        }
    }

    private enum class BundleKey { ChannelId, Count }

    companion object {
        fun newInstance(channelId: String, count: Int?) = SubscriberListFragment().apply {
            arguments = Bundle().apply {
                putString(BundleKey.ChannelId.name, channelId)
                if (count != null) {
                    putInt(BundleKey.Count.name, count)
                }
            }
        }
    }
}
