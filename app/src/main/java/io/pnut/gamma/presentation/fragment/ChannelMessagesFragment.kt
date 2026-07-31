package io.pnut.gamma.presentation.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.R
import io.pnut.gamma.databinding.ListWithToolbarBinding
import io.pnut.gamma.domain.entity.Message
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.model.PageableItemWrapper
import io.pnut.gamma.domain.model.io.GetMessagesInputData
import io.pnut.gamma.domain.model.params.single.PaginationParam
import io.pnut.gamma.domain.usecases.GetMessagesUseCase
import io.pnut.gamma.presentation.adapter.BaseListRecyclerViewAdapter
import io.pnut.gamma.presentation.adapter.MessageViewHolder
import io.pnut.gamma.presentation.util.BindingUtil
import io.pnut.gamma.presentation.util.DateUtil
import io.pnut.gamma.presentation.util.EntityOnTouchListener
import io.pnut.gamma.presentation.util.FragmentHelper
import io.pnut.gamma.presentation.util.SmoothScroller
import javax.inject.Inject

@AndroidEntryPoint
class ChannelMessagesFragment : BaseListFragment<Message, MessageViewHolder>(),
    BaseListRecyclerViewAdapter.IBaseList<Message, MessageViewHolder> {

    private enum class BundleKey { ChannelId, Title }

    private val channelId by lazy {
        arguments?.getString(BundleKey.ChannelId.name) ?: throw IllegalArgumentException("channelId is required")
    }

    private val title by lazy {
        arguments?.getString(BundleKey.Title.name) ?: ""
    }

    @Inject
    lateinit var getMessagesUseCase: GetMessagesUseCase

    override val reverse = true

    override fun getFragmentLayout(): Int = R.layout.list_with_toolbar
    override fun getRecyclerView(view: View): RecyclerView = ListWithToolbarBinding.bind(view).itemList
    override fun getSwipeRefreshLayout(view: View): SwipeRefreshLayout = ListWithToolbarBinding.bind(view).swipeRefreshLayout

    override val viewModel: BaseListViewModel<Message> by lazy {
        ViewModelProvider(
            this, ChannelMessagesViewModel.Factory(channelId, getMessagesUseCase)
        )[ChannelMessagesViewModel::class.java]
    }

    override val baseListListener: BaseListRecyclerViewAdapter.IBaseList<Message, MessageViewHolder> by lazy { this }

    override fun createViewHolder(mView: View, viewType: Int): MessageViewHolder {
        return MessageViewHolder(mView).also {
            it.bodyTextView.setOnTouchListener(EntityOnTouchListener())
        }
    }

    override fun onClickItemListener(
        viewHolder: MessageViewHolder,
        item: Message,
        itemWrapper: PageableItemWrapper<Message>
    ) {
        // Handle click if needed
    }

    override fun onBindViewHolder(
        item: Message,
        viewHolder: MessageViewHolder,
        position: Int,
        isMainItem: Boolean
    ) {
        item.user?.let { user ->
            viewHolder.screenNameTextView.text = user.name
            viewHolder.handleNameTextView.text = viewHolder.itemView.context.getString(R.string.user_name_format, user.username)
            val avatarUrl = User.getAvatarUrl(user, User.AvatarSize.Small)
            BindingUtil.glideAvatarSrc(viewHolder.avatarImageView, avatarUrl)
            viewHolder.avatarImageView.setOnClickListener {
                val fragment = ProfileFragment.newInstance(user.id, avatarUrl, user)
                FragmentHelper.addFragment(requireContext(), fragment, user.id)
            }
        }
        viewHolder.bodyTextView.text = item.content?.getSpannableStringBuilder(viewHolder.itemView.context)
        viewHolder.relativeTimeTextView.text = DateUtil.getShortDateStr(viewHolder.itemView.context, item.createdAt)
    }

    override fun getItemLayout(): Int = R.layout.fragment_message_item
    override val itemNameRes: Int = R.string.messages

    override fun onClickSegmentListener(
        viewHolder: BaseListRecyclerViewAdapter.SegmentViewHolder,
        itemWrapper: PageableItemWrapper.Pager<Message>
    ) {
        viewModel.loadMoreItems()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = ListWithToolbarBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener {
            backToPrevFragment()
        }
        binding.toolbar.setTitle(title)
        binding.toolbar.setOnClickListener {
            val ctx = context ?: return@setOnClickListener
            getRecyclerView(requireView()).layoutManager?.startSmoothScroll(SmoothScroller(ctx))
        }
    }

    class ChannelMessagesViewModel(
        private val channelId: String,
        private val getMessagesUseCase: GetMessagesUseCase
    ) : BaseListViewModel<Message>() {
        override suspend fun getItems(requestPager: PageableItemWrapper.Pager<Message>?): PnutResponse<List<Message>> {
            val pagination = requestPager?.let { PaginationParam.createFromPager(it) } ?: PaginationParam()
            return getMessagesUseCase.run(GetMessagesInputData(channelId, pagination)).messages
        }

        class Factory(
            private val channelId: String,
            private val getMessagesUseCase: GetMessagesUseCase
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChannelMessagesViewModel(channelId, getMessagesUseCase) as T
            }
        }
    }

    companion object {
        fun newInstance(channelId: String, title: String) = ChannelMessagesFragment().apply {
            arguments = Bundle().apply {
                putString(BundleKey.ChannelId.name, channelId)
                putString(BundleKey.Title.name, title)
            }
        }
    }
}
