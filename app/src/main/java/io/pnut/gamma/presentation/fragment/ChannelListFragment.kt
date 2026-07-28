package io.pnut.gamma.presentation.fragment

import android.os.Bundle
import androidx.core.os.BundleCompat
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.pnut.gamma.R
import io.pnut.gamma.domain.entity.Channel
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.raw.ChatSettings
import io.pnut.gamma.domain.model.ChannelType
import io.pnut.gamma.domain.model.PageableItemWrapper
import io.pnut.gamma.domain.model.io.GetChannelsInputData
import io.pnut.gamma.domain.model.io.UpdateMarkerInputData
import io.pnut.gamma.domain.model.params.composed.GetChannelsParam
import io.pnut.gamma.domain.model.params.single.GeneralChannelParam
import io.pnut.gamma.domain.model.params.single.PaginationParam
import io.pnut.gamma.domain.usecases.GetChannelsUseCase
import io.pnut.gamma.domain.usecases.UpdateMarkerUseCase
import io.pnut.gamma.presentation.adapter.BaseListRecyclerViewAdapter
import io.pnut.gamma.presentation.util.BindingUtil
import io.pnut.gamma.presentation.util.FragmentHelper
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.presentation.util.DateUtil
import io.pnut.gamma.databinding.FragmentChannelPostItemBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
open class ChannelListFragment : BaseListFragment<Channel, ChannelListFragment.ChannelViewHolder>(),
    BaseListRecyclerViewAdapter.IBaseList<Channel, ChannelListFragment.ChannelViewHolder> {
    private val channelType: ChannelType by lazy {
        arguments?.let { BundleCompat.getSerializable(it, BundleKey.ChannelType.name, ChannelType::class.java) }
            ?: ChannelType.Chat
    }

    @Inject
    lateinit var getChannelUseCase: GetChannelsUseCase

    @Inject
    lateinit var updateMarkerUseCase: UpdateMarkerUseCase

    override fun getFragmentLayout(): Int = R.layout.fragment_base_list
    override fun getRecyclerView(view: View): RecyclerView = view.findViewById(R.id.baseList)
    override fun getSwipeRefreshLayout(view: View): SwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
    override val viewModel: BaseListViewModel<Channel> by lazy {
        ViewModelProvider(
            this, ChannelListViewModel.Factory(channelType, getChannelUseCase)
        )[ChannelListViewModel::class.java]
    }

    override val baseListListener: BaseListRecyclerViewAdapter.IBaseList<Channel, ChannelViewHolder> by lazy { this }

    override fun createViewHolder(mView: View, viewType: Int): ChannelViewHolder {
        return ChannelViewHolder(mView)
    }

    override fun onClickItemListener(
        viewHolder: ChannelViewHolder,
        item: Channel,
        itemWrapper: PageableItemWrapper<Channel>
    ) {
        if (item.hasUnread && item.recentMessageId != null) {
            lifecycleScope.launch {
                updateMarkerUseCase.run(UpdateMarkerInputData(item.id, item.recentMessageId))
            }
        }
        val chatSettings = ChatSettings.getChatSettings(item.raw)
        val title = chatSettings?.name ?: (item.user?.username ?: "Channel ${item.id}")
        val fragment = ChannelMessagesFragment.newInstance(item.id, title)
        FragmentHelper.addFragment(requireContext(), fragment, item.id)
    }

    override fun onBindViewHolder(
        item: Channel,
        viewHolder: ChannelViewHolder,
        position: Int,
        isMainItem: Boolean
    ) {
        onBindChannelViewHolder(item, viewHolder, position, isMainItem)
    }

    open fun onBindChannelViewHolder(
        item: Channel,
        viewHolder: ChannelViewHolder,
        position: Int,
        isMainItem: Boolean
    ) {
        val chatSettings = ChatSettings.getChatSettings(item.raw)
        viewHolder.binding.screenNameTextView.text = chatSettings?.name ?: (item.user?.username ?: "Channel ${item.id}")
        
        val user = item.recentMessage?.user ?: item.user
        user?.let {
            viewHolder.binding.handleNameTextView.text = viewHolder.itemView.context.getString(R.string.user_name_format, it.username)
            BindingUtil.glideAvatarSrc(viewHolder.binding.avatarImageView, User.getAvatarUrl(it, User.AvatarSize.Small))
        }

        viewHolder.binding.bodyTextView.text = item.recentMessage?.content?.getSpannableStringBuilder(viewHolder.itemView.context)
        viewHolder.binding.relativeTimeTextView.text = DateUtil.getShortDateStr(viewHolder.itemView.context, item.recentMessage?.createdAt)
    }

    override fun getItemLayout(): Int = R.layout.fragment_channel_post_item

    override val itemNameRes: Int = R.string.chat_rooms

    override fun onClickSegmentListener(
        viewHolder: BaseListRecyclerViewAdapter.SegmentViewHolder,
        itemWrapper: PageableItemWrapper.Pager<Channel>
    ) {
        viewModel.loadMoreItems()
    }

    open class ChannelViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val binding: FragmentChannelPostItemBinding by lazy { FragmentChannelPostItemBinding.bind(mView) }
    }

    protected enum class BundleKey { ChannelType }

    class ChannelListViewModel(
        private val channelType: ChannelType,
        private val getChannelsUseCase: GetChannelsUseCase
    ) : BaseListViewModel<Channel>() {
        override suspend fun getItems(requestPager: PageableItemWrapper.Pager<Channel>?): PnutResponse<List<Channel>> {
            val params = GetChannelsParam().also { getChannelParams ->
                val type = if (channelType == ChannelType.PublicChat) null else channelType.value
                getChannelParams.add(GeneralChannelParam(includeRecentMessage = true, channelTypes = type))
                requestPager?.let { getChannelParams.add(PaginationParam.createFromPager(it)) }
            }
            val getChannelsOutputData =
                getChannelsUseCase.run(GetChannelsInputData(channelType, params))
            return getChannelsOutputData.channels
        }

        class Factory(
            private val channelType: ChannelType,
            private val getChannelsUseCase: GetChannelsUseCase
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChannelListViewModel(channelType, getChannelsUseCase) as T
            }
        }
    }

    companion object {
        fun pmChannels() = newInstance(ChannelType.PM)
        fun chatChannels() = newInstance(ChannelType.Chat)
        fun subscribedChannels() = newInstance(ChannelType.Chat)
        fun topicalChannels() = newInstance(ChannelType.PublicChat)

        private fun newInstance(channelType: ChannelType) = ChannelListFragment().apply {
            arguments = Bundle().apply {
                putSerializable(BundleKey.ChannelType.name, channelType)
            }
        }
    }

}
