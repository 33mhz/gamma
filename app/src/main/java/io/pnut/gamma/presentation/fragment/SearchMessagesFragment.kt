package io.pnut.gamma.presentation.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.R
import io.pnut.gamma.domain.entity.Message
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.entity.raw.OEmbed
import io.pnut.gamma.domain.model.PageableItemWrapper
import io.pnut.gamma.domain.model.ThumbAndFull
import io.pnut.gamma.domain.model.params.composed.GetChannelsParam
import io.pnut.gamma.domain.model.params.single.GeneralChannelParam
import io.pnut.gamma.domain.model.params.single.PaginationParam
import io.pnut.gamma.domain.model.params.single.SearchChannelParam
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.usecases.SearchMessagesUseCase
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
import javax.inject.Inject

@AndroidEntryPoint
class SearchMessagesFragment : BaseListFragment<Message, MessageViewHolder>(),
    BaseListRecyclerViewAdapter.IBaseList<Message, MessageViewHolder>,
    ThumbnailViewPagerAdapter.Listener {

    private val keyword by lazy {
        arguments?.getString(BundleKey.Keyword.name, "").orEmpty()
    }

    @Inject
    lateinit var searchMessagesUseCase: SearchMessagesUseCase

    @Inject
    lateinit var accountRepository: IAccountRepository

    override fun getFragmentLayout(): Int = R.layout.fragment_base_list
    override fun getRecyclerView(view: View): RecyclerView = view.findViewById(R.id.baseList)
    override fun getSwipeRefreshLayout(view: View): SwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

    override val viewModel: BaseListViewModel<Message> by lazy {
        ViewModelProvider(
            this, SearchMessagesViewModel.Factory(keyword, searchMessagesUseCase)
        )[SearchMessagesViewModel::class.java]
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
        // For search results, we might just want to open the thread or channel
        val fragment = MessageThreadFragment.newInstance(item.channelId, item)
        navigateTo(fragment, "MessageThread_${item.threadId}")
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
            viewHolder.screenNameTextView.text = user.name
            viewHolder.handleNameTextView.text = context.getString(R.string.user_name_format, user.username)
            val avatarUrl = User.getAvatarUrl(user, User.AvatarSize.Small)
            BindingUtil.glideAvatarSrc(viewHolder.avatarImageView, avatarUrl)
            viewHolder.avatarImageView.setOnClickListener {
                val fragment = ProfileFragment.newInstance(user.id, avatarUrl, user)
                navigateTo(fragment, user.id)
            }
        }

        val isNsfw = item.nsfwMask
        viewHolder.nsfwMaskLayout.visibility = if (isNsfw) View.VISIBLE else View.GONE
        viewHolder.showNsfwButton.setOnClickListener {
            item.nsfwMask = false
            adapter.notifyItemChanged(viewHolder.bindingAdapterPosition)
        }

        val isSpoiler = item.spoilerMask
        viewHolder.spoilerMaskLayout.visibility = if (isSpoiler) View.VISIBLE else View.GONE
        val spoilerTopic = item.spoiler?.topic ?: ""
        viewHolder.showSpoilerButton.text = context.getString(R.string.show_spoiler, spoilerTopic)
        viewHolder.showSpoilerButton.setOnClickListener {
            item.spoilerMask = false
            adapter.notifyItemChanged(viewHolder.bindingAdapterPosition)
        }
        viewHolder.contentsWrapperLayout.visibility = if (item.showContents) View.VISIBLE else View.GONE

        viewHolder.bodyTextView.text = item.content?.getSpannableStringBuilder(context)
        viewHolder.relativeTimeTextView.text = DateUtil.getShortDateStr(context, item.createdAt)

        val raw = item.raw
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

        // Hide expanded actions for search results to keep it simple
        viewHolder.foregroundActionsLayout.visibility = View.GONE
    }

    override fun getItemLayout(): Int = R.layout.fragment_message_item
    override val itemNameRes: Int = R.string.messages

    override fun onClick(path: String, position: Int, items: List<String>) {
        val newIntent = PhotoViewActivity.photoViewInstance(
            requireContext(),
            items.map { ThumbAndFull(it, it) },
            position
        )
        startActivity(newIntent)
    }

    override fun onClickSegmentListener(
        viewHolder: BaseListRecyclerViewAdapter.SegmentViewHolder,
        itemWrapper: PageableItemWrapper.Pager<Message>
    ) {
        viewModel.loadMoreItems()
    }

    class SearchMessagesViewModel(
        private val keyword: String,
        private val searchMessagesUseCase: SearchMessagesUseCase
    ) : BaseListViewModel<Message>() {
        override suspend fun getItems(requestPager: PageableItemWrapper.Pager<Message>?): PnutResponse<List<Message>> {
            val params = GetChannelsParam().also {
                it.add(SearchChannelParam(
                    keyword = keyword,
                    order = "id",
                    channelIds = "pm",
                ))
                it.add(GeneralChannelParam(
                    channelTypes = io.pnut.gamma.domain.model.ChannelType.PM.value
                ))
                requestPager?.let { pager -> it.add(PaginationParam.createFromPager(pager)) }
            }
            return searchMessagesUseCase.run(SearchMessagesUseCase.InputData(params)).messages
        }

        class Factory(
            private val keyword: String,
            private val searchMessagesUseCase: SearchMessagesUseCase
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SearchMessagesViewModel(keyword, searchMessagesUseCase) as T
            }
        }
    }

    private enum class BundleKey { Keyword }

    companion object {
        fun newInstance(keyword: String) = SearchMessagesFragment().apply {
            arguments = Bundle().apply {
                putString(BundleKey.Keyword.name, keyword)
            }
        }
    }
}
