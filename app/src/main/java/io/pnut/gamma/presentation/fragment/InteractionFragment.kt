package io.pnut.gamma.presentation.fragment


import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import io.pnut.gamma.R
import io.pnut.gamma.databinding.InteractionItemBinding
import io.pnut.gamma.domain.entity.Interaction
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.model.PageableItemWrapper
import io.pnut.gamma.domain.model.io.CacheInteractionInputData
import io.pnut.gamma.domain.model.io.GetInteractionInputData
import io.pnut.gamma.domain.model.params.composed.GetInteractionsParam
import io.pnut.gamma.domain.model.params.single.PaginationParam
import io.pnut.gamma.domain.usecases.CacheInteractionUseCase
import io.pnut.gamma.domain.usecases.GetCachedInteractionListUseCase
import io.pnut.gamma.domain.usecases.GetInteractionUseCase
import io.pnut.gamma.presentation.adapter.BaseListRecyclerViewAdapter
import io.pnut.gamma.presentation.adapter.ReactionUsersAdapter
import io.pnut.gamma.presentation.util.DateUtil
import io.pnut.gamma.presentation.util.navigateTo
import io.pnut.gamma.util.LogUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InteractionFragment :
    BaseListFragment<Interaction, InteractionFragment.InteractionViewHolder>(),
    BaseListRecyclerViewAdapter.IBaseList<Interaction, InteractionFragment.InteractionViewHolder> {
    override fun onClickSegmentListener(
        viewHolder: BaseListRecyclerViewAdapter.SegmentViewHolder,
        itemWrapper: PageableItemWrapper.Pager<Interaction>
    ) {
        viewModel.loadMoreItems()
    }

    override val itemNameRes: Int = R.string.interactions

    @Inject
    lateinit var getInteractionUseCase: GetInteractionUseCase
    @Inject
    lateinit var getCachedInteractionListUseCase: GetCachedInteractionListUseCase
    @Inject
    lateinit var cacheInteractionUseCase: CacheInteractionUseCase

    override lateinit var viewModel: InteractionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(
            this,
            InteractionViewModel.Factory(
                getInteractionUseCase,
                getCachedInteractionListUseCase,
                cacheInteractionUseCase
            )
        )[InteractionViewModel::class.java]
        super.onCreate(savedInstanceState)
    }

    override fun createViewHolder(mView: View, viewType: Int): InteractionViewHolder =
        InteractionViewHolder(mView)

    override fun onClickItemListener(
        viewHolder: InteractionViewHolder,
        item: Interaction,
        itemWrapper: PageableItemWrapper<Interaction>
    ) {
    }

    override fun onBindViewHolder(
        item: Interaction,
        viewHolder: InteractionViewHolder,
        position: Int,
        isMainItem: Boolean
    ) {
        val concreteItem = when (item.action) {
            Interaction.Action.Bookmark -> item as Interaction.Bookmark
            Interaction.Action.PollResponse -> item as Interaction.PollResponse
            Interaction.Action.Follow -> item as Interaction.Follow
            Interaction.Action.Reply -> item as Interaction.Reply
            Interaction.Action.Repost -> item as Interaction.Repost
        }
        viewHolder.iconView.setImageResource(item.action.iconRes)
        viewHolder.timeTextView.text =
            DateUtil.getShortDateStr(viewHolder.itemView.context, item.eventDate)
        viewHolder.messageTextView.text = item.getMessage(viewHolder.itemView.context)
        viewHolder.bodyTextView.visibility = when (concreteItem) {
            is Interaction.Repost,
            is Interaction.Bookmark,
            is Interaction.Reply,
            is Interaction.PollResponse -> View.VISIBLE
            is Interaction.Follow -> View.GONE
        }
        viewHolder.bodyTextView.text = when (concreteItem) {
            is Interaction.Repost -> handlePost(concreteItem.objects)
            is Interaction.Bookmark -> handlePost(concreteItem.objects)
            is Interaction.Reply -> handlePost(concreteItem.objects)
            is Interaction.Follow -> handleUser(concreteItem.objects)
            is Interaction.PollResponse -> handlePoll(concreteItem.objects)
        }
        viewHolder.reactionUsersRecyclerView.adapter = when (concreteItem) {
            is Interaction.HasUsersFieldInteraction -> {
                ReactionUsersAdapter(
                    concreteItem.users.orEmpty(),
                    reactionUsersAdapterListener,
//                    preferenceRepository.shapeOfAvatar
                )
            }
            else -> null
        }
        viewHolder.reactionUsersRecyclerView.visibility = when (concreteItem) {
            is Interaction.HasUsersFieldInteraction -> View.VISIBLE
            else -> View.GONE
        }
        viewHolder.itemView.setOnClickListener {
            when (concreteItem) {
                is Interaction.Repost -> showPost(concreteItem.objects[0])
                is Interaction.Bookmark -> showPost(concreteItem.objects[0])
                is Interaction.Reply -> Unit
                is Interaction.Follow -> Unit
                is Interaction.PollResponse -> Unit
            }
        }
    }

    private fun showPost(post: Post) {
        val fragment = ThreadFragment.newInstance(post, post.id)
        navigateTo(fragment, post.id)
    }


    private val reactionUsersAdapterListener by lazy {
        object : ReactionUsersAdapter.Listener {
            override fun onUserClick(user: User) {
                val fragment = ProfileFragment.newInstance(user.id, user.getAvatarUrl(), user)
                navigateTo(fragment, user.username)
            }
        }
    }


    private fun handlePoll(objects: List<Interaction.PollResponse.InteractionPoll>): CharSequence {
        return objects[0].prompt
    }

    private fun handleUser(objects: List<User>): CharSequence {
        return objects[0].username
    }

    private fun handlePost(posts: List<Post>): SpannableStringBuilder? {
        return context?.let { posts[0].content?.getSpannableStringBuilder(it) }
    }

    override fun getItemLayout(): Int = R.layout.interaction_item

    override val baseListListener = this

    class InteractionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = InteractionItemBinding.bind(view)
        val messageTextView: TextView = binding.messageTextView
        val iconView: ImageView = binding.iconView
        val bodyTextView: TextView = binding.bodyTextView
        val timeTextView: TextView = binding.timeTextView
        val reactionUsersRecyclerView: RecyclerView = binding.reactionUsersRecyclerView

        init {
            addSpacerDecoration()
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

    class InteractionViewModel(
        private val getInteractionUseCase: GetInteractionUseCase,
        private val getCachedInteractionListUseCase: GetCachedInteractionListUseCase,
        private val cacheInteractionUseCase: CacheInteractionUseCase
    ) :
        BaseListViewModel<Interaction>() {
        override fun storeItems() {
            viewModelScope.launch {
                runCatching {
                    cacheInteractionUseCase.run(CacheInteractionInputData(items))
                }
            }
        }

        override fun loadCache() {
            viewModelScope.launch {
                runCatching {
                    getCachedInteractionListUseCase.run(Unit)
                }.onSuccess {
                    items.addAll(it.interactions.data)
                }
                super.loadCache()
            }
        }

        override suspend fun getItems(
            requestPager: PageableItemWrapper.Pager<Interaction>?
        ): PnutResponse<List<Interaction>> {
            val modParams = GetInteractionsParam().apply {
                requestPager?.let {
                    add(
                        PaginationParam.createFromPager(requestPager)
                    )
                }
            }
            LogUtil.d(modParams.toString())
            return getInteractionUseCase.run(GetInteractionInputData(modParams)).res
        }


        class Factory(
            private val getInteractionUseCase: GetInteractionUseCase,
            private val getCachedInteractionListUseCase: GetCachedInteractionListUseCase,
            private val cacheInteractionUseCase: CacheInteractionUseCase

        ) :
            ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return InteractionViewModel(
                    getInteractionUseCase,
                    getCachedInteractionListUseCase,
                    cacheInteractionUseCase
                ) as T
            }

        }
    }

    companion object {
        fun newInstance() = InteractionFragment()
    }
}
