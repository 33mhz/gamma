package io.pnut.gamma.presentation.fragment

import android.R as Rr
import io.pnut.gamma.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.core.os.BundleCompat
import android.transition.Transition
import android.transition.TransitionInflater
import android.transition.TransitionSet
import android.view.*
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.broadcast.PostReceiver
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.Poll
import io.pnut.gamma.domain.entity.PollLikeValue
import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.entity.raw.LongPost
import io.pnut.gamma.domain.entity.raw.OEmbed
import io.pnut.gamma.domain.entity.raw.PollNotice
import io.pnut.gamma.domain.model.PageableItemWrapper
import io.pnut.gamma.domain.model.StreamType
import io.pnut.gamma.domain.model.ThumbAndFull
import io.pnut.gamma.domain.model.io.CachePostInputData
import io.pnut.gamma.domain.model.io.GetCachedPostListInputData
import io.pnut.gamma.domain.model.io.GetPollInputData
import io.pnut.gamma.domain.model.io.GetPostInputData
import io.pnut.gamma.domain.model.io.VoteInputData
import io.pnut.gamma.domain.model.params.composed.GetPostsParam
import io.pnut.gamma.domain.model.params.single.GeneralPostParam
import io.pnut.gamma.domain.model.params.single.PaginationParam
import io.pnut.gamma.domain.usecases.CachePostUseCase
import io.pnut.gamma.domain.usecases.GetCachedPostListUseCase
import io.pnut.gamma.domain.usecases.GetPollUseCase
import io.pnut.gamma.domain.usecases.GetPostUseCase
import io.pnut.gamma.domain.usecases.VoteUseCase
import io.pnut.gamma.presentation.activity.ComposePostActivity
import io.pnut.gamma.presentation.activity.PhotoViewActivity
import io.pnut.gamma.presentation.adapter.BaseListRecyclerViewAdapter
import io.pnut.gamma.presentation.adapter.PollOptionsAdapter
import io.pnut.gamma.presentation.adapter.ReactionUsersAdapter
import io.pnut.gamma.presentation.adapter.ThumbnailViewPagerAdapter
import io.pnut.gamma.presentation.util.BindingUtil
import io.pnut.gamma.presentation.util.DateUtil
import io.pnut.gamma.presentation.util.EntityOnTouchListener
import io.pnut.gamma.presentation.util.FragmentHelper
import io.pnut.gamma.presentation.util.PostTouchHelperCallback
import io.pnut.gamma.presentation.util.Util
import io.pnut.gamma.presentation.view.LinkableTextView
import io.pnut.gamma.service.PostWorker
import io.pnut.gamma.util.LogUtil
import io.pnut.gamma.util.SingleLiveEvent
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.set
import kotlin.math.abs


