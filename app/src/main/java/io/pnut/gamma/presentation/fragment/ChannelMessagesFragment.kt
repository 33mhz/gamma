package io.pnut.gamma.presentation.fragment

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.R
import io.pnut.gamma.databinding.ListWithToolbarBinding
import io.pnut.gamma.domain.entity.Message
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.entity.raw.Broadcast
import io.pnut.gamma.domain.entity.raw.ChannelInvite
import io.pnut.gamma.domain.entity.raw.OEmbed
import androidx.lifecycle.lifecycleScope
import io.pnut.gamma.domain.model.PageableItemWrapper
import io.pnut.gamma.presentation.activity.ComposeMessageActivity
import io.pnut.gamma.domain.model.ThumbAndFull
import io.pnut.gamma.domain.model.params.composed.GetPostsParam
import io.pnut.gamma.domain.model.StreamType
import io.pnut.gamma.domain.model.io.GetMessagesInputData
import io.pnut.gamma.domain.model.io.GetPostInputData
import io.pnut.gamma.domain.model.params.single.PaginationParam
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.entity.Channel
import io.pnut.gamma.domain.usecases.DeleteMessageUseCase
import io.pnut.gamma.domain.usecases.GetChannelUseCase
import io.pnut.gamma.domain.usecases.GetMessagesUseCase
import io.pnut.gamma.domain.usecases.GetPostUseCase
import io.pnut.gamma.presentation.activity.PhotoViewActivity
import io.pnut.gamma.presentation.adapter.BaseListRecyclerViewAdapter
import io.pnut.gamma.presentation.adapter.MessageViewHolder
import io.pnut.gamma.presentation.adapter.ThumbnailViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.pnut.gamma.presentation.util.BindingUtil
import io.pnut.gamma.presentation.util.DateUtil
import io.pnut.gamma.presentation.util.EntityOnTouchListener
import io.pnut.gamma.presentation.util.navigateTo
import io.pnut.gamma.presentation.util.SmoothScroller
import io.pnut.gamma.presentation.util.Util
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
open class ChannelMessagesFragment : BaseListFragment<Message, MessageViewHolder>(),
    BaseListRecyclerViewAdapter.IBaseList<Message, MessageViewHolder>,
    DeleteMessageDialogFragment.Callback, SimpleBottomSheetMenuFragment.Callback,
    Util.DrawerContentFragment, ThumbnailViewPagerAdapter.Listener {

    override val menuItemId: Int
        get() = if (isPm) R.id.privateMessages else R.id.channels

    protected enum class BundleKey { ChannelId, Title, ChannelType, Usernames }

    val channelId by lazy {
        arguments?.getString(BundleKey.ChannelId.name) ?: throw IllegalArgumentException("channelId is required")
    }

    val title by lazy {
        arguments?.getString(BundleKey.Title.name) ?: ""
    }

    private val channelType by lazy {
        arguments?.getString(BundleKey.ChannelType.name)
    }

    val isPm: Boolean
        get() = channelType == io.pnut.gamma.domain.model.ChannelType.PM.value

    fun getUsernames(): ArrayList<String>? {
        val argumentsUsernames = arguments?.getStringArrayList(BundleKey.Usernames.name)
        if (!argumentsUsernames.isNullOrEmpty()) return argumentsUsernames

        (viewModel as? ChannelMessagesViewModel)?.currentChannel?.value?.let { channel ->
            val myId = accountRepository.getStoredIds().firstOrNull()
            val users = channel.acl.write.users?.filter { it.id != myId }
            if (!users.isNullOrEmpty()) {
                return ArrayList(users.map { it.username })
            }
        }

        return null
    }

    @Inject
    lateinit var getChannelUseCase: GetChannelUseCase

    @Inject
    lateinit var getMessagesUseCase: GetMessagesUseCase

    @Inject
    lateinit var deleteMessageUseCase: DeleteMessageUseCase

    @Inject
    lateinit var getPostUseCase: GetPostUseCase

    @Inject
    lateinit var accountRepository: IAccountRepository

    private var previousViewHolderItem: ViewHolderItem? = null

    data class ViewHolderItem(
        val viewHolder: MessageViewHolder,
        val message: Message,
        val position: Int,
        val itemWrapper: PageableItemWrapper<Message>
    )

    override val reverse = true

    override fun getFragmentLayout(): Int = R.layout.list_with_toolbar
    override fun getRecyclerView(view: View): RecyclerView = ListWithToolbarBinding.bind(view).itemList
    override fun getSwipeRefreshLayout(view: View): SwipeRefreshLayout = ListWithToolbarBinding.bind(view).swipeRefreshLayout

    override val viewModel: BaseListViewModel<Message> by lazy {
        ViewModelProvider(
            this, ChannelMessagesViewModel.Factory(channelId, getMessagesUseCase, getChannelUseCase)
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
        val clickedItemPosition = viewHolder.bindingAdapterPosition
        if (clickedItemPosition == RecyclerView.NO_POSITION) return

        val previousViewHolderItemLocal = previousViewHolderItem
        previousViewHolderItem = when {
            previousViewHolderItemLocal != null -> when {
                previousViewHolderItemLocal.message != item -> {
                    val position = viewModel.items.indexOf(previousViewHolderItemLocal.itemWrapper)
                    if (position >= 0 && position != clickedItemPosition) {
                        adapter.notifyItemChanged(position)
                    }
                    ViewHolderItem(viewHolder, item, clickedItemPosition, itemWrapper)
                }
                else -> null
            }
            else -> ViewHolderItem(viewHolder, item, clickedItemPosition, itemWrapper)
        }
        adapter.notifyItemChanged(clickedItemPosition)
    }

    override fun onBindViewHolder(
        item: Message,
        viewHolder: MessageViewHolder,
        position: Int,
        isMainItem: Boolean
    ) {
        val context = viewHolder.itemView.context
        val isDeleted = item.isDeleted == true
        viewHolder.itemView.alpha = if (isDeleted) 0.5f else 1f

        item.user?.let { user ->
            viewHolder.userTypeIconImageView.apply {
                when (user.type) {
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
            viewHolder.screenNameTextView.text = user.name
            viewHolder.handleNameTextView.text = context.getString(R.string.user_name_format, user.username)
            val avatarUrl = User.getAvatarUrl(user, User.AvatarSize.Small)
            BindingUtil.loadAvatar(viewHolder.avatarImageView, user, User.AvatarSize.Small)
            viewHolder.avatarImageView.setOnClickListener {
                val fragment = ProfileFragment.newInstance(user.id, avatarUrl, user)
                navigateTo(fragment, user.id)
            }
        }

        val isNsfw = item.nsfwMask
        viewHolder.nsfwMaskLayout.visibility = getVisibility(isNsfw)
        viewHolder.showNsfwButton.setOnClickListener {
            item.nsfwMask = false
            adapter.notifyItemChanged(viewHolder.bindingAdapterPosition)
        }

        val isSpoiler = item.spoilerMask
        viewHolder.spoilerMaskLayout.visibility = getVisibility(isSpoiler)
        val spoilerTopic = item.spoiler?.topic ?: ""
        viewHolder.showSpoilerButton.text = context.getString(R.string.show_spoiler, spoilerTopic)
        viewHolder.showSpoilerButton.setOnClickListener {
            item.spoilerMask = false
            adapter.notifyItemChanged(viewHolder.bindingAdapterPosition)
        }
        viewHolder.contentsWrapperLayout.visibility = getVisibility(item.showContents)

        viewHolder.bodyTextView.text = item.content?.getSpannableStringBuilder(context)
        
        val raw = item.raw
        val invite = ChannelInvite.getChannelInvite(raw)
        viewHolder.goToChannelButton.visibility = getVisibility(invite != null)
        if (invite != null) {
            val channelLabel = invite.name ?: invite.channelId
            val prefix = if (invite.name != null) { getString(R.string.go_to) } else { getString(R.string.channel) + " " + getString(R.string.go_to) }
            val text = "$prefix $channelLabel"
            val spannable = SpannableString(text)
            spannable.setSpan(
                StyleSpan(Typeface.ITALIC),
                prefix.length + 1,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            viewHolder.goToChannelButton.text = spannable
            viewHolder.goToChannelButton.setOnClickListener {
                val fragment =
                    newInstance(invite.channelId, channelLabel, null, null)
                navigateTo(fragment, invite.channelId)
            }
        }

        viewHolder.relativeTimeTextView.text = DateUtil.getShortDateStr(context, item.createdAt)

        val photos = OEmbed.Photo.getPhotos(raw)
        if (photos.isNotEmpty()) {
            viewHolder.thumbnailViewPagerFrameLayout.visibility = View.VISIBLE
            viewHolder.thumbnailViewPager.adapter = ThumbnailViewPagerAdapter(photos, this)
            TabLayoutMediator(
                viewHolder.thumbnailTabLayout,
                viewHolder.thumbnailViewPager
            ) { _: TabLayout.Tab, _: Int ->
            }.attach()
            viewHolder.thumbnailTabLayout.visibility =
                if (photos.size == 1) View.GONE else View.VISIBLE
        } else {
            viewHolder.thumbnailViewPagerFrameLayout.visibility = View.GONE
            viewHolder.thumbnailViewPager.adapter = null
        }

        val isExpanded = previousViewHolderItem?.message == item
        viewHolder.foregroundActionsLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
        
        if (isExpanded) {
            val recipients = getReplyRecipients(item)
            val replyIcon = if (recipients.size > 1) R.drawable.ic_reply_all_black_24dp else R.drawable.ic_reply_black_24dp
            viewHolder.replyButton.setImageResource(replyIcon)

            val onReplyClick = View.OnClickListener {
                showReplyCompose(item)
            }
            val onReplyLongClick = View.OnLongClickListener {
                showReplyCompose(item, replyAll = false)
                true
            }

            viewHolder.replyButton.setOnClickListener(onReplyClick)
            viewHolder.replyButton.setOnLongClickListener(onReplyLongClick)
            
            val isMyMessage = item.user?.id == accountRepository.getStoredIds().firstOrNull()
            viewHolder.deleteButton.visibility = if (isMyMessage) View.VISIBLE else View.GONE
            viewHolder.deleteButton.setOnClickListener {
                confirmDeleteMessage(position, item)
            }
            
            viewHolder.threadButton.setOnClickListener {
                showThread(item)
            }
            
            val broadcast = item.raw?.get(Broadcast.TYPE)?.firstOrNull() as? Broadcast
            viewHolder.broadcastButton.visibility = getVisibility(broadcast != null)
            viewHolder.broadcastButton.imageTintList = ColorStateList.valueOf(Util.getPrimaryColor(context))
            viewHolder.broadcastButton.setOnClickListener {
                broadcast?.let { showPostThread(it.id) }
            }

            viewHolder.moreButton.setOnClickListener {
                showMoreMenu(item)
            }
        }
    }

    private var selectedMessage: Message? = null

    private fun showMoreMenu(message: Message) {
        selectedMessage = message
        val fragment = SimpleBottomSheetMenuFragment.newInstance(R.menu.post_item_more)
        fragment.show(childFragmentManager, DialogKey.More.name)
    }

    private enum class DialogKey { More }

    override fun onMenuShow(menu: Menu, tag: String?) {
        if (tag == DialogKey.More.name) {
            menu.findItem(R.id.menuShare)?.isVisible = false
            menu.findItem(R.id.menuReport)?.isVisible = false
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem, tag: String?) {
        when (tag) {
            DialogKey.More.name -> handleMessageMoreMenu(menuItem)
        }
    }

    private fun handleMessageMoreMenu(menuItem: MenuItem) {
        val message = selectedMessage ?: return
        when (menuItem.itemId) {
            R.id.menuProfile -> {
                val user = message.user ?: return
                val fragment = ProfileFragment.newInstance(user.id, User.getAvatarUrl(user), user)
                navigateTo(fragment, user.id)
            }
        }
    }

    private fun showReplyCompose(message: Message, replyAll: Boolean = true) {
        val channel = (viewModel as? ChannelMessagesViewModel)?.currentChannel?.value
        val intent = ComposeMessageActivity.newIntent(requireContext(), channelId = channelId, replyTarget = message, channel = channel, replyAll = replyAll)
        startActivity(intent)
    }

    private fun getReplyRecipients(message: Message): List<String> {
        val replyTargetUserUsername = message.username
        val mentions = message.content?.entities?.mentions?.map { it.text }?.toMutableList() ?: mutableListOf()
        if (replyTargetUserUsername != null) {
            mentions.add(0, replyTargetUserUsername)
        }
        val currentUsername = accountRepository.getDefaultAccount()?.screenName
        return mentions.distinctBy { it.lowercase() }
            .filterNot { it.equals(currentUsername, ignoreCase = true) }
    }

    private fun confirmDeleteMessage(position: Int, message: Message) {
        val fragment = DeleteMessageDialogFragment.newInstance(position, message)
        fragment.show(childFragmentManager, "DeleteMessage")
    }

    private fun showThread(message: Message) {
        val fragment = MessageThreadFragment.newInstance(channelId, message)
        addFragment(fragment, "MessageThread_${message.threadId}")
    }

    private fun showPostThread(postId: String) {
        lifecycleScope.launch {
            try {
                val res = getPostUseCase.run(GetPostInputData(StreamType.Posts(listOf(postId)), GetPostsParam()))
                res.res.data.firstOrNull()?.let { post ->
                    val fragment = ThreadFragment.newInstance(post)
                    addFragment(fragment, "Thread_$postId")
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun getVisibility(b: Boolean): Int = if (b) View.VISIBLE else View.GONE

    override fun onClick(path: String, position: Int, items: List<String>) {
        val newIntent = PhotoViewActivity.photoViewInstance(
            requireContext(),
            items.map { ThumbAndFull(it, it) },
            position
        )
        startActivity(newIntent)
    }

    override fun getItemLayout(): Int = R.layout.fragment_message_item
    override val itemNameRes: Int = R.string.messages

    override fun ok(position: Int, message: Message) {
        lifecycleScope.launch {
            try {
                deleteMessageUseCase.run(io.pnut.gamma.domain.model.io.DeleteMessageInputData(channelId, message.id))
                adapter.removeItem(PageableItemWrapper.Item(message))
                previousViewHolderItem = null
            } catch (_: Exception) {
                // Show error
            }
        }
    }

    override fun cancel() {
    }

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
            androidx.appcompat.app.AlertDialog.Builder(ctx)
                .setMessage(title)
                .setPositiveButton(android.R.string.ok, null)
                .show()
            getRecyclerView(requireView()).layoutManager?.startSmoothScroll(SmoothScroller(ctx))
        }

        binding.toolbar.inflateMenu(R.menu.channel_messages_menu)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.channelInfo -> {
                    val dialog = ChannelInfoDialogFragment.newInstance(channelId, isPm)
                    dialog.show(childFragmentManager, "ChannelInfo")
                    true
                }
                else -> false
            }
        }

        if (isPm && getUsernames().isNullOrEmpty()) {
            (viewModel as? ChannelMessagesViewModel)?.fetchChannel()
        }
    }

    class ChannelMessagesViewModel(
        private val channelId: String,
        private val getMessagesUseCase: GetMessagesUseCase,
        private val getChannelUseCase: GetChannelUseCase
    ) : BaseListViewModel<Message>() {
        val currentChannel = MutableLiveData<Channel>()

        override suspend fun getItems(requestPager: PageableItemWrapper.Pager<Message>?): PnutResponse<List<Message>> {
            val pagination = requestPager?.let { PaginationParam.createFromPager(it) } ?: PaginationParam()
            return getMessagesUseCase.run(GetMessagesInputData(channelId, pagination)).messages
        }

        fun fetchChannel() {
            viewModelScope.launch {
                runCatching {
                    getChannelUseCase.run(channelId)
                }.onSuccess {
                    currentChannel.value = it.data
                }
            }
        }

        class Factory(
            private val channelId: String,
            private val getMessagesUseCase: GetMessagesUseCase,
            private val getChannelUseCase: GetChannelUseCase
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChannelMessagesViewModel(channelId, getMessagesUseCase, getChannelUseCase) as T
            }
        }
    }

    companion object {
        fun newInstance(channelId: String, title: String, channelType: String? = null, usernames: ArrayList<String>? = null) = ChannelMessagesFragment().apply {
            arguments = Bundle().apply {
                putString(BundleKey.ChannelId.name, channelId)
                putString(BundleKey.Title.name, title)
                putString(BundleKey.ChannelType.name, channelType)
                putStringArrayList(BundleKey.Usernames.name, usernames)
            }
        }
    }
}
