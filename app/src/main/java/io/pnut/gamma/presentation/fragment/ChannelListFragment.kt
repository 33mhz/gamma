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
import io.pnut.gamma.domain.model.params.single.SearchChannelParam
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.usecases.GetChannelsUseCase
import io.pnut.gamma.domain.usecases.UpdateMarkerUseCase
import io.pnut.gamma.presentation.adapter.BaseListRecyclerViewAdapter
import io.pnut.gamma.presentation.util.BindingUtil
import io.pnut.gamma.presentation.util.navigateTo
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
    private val keyword: String? by lazy {
        arguments?.getString(BundleKey.Keyword.name)
    }
    private val categories: String? by lazy {
        arguments?.getString(BundleKey.Categories.name)
    }

    @Inject
    lateinit var getChannelUseCase: GetChannelsUseCase

    @Inject
    lateinit var updateMarkerUseCase: UpdateMarkerUseCase

    @Inject
    lateinit var accountRepository: IAccountRepository

    override fun getFragmentLayout(): Int = R.layout.fragment_base_list
    override fun getRecyclerView(view: View): RecyclerView = view.findViewById(R.id.baseList)
    override fun getSwipeRefreshLayout(view: View): SwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
    override val viewModel: BaseListViewModel<Channel> by lazy {
        ViewModelProvider(
            this, ChannelListViewModel.Factory(channelType, keyword, categories, getChannelUseCase, accountRepository)
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
        val title = getChannelTitle(item)
        val usernames = getChannelUsernames(item)
        val fragment = ChannelMessagesFragment.newInstance(item.id, title, item.type, usernames)
        navigateTo(fragment, item.id)
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
        val title = getChannelTitle(item)
        viewHolder.binding.screenNameTextView.text = title

        val description = chatSettings?.description?.text
        viewHolder.binding.descriptionTextView.text = description
        viewHolder.binding.descriptionTextView.visibility = if (description.isNullOrBlank()) View.GONE else View.VISIBLE

        val user = item.recentMessage?.user ?: item.user
        viewHolder.binding.userTypeIconImageView.apply {
            when (user?.type) {
                User.AccountType.BOT -> {
                    visibility = View.VISIBLE
                    setImageResource(R.drawable.ic_robot_24dp)
                }
                User.AccountType.FEED -> {
                    visibility = View.VISIBLE
                    setImageResource(R.drawable.ic_newspaper_24dp)
                }
                else -> {
                    visibility = View.GONE
                }
            }
        }
        viewHolder.binding.messageAuthorHandleTextView.text = user?.username?.let { "@$it" }
        viewHolder.binding.messageAuthorNameTextView.text = user?.name
        val hasUser = user != null
        viewHolder.binding.messageAuthorHandleTextView.visibility = if (hasUser) View.VISIBLE else View.GONE
        viewHolder.binding.messageAuthorNameTextView.visibility = if (hasUser) View.VISIBLE else View.GONE

        user?.let { u ->
            val avatarUrl = User.getAvatarUrl(u, User.AvatarSize.Small)
            BindingUtil.loadAvatar(viewHolder.binding.avatarImageView, u, User.AvatarSize.Small)
            viewHolder.binding.avatarImageView.setOnClickListener {
                val fragment = ProfileFragment.newInstance(u.id, avatarUrl, u)
                navigateTo(fragment, u.id)
            }
        }

        viewHolder.binding.bodyTextView.text = item.recentMessage?.content?.getSpannableStringBuilder(viewHolder.itemView.context)
        viewHolder.binding.relativeTimeTextView.text = DateUtil.getShortDateStr(viewHolder.itemView.context, item.recentMessage?.createdAt)
        viewHolder.binding.unreadIcon.visibility = if (item.hasUnread) View.VISIBLE else View.GONE
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

    protected fun getChannelTitle(channel: Channel): String {
        val chatSettings = ChatSettings.getChatSettings(channel.raw)
        if (chatSettings != null) return chatSettings.name!!

        val myId = accountRepository.getStoredIds().firstOrNull()
        val users = channel.acl.write.users?.filter { it.id != myId }
        if (!users.isNullOrEmpty()) {
            return '@' + users.joinToString(", @") { it.username }
        }

        return channel.user?.username ?: "Channel ${channel.id}"
    }

    protected fun getChannelUsernames(channel: Channel): ArrayList<String> {
        val myId = accountRepository.getStoredIds().firstOrNull()
        val users = channel.acl.write.users?.filter { it.id != myId }
        return if (!users.isNullOrEmpty()) {
            ArrayList(users.map { it.username })
        } else {
            ArrayList()
        }
    }

    protected enum class BundleKey { ChannelType, Keyword, Categories }

    class ChannelListViewModel(
        private val channelType: ChannelType,
        private val keyword: String?,
        private val categories: String?,
        private val getChannelsUseCase: GetChannelsUseCase,
        private val accountRepository: IAccountRepository
    ) : BaseListViewModel<Channel>() {
        override suspend fun getItems(requestPager: PageableItemWrapper.Pager<Channel>?): PnutResponse<List<Channel>> {
            val params = GetChannelsParam().also { getChannelParams ->
                val type = channelType.value
                val isPublic = if (channelType == ChannelType.PublicChat) true else null
                val ownerId = if (channelType == ChannelType.Yours) accountRepository.getStoredIds().firstOrNull() else null
                getChannelParams.add(GeneralChannelParam(includeRecentMessage = true, channelTypes = type, ownerId = ownerId, isPublic = isPublic))
                if (channelType == ChannelType.Search) {
                    if (keyword != null || categories != null) {
                        getChannelParams.add(SearchChannelParam(keyword = keyword ?: "", categories = categories, order = "activity"))
                    }
                }
                requestPager?.let { getChannelParams.add(PaginationParam.createFromPager(it)) }
            }
            val getChannelsOutputData =
                getChannelsUseCase.run(GetChannelsInputData(channelType, params))
            return getChannelsOutputData.channels
        }

        class Factory(
            private val channelType: ChannelType,
            private val keyword: String?,
            private val categories: String?,
            private val getChannelsUseCase: GetChannelsUseCase,
            private val accountRepository: IAccountRepository
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChannelListViewModel(channelType, keyword, categories, getChannelsUseCase, accountRepository) as T
            }
        }
    }

    companion object {
        fun subscribedChannels() = newInstance(ChannelType.Chat)
        fun yoursChannels() = newInstance(ChannelType.Yours)
        fun publicChannels() = newInstance(ChannelType.PublicChat)

        fun newInstance(channelType: ChannelType) = ChannelListFragment().apply {
            arguments = Bundle().apply {
                putSerializable(BundleKey.ChannelType.name, channelType)
            }
        }
    }

    @AndroidEntryPoint
    class SearchChannelsFragment : ChannelListFragment() {
        companion object {
            fun newInstance(keyword: String) = SearchChannelsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(BundleKey.ChannelType.name, ChannelType.Search)
                    putString(BundleKey.Keyword.name, keyword)
                }
            }

            fun newInstance(keyword: String, categories: String) = SearchChannelsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(BundleKey.ChannelType.name, ChannelType.Search)
                    putString(BundleKey.Keyword.name, keyword)
                    putString(BundleKey.Categories.name, categories)
                }
            }
        }
    }

}