abstract class PostItemFragment : BaseListFragment<Post, PostItemFragment.PostViewHolder>(),
    BaseListRecyclerViewAdapter.IBaseList<Post, PostItemFragment.PostViewHolder>,
    ThumbnailViewPagerAdapter.Listener, DeletePostDialogFragment.Callback,
    SimpleBottomSheetMenuFragment.Callback, PostReceiver.Callback {
    override fun onClickSegmentListener(
        viewHolder: BaseListRecyclerViewAdapter.SegmentViewHolder,
        itemWrapper: PageableItemWrapper.Pager<Post>
    ) {
        LogUtil.e("onClickSegmentListener")
        lifecycleScope.launch {
            viewModel.loadSegmentItems(itemWrapper)
        }
    }

    private val updatePollObserver = Observer<GetPoll?> { getPoll ->
        if (getPoll == null) return@Observer
        val items = viewModel.items
        val index = items.indexOfFirst { it.uniqueKey == getPoll.postId }
        if (index < 0) return@Observer
        val item = items[index] as? PageableItemWrapper.Item<Post> ?: return@Observer
        val post = item.item
        post.poll = getPoll.poll
        post.pollOptionsAdapter?.setPollDetail(getPoll.poll)
        adapter.updateItem(PageableItemWrapper.Item(post))
        viewModel.storeItems()
    }
    private val wifiManager by lazy {
        val ctx = activity?.applicationContext ?: return@lazy null
        ctx.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private val isWifiEnabled
        get() = wifiManager?.isWifiEnabled == true

    override fun onPostReceive(post: Post) {
        viewModel.isAutoScrollTemporarily = true
        viewModel.loadNewItems()
    }

    override fun onReceiveNewItemsAfter() {
        super.onReceiveNewItemsAfter()
        if (!viewModel.isAutoScrollTemporarily) return
        view?.let { getRecyclerView(it) }?.let { recyclerView ->
            val isTop =
                context?.let { (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() == 0 }
            if (isTop == true) scrollToTop()
        }

        viewModel.isAutoScrollTemporarily = false
    }

    override fun onStarReceive(post: Post) {
        adapter.updateItem(PageableItemWrapper.Item(post))
    }

    private fun updatePost(post: PageableItemWrapper<Post>) {
        adapter.updateItem(post)
    }

    override fun onRepostReceive(post: Post) {
        adapter.updateItem(PageableItemWrapper.Item(post))
    }

    override fun onDeletePostReceive(post: Post) {}

    override fun onReportPostReceive() {}

    private val receiverManager by lazy {
        activity?.let { LocalBroadcastManager.getInstance(it.applicationContext) }
    }

    private val postReceiver by lazy {
        PostReceiver(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        receiverManager?.registerReceiver(postReceiver, PostWorker.getIntentFilter())
    }

    override fun onDetach() {
        super.onDetach()
        receiverManager?.unregisterReceiver(postReceiver)
    }

    enum class BundleKey { MainPostId }

    override fun onMenuShow(menu: Menu, tag: String?) {
        val post = selectedPost ?: return
        if (tag == DialogKey.More.name) {
            val myIds = accountRepository.getStoredIds()
            if (myIds.contains(post.user?.id)) {
                menu.findItem(R.id.menuReport)?.isVisible = false
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem, tag: String?) {
        when (tag) {
            DialogKey.More.name -> handlePostMoreMenu(menuItem)
        }
    }

    private fun handlePostMoreMenu(menuItem: MenuItem) {
        val post = selectedPost ?: return
        when (menuItem.itemId) {
            R.id.menuShare -> showShareMenu(post)
            R.id.menuReport -> showReportDialog(post)
        }
    }

    private fun showReportDialog(post: Post) {
        val reasons = io.pnut.gamma.domain.entity.ReportReason.entries
        val reasonNames = arrayOf(
            getString(R.string.report_reason_soliciting),
            getString(R.string.report_reason_account_type),
            getString(R.string.report_reason_nsfw),
            getString(R.string.report_reason_user_abuse)
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.report_post)
            .setItems(reasonNames) { _, which ->
                val reason = reasons[which]
                val accountId = accountRepository.getDefaultAccount()?.id ?: ""
                PostWorker.enqueueReportPost(requireContext(), post.id, reason, accountId)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showShareMenu(post: Post) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, post.canonicalUrl)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.share)))
    }

    private var previousViewHolderItem: ViewHolderItem? = null

    data class ViewHolderItem(
        val viewHolder: PostViewHolder,
        val post: Post,
        val position: Int,
        val itemWrapper: PageableItemWrapper<Post>
    )

    protected var currentMainPostId: String = ""

    private val mainPostId by lazy {
        arguments?.getString(BundleKey.MainPostId.name, "") ?: ""
    }

    override fun overrideOptions(options: BaseListRecyclerViewAdapter.BaseListRecyclerViewAdapterOptions<Post, PostViewHolder>): BaseListRecyclerViewAdapter.BaseListRecyclerViewAdapterOptions<Post, PostViewHolder> {
        val defaultOptions = super.overrideOptions(options)
        if (currentMainPostId.isEmpty()) currentMainPostId = mainPostId
        return defaultOptions.copy(mainItemId = currentMainPostId)
    }

    open fun updateMainPostId(id: String) {
        currentMainPostId = id
        adapter.updateMainItemId(id)
    }

    override fun ok(position: Int, post: Post) {
        context?.let { PostWorker.enqueueDeletePost(it, post.id) }
        adapter.removeItem(PageableItemWrapper.Item(post))
    }

    override fun cancel() {}

    override fun onClick(path: String, position: Int, items: List<String>) {
        val newIntent = PhotoViewActivity.photoViewInstance(
            requireContext(),
            items.map { ThumbAndFull(it, it) },
            position
        )
        startActivity(newIntent)
    }

    override val itemNameRes: Int = R.string.posts

    private val itemTouchHelper: ItemTouchHelper by lazy {
        val postTouchHelperCallback = PostTouchHelperCallback(requireContext(), adapter)
        ItemTouchHelper(postTouchHelperCallback)
    }

    private val moveTransition: Transition by lazy {
        val transition =
            TransitionInflater.from(context)
                .inflateTransition(R.transition.image_shared_element_transition)
        val duration = resources.getInteger(Rr.integer.config_mediumAnimTime).toLong()
        transition.duration = duration
        transition
    }

    override lateinit var viewModel: PostItemViewModel
    private val slideToLeftIn by lazy {
        TransitionInflater.from(context).inflateTransition(R.transition.slide_to_left_in)
    }
    private val slideToLeftOut by lazy {
        TransitionInflater.from(context).inflateTransition(R.transition.slide_to_left_out)
    }

    @Inject
    lateinit var getPostUseCase: GetPostUseCase

    @Inject
    lateinit var getCachedPostUseCase: GetCachedPostListUseCase

    @Inject
    lateinit var cachePostUseCase: CachePostUseCase

    @Inject
    lateinit var getPollUseCase: GetPollUseCase

    @Inject
    lateinit var voteUseCase: VoteUseCase

    @Inject
    lateinit var accountRepository: IAccountRepository

    private val glideRequest by lazy {
        Glide.with(this)
    }

    abstract val streamType: StreamType
    open val generalPostParam: GeneralPostParam = GeneralPostParam(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(
            this,
            PostItemViewModel.Factory(
                streamType,
                getPostUseCase,
                getCachedPostUseCase,
                cachePostUseCase,
                generalPostParam,
                getPollUseCase,
                voteUseCase
            )
        )[PostItemViewModel::class.java]
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(StateKey.CurrentPosition.name, -1)
            selectedPost = BundleCompat.getParcelable(savedInstanceState, StateKey.SelectedPost.name, Post::class.java)
        }
        viewModel.updatePoll.observe(this, updatePollObserver)

    }

    override val baseListListener by lazy { this }

    override fun createViewHolder(mView: View, viewType: Int): PostViewHolder =
        PostViewHolder(
            mView,
            itemTouchHelper,
            preferenceRepository.avatarSwipe
        )

    override fun onClickItemListener(
        viewHolder: PostViewHolder,
        item: Post,
        itemWrapper: PageableItemWrapper<Post>
    ) {
        val clickedItemPosition = viewHolder.bindingAdapterPosition
        if (clickedItemPosition == RecyclerView.NO_POSITION) return

        val previousViewHolderItemLocal = previousViewHolderItem
        previousViewHolderItem = when {
            previousViewHolderItemLocal != null -> when {
                previousViewHolderItemLocal.post != item -> {
                    // click another item when already expanded
                    val position = viewModel.items.indexOf(previousViewHolderItemLocal.itemWrapper)
                    if (position >= 0 && position != clickedItemPosition) {
                        adapter.notifyItemChanged(position)
                    }
                    ViewHolderItem(viewHolder, item, clickedItemPosition, itemWrapper)
                }
                else -> // click same item
                    null
            }
            else -> ViewHolderItem(viewHolder, item, clickedItemPosition, itemWrapper)
        }

        adapter.notifyItemChanged(clickedItemPosition)
    }

    private var selectedPost: Post? = null

    private fun showMoreMenu(post: Post) {
        selectedPost = post
        val fragment = SimpleBottomSheetMenuFragment.newInstance(R.menu.post_item_more)
        fragment.show(childFragmentManager, DialogKey.More.name)
    }

    private enum class DialogKey { Compose, DeletePost, More, LongPost }


    override fun onBindViewHolder(
        item: Post,
        viewHolder: PostViewHolder,
        position: Int,
        isMainItem: Boolean
    ) {
        val context = viewHolder.itemView.context
        val isDeleted = item.isDeleted == true
        viewHolder.postItemForegroundView.alpha = if (isDeleted) 0.5f else 1f
        val bgColor =
            if (isDeleted) context.getColor(R.color.colorWindowBackground) else Util.getPrimaryColor(
                context
            )
        viewHolder.swipeActionsLayout.setBackgroundColor(bgColor)
        viewHolder.screenNameTextView.text =
            item.mainPost.user?.username ?: getString(R.string.deleted_post_user_name)
        viewHolder.handleNameTextView.text = item.mainPost.user?.name.orEmpty()
        viewHolder.avatarView.isEnabled = !isDeleted

        updateStarView(viewHolder, item)
        viewHolder.actionReplyImageView.setOnClickListener {
            showReplyCompose(item)
        }
        viewHolder.replyButton.isEnabled = !isDeleted
        viewHolder.replyButton.setOnClickListener {
            showReplyCompose(item)
        }

        updateRepostView(viewHolder, item)

        val hasConversation =
            item.mainPost.replyTo != null || item.mainPost.counts.replies > 0
        viewHolder.chatIconImageView.visibility =
            if (hasConversation) View.VISIBLE else View.GONE
        if (hasConversation) {
            val res =
                if (item.mainPost.replyTo != null) R.drawable.ic_chat_bubble_black_24dp else R.drawable.ic_chat_bubble_outline_black_24dp
            viewHolder.chatIconImageView.setImageResource(res)
        }

        viewHolder.bodyTextView.text =
            item.mainPost.content?.getSpannableStringBuilder(context)
                ?: getString(R.string.this_post_has_deleted)

        val url = item.mainPost.user?.getAvatarUrl(User.AvatarSize.Large).orEmpty()
        BindingUtil.glideAvatarSrc(viewHolder.avatarView, url)
        viewHolder.avatarView.clipToOutline = true
        val iconTransition = getString(R.string.icon_transition)
        val iconTransitionName =
            "$iconTransition + ${viewHolder.bindingAdapterPosition} ${streamType::class.java.simpleName}"
        viewHolder.avatarView.transitionName = iconTransitionName
        viewHolder.avatarView.setOnClickListener {
            currentPosition = viewHolder.bindingAdapterPosition
            val transitionMap = HashMap(
                hashMapOf<View, String>(
                    Pair(it, it.transitionName)
                )
            )
            val id = item.mainPost.user?.id ?: return@setOnClickListener
            val fragment =
                ProfileFragment.newInstance(id, url, item.mainPost.user, it.transitionName)
            sharedElementReturnTransition = moveTransition
            fragment.sharedElementEnterTransition = moveTransition
            (fragment.exitTransition as? TransitionSet)?.excludeTarget(it.transitionName, true)
            FragmentHelper.addFragment(requireContext(), fragment, id, transitionMap)
        }

        viewHolder.dateTextView.text =
            if (!isMainItem) DateUtil.getShortDateStr(
                viewHolder.itemView.context,
                item.mainPost.createdAt
            ) else ""
        viewHolder.absoluteDateTextView.text =
            DateUtil.getShortDateStrWithTime(context, item.mainPost.createdAt)
        viewHolder.contentsWrapperLayout.visibility = getVisibility(item.mainPost.showContents)

        val isNsfw = item.mainPost.nsfwMask
        viewHolder.nsfwMaskLayout.visibility = getVisibility(isNsfw)
        viewHolder.showNsfwButton.setOnClickListener {
            item.mainPost.nsfwMask = false
            adapter.notifyItemChanged(viewHolder.bindingAdapterPosition)
        }

        val isSpoiler = item.mainPost.spoilerMask
        viewHolder.spoilerMaskLayout.visibility = getVisibility(isSpoiler)
        val spoilerTopic = item.mainPost.spoiler?.topic ?: ""
        viewHolder.showSpoilerButton.text = context.getString(R.string.show_spoiler, spoilerTopic)
        viewHolder.showSpoilerButton.setOnClickListener {
            item.mainPost.spoilerMask = false
            adapter.notifyItemChanged(viewHolder.bindingAdapterPosition)
        }

        val raw = item.mainPost.raw
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
            if (isWifiEnabled) photos.forEach {
                glideRequest.load(it.url).preload()
            }
        } else {
            viewHolder.thumbnailViewPagerFrameLayout.visibility = View.GONE
            viewHolder.thumbnailViewPager.adapter = null
        }
        viewHolder.detailInfoLayout.visibility = getVisibility(isMainItem)
        val replyCount = item.mainPost.counts.replies
        val replyText =
            resources.getQuantityString(R.plurals.reply_count_template, replyCount, replyCount)

        viewHolder.replyCountTextView.text = replyText

        viewHolder.rootCardView.let {
            it.isClickable = !isMainItem
            it.isFocusable = !isMainItem
            it.elevation =
                if (isMainItem) resources.getDimension(R.dimen.elevation_main_item) else 0f
            val padding =
                if (isMainItem) resources.getDimensionPixelSize(R.dimen.pad_main_item) else 0
            it.setPadding(0, padding, 0, padding)

        }
        viewHolder.rootCardView.setOnLongClickListener {
            showThread(item)
        }
        viewHolder.reactionUsersRecyclerView.also {
            it.adapter = if (isMainItem) ReactionUsersAdapter(
                item.mainPost.reactionUsers,
                reactionUsersAdapterListener,
//                preferenceRepository.shapeOfAvatar
            ) else null
        }
        viewHolder.clientNameTextView.text = item.mainPost.source.name
        viewHolder.clientNameTextView.setOnClickListener {
            Util.openCustomTabUrl(context, item.mainPost.source.url)
        }
        viewHolder.foregroundActionsLayout.visibility =
            getVisibility(isMainItem || (previousViewHolderItem?.post == item))
        viewHolder.threadButton.let {
            it.setOnClickListener { showThread(item) }
            it.visibility = getVisibility(item.mainPost.id != currentMainPostId)
        }
        viewHolder.actionThreadImageView.let {
            it.setOnClickListener { showThread(item) }
//            it.visibility = getVisibility(post.mainPost.id != currentMainPostId)
        }
        viewHolder.isMainItem = isMainItem
        viewHolder.moreButton.setOnClickListener { showMoreMenu(item) }
        viewHolder.actionMoreImageView.setOnClickListener { showMoreMenu(item) }

        val longPost = LongPost.findLongPost(raw)
        viewHolder.showLongPostButton.visibility = getVisibility(longPost != null)
        if (longPost != null) {
            viewHolder.showLongPostButton.setOnClickListener {
                val fragment = LongPostDialogFragment.newInstance(longPost)
                fragment.show(childFragmentManager, DialogKey.LongPost.name)
            }
        }
        val revisionType = when {
            item.mainPost.revision != null -> RevisionType.Original
            item.mainPost.isRevised == true -> RevisionType.Revised
            else -> RevisionType.None
        }
        revisionType.iconRes?.let { viewHolder.revisedIconImageView.setImageResource(it) }
        viewHolder.revisedIconImageView.visibility = getVisibility(revisionType.iconRes != null)

        val pollNotice = item.pollNotice
        val isPollNeedUpdate = item.isPollNeedUpdate
        if (pollNotice != null && isPollNeedUpdate) {
            viewModel.loadPoll(item.id, pollNotice)
        }
        val poll = item.poll
        val pollLikeValue: PollLikeValue? =
            pollNotice ?: item.poll as? PollLikeValue
        if (pollLikeValue != null) {
            // TODO: get detail of poll in viewModel
            viewHolder.pollPromptTextView.text = pollLikeValue.prompt
            viewHolder.pollVoteButton.visibility = getVisibility((!pollLikeValue.alreadyClosed))
            viewHolder.pollFooterTextView.text = pollLikeValue.getDateText(context)
            if (poll != null) {
                // prevent chosen position to be initialized when it is triggered update like notifyDataSetChanged.
                if (viewHolder.pollOptionsRecyclerView.adapter != item.pollOptionsAdapter) {
                    viewHolder.pollOptionsRecyclerView.adapter = item.pollOptionsAdapter?.also {
                        it.setPollDetail(poll)
                        it.listener = object : PollOptionsAdapter.Callback {
                            override fun onUpdateChoiceState(votable: Boolean) {
                                viewHolder.pollVoteButton.isEnabled = votable
                            }
                        }
                    }
                }
                viewHolder.pollVoteButton.isEnabled = item.pollOptionsAdapter?.votable ?: false
                viewHolder.pollVoteButton.setOnClickListener {
                    viewModel.vote(
                        poll,
                        item.pollOptionsAdapter?.getChosenPositions,
                        item.id
                    )
                }
            } else {
                viewHolder.pollOptionsRecyclerView.adapter = item.pollOptionsAdapter
            }
        } else {
            viewHolder.pollVoteButton.setOnClickListener(null)
            viewHolder.pollOptionsRecyclerView.adapter = null
        }
        viewHolder.pollCardView.visibility = getVisibility(pollNotice != null)
    }

    private fun updateRepostView(viewHolder: PostViewHolder, item: Post) {
        val repostType = when {
            item.mainPost.youReposted == true -> RepostButtonType.DeleteRepost
            item.mainPost.user?.me == true -> RepostButtonType.DeletePost
            else -> RepostButtonType.Repost
        }
        viewHolder.actionRepostImageView.let {
            //                it.setText(repostType.textRes)
            it.setImageResource(repostType.iconRes)
            it.imageTintList = ColorStateList.valueOf(Color.WHITE)
            it.setOnClickListener {
                toggleRepost(repostType, item.mainPost, viewHolder.bindingAdapterPosition)
            }
        }
        viewHolder.repostButton.isEnabled = !item.isDeletedNonNull
        viewHolder.repostButton.let {
            it.setImageResource(repostType.iconRes)
            it.setOnClickListener {
                toggleRepost(repostType, item.mainPost, viewHolder.bindingAdapterPosition)
            }
        }
        setupRepostView(item, viewHolder.repostedByTextView)

        viewHolder.repostStateView.visibility =
            if (item.mainPost.youReposted == true) View.VISIBLE else View.GONE
        val repostCount = item.mainPost.counts.reposts
        val repostText =
            resources.getQuantityString(R.plurals.repost_count_template, repostCount, repostCount)
        viewHolder.repostCountTextView.text = repostText
    }

    private fun updateStarView(viewHolder: PostViewHolder, item: Post) {
        val starDrawableRes =
            if (item.mainPost.youBookmarked == true) R.drawable.ic_star_black_24dp else R.drawable.ic_star_border_black_24dp
        viewHolder.actionStarImageView.let {
            it.setOnClickListener {
                toggleStar(item, viewHolder.bindingAdapterPosition)

            }
//                val starTextRes = if (item.mainPost.youBookmarked == true) R.string.unstar else R.string.star
//                it.text = context.getString(starTextRes)
            it.setImageResource(starDrawableRes)
            it.imageTintList = ColorStateList.valueOf(Color.WHITE)
        }
        viewHolder.starButton.isEnabled = !item.isDeletedNonNull
        viewHolder.starButton.let {
            it.setOnClickListener {
                toggleStar(item, viewHolder.bindingAdapterPosition)
            }
            it.setImageResource(starDrawableRes)
        }
        viewHolder.starStateView.visibility =
            if (item.mainPost.youBookmarked == true) View.VISIBLE else View.GONE
        val starCount = item.mainPost.counts.bookmarks
        val starText =
            resources.getQuantityString(R.plurals.star_count_template, starCount, starCount)
        viewHolder.starCountTextView.text = starText
    }

    private enum class RevisionType(val iconRes: Int?) {
        None(null), Revised(R.drawable.ic_create_black_24dp), Original(R.drawable.ic_outline_create_24dp)
    }

    private fun toggleRepost(repostType: RepostButtonType, item: Post, adapterPosition: Int) {
        when (repostType) {
            RepostButtonType.DeletePost -> {
                if (item.mainPost.user?.me == false) return
                val dialog = DeletePostDialogFragment.newInstance(adapterPosition, item.mainPost)
                dialog.show(childFragmentManager, DialogKey.DeletePost.name)
            }
            RepostButtonType.DeleteRepost,
            RepostButtonType.Repost -> {
                val newState = item.mainPost.youReposted == false
                context?.let { PostWorker.enqueueRepost(it, item.mainPost.id, newState) }
                item.mainPost.youReposted = newState
            }
        }
        // TODO: revert state when raised error
        adapter.notifyItemChanged(adapterPosition)
    }

    private fun showReplyCompose(item: Post) {
        val intent = ComposePostActivity.newIntent(requireContext(), replyTarget = item)
        startActivity(intent)
    }

    private fun toggleStar(item: Post, adapterPosition: Int) {
        val newState = item.mainPost.youBookmarked == false
        context?.let { PostWorker.enqueueStar(it, item.mainPost.id, newState) }
        // TODO: revert state when raised error
        // star "this post"
        item.mainPost.youBookmarked = newState
        adapter.notifyItemChanged(adapterPosition)
    }

    private fun showThread(item: Post): Boolean {
        val mainPost = item.mainPost
        val tag = "PostThread_${mainPost.threadId}"
        val fm = parentFragmentManager
        val currentFragment = fm.findFragmentById(R.id.fragmentPlaceholder)
        if (currentFragment != null && currentFragment.tag == tag) {
            if (currentFragment is PostItemFragment) {
                currentFragment.updateMainPostId(item.id)
                return true
            }
        }

        val fragment = getThreadInstance(mainPost, mainPost.id)
        return addFragment(fragment, tag) == null
    }

    private val reactionUsersAdapterListener by lazy {
        object : ReactionUsersAdapter.Listener {
            override fun onUserClick(user: User) {
                val fragment = ProfileFragment.newInstance(user.id, user.getAvatarUrl(), user)
                addFragment(fragment, user.username)
            }
        }
    }

    private fun getVisibility(b: Boolean): Int = if (b) View.VISIBLE else View.GONE

    private enum class RepostButtonType(
        @StringRes val textRes: Int,
        @DrawableRes val iconRes: Int
    ) {
        DeleteRepost(R.string.delete_repost, R.drawable.ic_repeat_black_24dp),
        DeletePost(R.string.delete_post, R.drawable.ic_delete_black_24dp),
        Repost(R.string.repost, R.drawable.ic_repeat_border_black_24dp)
    }

    private fun setupRepostView(item: Post, repostedByTextView: TextView) {
        val originalUser = item.user
        val visibility = if (item.repostOf != null && originalUser != null) {
            repostedByTextView.setOnClickListener {
                val fragment = ProfileFragment.newInstance(originalUser.id)
                addFragment(fragment, originalUser.id)
            }
            repostedByTextView.text =
                repostedByTextView.context.getString(
                    R.string.reposted_by_template,
                    originalUser.username
                )
            true
        } else {
            false
        }
        repostedByTextView.visibility = getVisibility(visibility)
    }

    override fun getItemLayout(): Int = R.layout.fragment_post_item

    private var currentPosition = -1

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(StateKey.CurrentPosition.name, currentPosition)
        outState.putParcelable(StateKey.SelectedPost.name, selectedPost)
    }

    private enum class StateKey { CurrentPosition, SelectedPost }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = getRecyclerView(view)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>?,
                sharedElements: MutableMap<String, View>?
            ) {
                val viewHolder =
                    recyclerView.findViewHolderForLayoutPosition(currentPosition) ?: return
                if (names == null || sharedElements == null) return
                sharedElements[names[0]] = viewHolder.itemView.findViewById(R.id.avatarImageView)
            }
        })

    }

    // TODO: create the view holder for deleted post
    @SuppressLint("ClickableViewAccessibility")
    class PostViewHolder(
        mView: View,
        itemTouchHelper: ItemTouchHelper,
        avatarSwipe: Boolean
    ) : RecyclerView.ViewHolder(mView) {
        val rootCardView: CardView = itemView.findViewById(R.id.rootCardView)
        var startX = -1f
        private val threshold = 10f
        val avatarView: ImageView = itemView.findViewById<ImageView>(R.id.avatarImageView).also {
            it.setOnTouchListener { view, motionEvent ->
                if (!view.isEnabled) return@setOnTouchListener false
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    startX = motionEvent.x
                }
                if (motionEvent.actionMasked == MotionEvent.ACTION_MOVE && avatarSwipe && abs(
                        startX - motionEvent.x
                    ) > threshold
                ) {
                    itemTouchHelper.startSwipe(this)
                }
                false
            }
        }
        val screenNameTextView: TextView = itemView.findViewById(R.id.screenNameTextView)
        val bodyTextView: LinkableTextView = itemView.findViewById(R.id.bodyTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.relativeTimeTextView)
        val handleNameTextView: TextView = itemView.findViewById(R.id.handleNameTextView)
        val repostedByTextView: TextView = itemView.findViewById(R.id.repostedByTextView)
        val starStateView: View = itemView.findViewById(R.id.starStateView)
        val repostStateView: View = itemView.findViewById(R.id.repostStateView)
        val actionReplyImageView: ImageView = itemView.findViewById(R.id.actionReplyImageView)
        val actionStarImageView: ImageView = itemView.findViewById(R.id.actionStarImageView)
        val actionRepostImageView: ImageView = itemView.findViewById(R.id.actionRepostImageView)
        val actionThreadImageView: ImageView = itemView.findViewById(R.id.actionThreadImageView)
        val actionMoreImageView: ImageView = itemView.findViewById(R.id.actionMoreImageView)
        val thumbnailViewPager: ViewPager2 = itemView.findViewById(R.id.thumbnailViewPager)
        val thumbnailViewPagerFrameLayout: FrameLayout = itemView.findViewById(R.id.thumbnailViewPagerFrameLayout)
        val thumbnailTabLayout: TabLayout = itemView.findViewById(R.id.thumbnailTabLayout)
        val chatIconImageView: ImageView = itemView.findViewById(R.id.chatIconImageView)
        val nsfwMaskLayout: FrameLayout = itemView.findViewById(R.id.nsfwMaskLayout)
        val showNsfwButton: MaterialButton = itemView.findViewById(R.id.showNsfwButton)
        val spoilerMaskLayout: FrameLayout = itemView.findViewById(R.id.spoilerMaskLayout)
        val showSpoilerButton: MaterialButton = itemView.findViewById(R.id.showSpoilerButton)
        val contentsWrapperLayout: LinearLayout = itemView.findViewById(R.id.contentsWrapperLayout)
        val detailInfoLayout: ConstraintLayout = itemView.findViewById(R.id.detailInfoLayout)
        val replyCountTextView: TextView = itemView.findViewById(R.id.replyCountTextView)
        val repostCountTextView: TextView = itemView.findViewById(R.id.repostCountTextView)
        val starCountTextView: TextView = itemView.findViewById(R.id.starCountTextView)
        val postItemForegroundView: ConstraintLayout = itemView.findViewById(R.id.postItemForegroundView)
        val reactionUsersRecyclerView: RecyclerView = itemView.findViewById(R.id.reactionUsersRecyclerView)
        val clientNameTextView: TextView = itemView.findViewById(R.id.clientNameTextView)
        val foregroundActionsLayout: LinearLayout = itemView.findViewById(R.id.foregroundActionsLayout)
        val replyButton: ImageButton = itemView.findViewById(R.id.replyButton)
        val starButton: ImageButton = itemView.findViewById(R.id.starButton)
        val repostButton: ImageButton = itemView.findViewById(R.id.repostButton)
        val threadButton: ImageButton = itemView.findViewById(R.id.threadButton)
        val moreButton: ImageButton = itemView.findViewById(R.id.moreButton)
        var isMainItem: Boolean = false
        val showLongPostButton: MaterialButton = itemView.findViewById(R.id.showLongPostButton)
        val revisedIconImageView: ImageView = itemView.findViewById(R.id.revisedIconImageView)
        val swipeActionsLayout: FrameLayout = itemView.findViewById(R.id.swipeActionsLayout)
        val absoluteDateTextView: TextView = itemView.findViewById(R.id.absoluteDateTextView)
        val pollCardView: CardView = itemView.findViewById(R.id.pollCardView)
        val pollPromptTextView: TextView = itemView.findViewById(R.id.pollPromptTextView)
        val pollOptionsRecyclerView: RecyclerView = itemView.findViewById<RecyclerView>(R.id.pollOptionsRecyclerView).also {
            it.isNestedScrollingEnabled = false
        }
        val pollFooterTextView: TextView = itemView.findViewById(R.id.pollFooterTextView)
        val pollVoteButton: MaterialButton = itemView.findViewById(R.id.pollVoteButton)

        init {
            addSpacerDecoration()
            bodyTextView.setOnTouchListener(EntityOnTouchListener())
        }

        private fun addSpacerDecoration() {
            val context = itemView.context
            val drawable =
                ContextCompat.getDrawable(context, R.drawable.spacer_width_half) ?: return
            val reactionSpacerDecoration =
                DividerItemDecoration(context, RecyclerView.HORIZONTAL).also {
                    it.setDrawable(drawable)
                }
            reactionUsersRecyclerView.addItemDecoration(reactionSpacerDecoration)
        }
    }

    data class GetPoll(val postId: String, val poll: Poll)

    class PostItemViewModel(
        private val streamType: StreamType,
        private val getPostUseCase: GetPostUseCase,
        private val getCachedPostUseCase: GetCachedPostListUseCase,
        private val cachePostUseCase: CachePostUseCase,
        private val generalPostParam: GeneralPostParam,
        private val getPollUseCase: GetPollUseCase,
        private val voteUseCase: VoteUseCase
    ) :
        BaseListViewModel<Post>() {
        var isAutoScrollTemporarily = false
        val updatePoll = SingleLiveEvent<GetPoll>()

        override suspend fun getItems(requestPager: PageableItemWrapper.Pager<Post>?): PnutResponse<List<Post>> {
            val getPostParam = GetPostsParam().apply {
                requestPager?.let { add(PaginationParam.createFromPager(requestPager)) }
                add(generalPostParam)
            }
            return getPostUseCase.run(GetPostInputData(streamType, getPostParam)).res
        }

        override suspend fun onReceiveNewItems(response: PnutResponse<List<Post>>) {
            val idsToReload = mutableListOf<String>()
            response.meta.revisedIds?.let { idsToReload.addAll(it) }
            response.meta.deletedIds?.let { idsToReload.addAll(it) }

            if (idsToReload.isEmpty()) return

            val existingIds =
                items.filterIsInstance<PageableItemWrapper.Item<Post>>().map { it.item.id }.toSet()
            val filteredIdsToReload = idsToReload.filter { existingIds.contains(it) }

            if (filteredIdsToReload.isEmpty()) return

            runCatching {
                getPostUseCase.run(
                    GetPostInputData(
                        StreamType.Posts(filteredIdsToReload),
                        GetPostsParam()
                    )
                ).res
            }.onSuccess { reloadedResponse ->
                reloadedResponse.data.forEach { reloadedPost ->
                    val index =
                        items.indexOfFirst { it is PageableItemWrapper.Item && it.item.id == reloadedPost.id }
                    if (index >= 0) {
                        if (reloadedPost.isDeleted == true) {
                            items.removeAt(index)
                        } else {
                            items[index] = PageableItemWrapper.Item(reloadedPost)
                        }
                    }
                }
            }
        }

        override fun loadCache() {
            viewModelScope.launch {
                runCatching {
                    getCachedPostUseCase.run(GetCachedPostListInputData(streamType))
                }.onSuccess {
                    items.addAll(it.posts.data)
                }
                if (streamType is StreamType.Thread) {
                    loadItems(null)
                }
                super.loadCache()
            }
        }

        override fun storeItems() {
            viewModelScope.launch {
                runCatching {
                    cachePostUseCase.run((CachePostInputData(items, streamType)))
                }
            }
        }

        fun loadPoll(postId: String, pollNotice: PollNotice) {
            LogUtil.e("loadPoll")
            viewModelScope.launch {
                runCatching {
                    getPollUseCase.run(
                        GetPollInputData(
                            pollNotice.id,
                            pollNotice.pollToken
                        )
                    )
                }.onSuccess {
                    updatePoll.emit(GetPoll(postId, it.poll))
                }
            }
        }

        fun vote(poll: Poll, chosenPositions: Set<Int>?, postId: String) {
            if (chosenPositions == null) return
            viewModelScope.launch {
                runCatching {
                    voteUseCase.run(VoteInputData(poll.id, poll.pollToken, chosenPositions))
                }.onSuccess {
                    updatePoll.emit(GetPoll(postId, it.poll))
                }
            }
        }

        class Factory(
            private val streamType: StreamType,
            private val getPostUseCase: GetPostUseCase,
            private val getCachedPostUseCase: GetCachedPostListUseCase,
            private val cachePostUseCase: CachePostUseCase,
            private val generalPostParam: GeneralPostParam,
            private val getPollUseCase: GetPollUseCase,
            private val voteUseCase: VoteUseCase
        ) :
            ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PostItemViewModel(
                    streamType,
                    getPostUseCase,
                    getCachedPostUseCase,
                    cachePostUseCase,
                    generalPostParam,
                    getPollUseCase,
                    voteUseCase
                ) as T
            }
        }
    }

//    override fun onRefresh() {
//        viewModel.loadNewPosts()
//    }

    @AndroidEntryPoint
    class HomeStream : PostItemFragment() {
        override val streamType = StreamType.Home
        override val generalPostParam: GeneralPostParam by lazy {
            GeneralPostParam(
                includeDeleted = false,
                includeDirectedPosts = !preferenceRepository.hideDirectedPosts
            )
        }
    }

    @AndroidEntryPoint
    class MentionsStream : PostItemFragment() {
        override val streamType = StreamType.Mentions
    }

    @AndroidEntryPoint
    class SearchPostsFragment : PostItemFragment() {
        private val keyword by lazy {
            arguments?.getString(BundleKey.Keyword.name, "").orEmpty()
        }

        private enum class BundleKey { Keyword }

        override val streamType by lazy {
            StreamType.Search(keyword)
        }

        companion object {
            fun newInstance(keyword: String) = SearchPostsFragment().apply {
                arguments = Bundle().apply {
                    putString(BundleKey.Keyword.name, keyword)
                }
            }
        }
    }

    companion object {
        fun getHomeStreamInstance() = HomeStream()
        fun getMentionStreamInstance() = MentionsStream()
//        fun getConversationInstance() = ExploreFragment.ConversationsFragment.newInstance()
//        fun getMissedConversationInstance() =
//            ExploreFragment.MissedConversationsFragment.newInstance()
//
//        fun getNewcomersInstance() = ExploreFragment.NewcomersFragment.newInstance()
//        fun getPhotoInstance() = ExploreFragment.PhotosFragment.newInstance()
//        fun getTrendingInstance() = ExploreFragment.TrendingFragment.newInstance()
//        fun getGlobalInstance() = ExploreFragment.GlobalFragment.newInstance()
        fun getTaggedStreamInstance(tag: String) = TagStreamFragment.newInstance(tag)

        fun getUserPostInstance(userId: String) =
            SpecificUserPostFragment.UserPostFragment.newInstance(userId)

        fun getStarInstance(userId: String = "me") =
            SpecificUserPostFragment.StarsPostFragment.newInstance(userId)

        fun getThreadInstance(post: Post, mainPostId: String = "") =
            ThreadFragment.newInstance(post, mainPostId)
    }
}