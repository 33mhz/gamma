package io.pnut.gamma.presentation.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import io.pnut.gamma.R
import io.pnut.gamma.domain.Relationship
import io.pnut.gamma.domain.entity.PnutResponse
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.model.PageableItemWrapper
import io.pnut.gamma.domain.model.UserListType
import io.pnut.gamma.domain.model.io.CacheUserInputData
import io.pnut.gamma.domain.model.io.GetCachedUserListInputData
import io.pnut.gamma.domain.model.io.GetUsersInputData
import io.pnut.gamma.domain.model.io.UpdateRelationshipInputData
import io.pnut.gamma.domain.model.params.composed.GetUsersParam
import io.pnut.gamma.domain.model.params.single.PaginationParam
import io.pnut.gamma.domain.model.params.single.SearchUserParam
import io.pnut.gamma.domain.usecases.CacheUserUseCase
import io.pnut.gamma.domain.usecases.GetCachedUserListUseCase
import io.pnut.gamma.domain.usecases.GetUsersUseCase
import io.pnut.gamma.domain.usecases.UpdateRelationshipUseCase
import io.pnut.gamma.presentation.adapter.BaseListRecyclerViewAdapter
import io.pnut.gamma.presentation.adapter.UserViewHolder
import io.pnut.gamma.util.SingleLiveEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

abstract class UserListFragment : BaseListFragment<User, UserViewHolder>(),
    BaseListRecyclerViewAdapter.IBaseList<User, UserViewHolder>,
    UserViewHolder.Callback {
    private val updateUserObserver = Observer<User> {
        if (it == null) return@Observer
        adapter.updateItem(PageableItemWrapper.Item(it))
        viewModel.storeItems()

    }
    override val itemNameRes: Int = R.string.users
    override fun onClickSegmentListener(
        viewHolder: BaseListRecyclerViewAdapter.SegmentViewHolder,
        itemWrapper: PageableItemWrapper.Pager<User>
    ) {
        viewModel.loadMoreItems()
    }

    override val baseListListener: BaseListRecyclerViewAdapter.IBaseList<User, UserViewHolder> by lazy {
        this
    }
    override val viewModel by lazy {
        ViewModelProvider(
            this,
            UserListViewModel.Factory(
                userListType,
                getUsersUseCase,
                cachedUserListUseCase,
                cacheUserUseCase,
                updateRelationshipUseCase
            )
        )[UserListViewModel::class.java]
    }


    @Inject
    lateinit var getUsersUseCase: GetUsersUseCase
    abstract val userListType: UserListType
    @Inject
    lateinit var cachedUserListUseCase: GetCachedUserListUseCase
    @Inject
    lateinit var cacheUserUseCase: CacheUserUseCase
    @Inject
    lateinit var updateRelationshipUseCase: UpdateRelationshipUseCase

    override fun createViewHolder(mView: View, viewType: Int): UserViewHolder =
        UserViewHolder(mView)

    override fun onClickItemListener(
        viewHolder: UserViewHolder,
        item: User,
        itemWrapper: PageableItemWrapper<User>
    ) {
        val fragment = ProfileFragment.newInstance(item.id, item.content.avatarImage.url, item)
        addFragment(fragment, item.id)
    }

    override fun onBindViewHolder(
        item: User,
        viewHolder: UserViewHolder,
        position: Int,
        isMainItem: Boolean
    ) {
        viewHolder.bind(item, this)
    }

    override fun getItemLayout(): Int = R.layout.fragment_user_item

    class UserListViewModel(
        private val userListType: UserListType,
        private val getUsersUseCase: GetUsersUseCase,
        private val cachedUserListUseCase: GetCachedUserListUseCase,
        private val cacheUserUseCase: CacheUserUseCase,
        private val updateRelationshipUseCase: UpdateRelationshipUseCase
    ) : BaseListViewModel<User>() {
        val updateUser = SingleLiveEvent<User>()
        override suspend fun getItems(requestPager: PageableItemWrapper.Pager<User>?): PnutResponse<List<User>> {
            val getUsersParam = GetUsersParam().apply {
                requestPager?.let { add(PaginationParam.createFromPager(it)) }
            }
            if (userListType is UserListType.Search) getUsersParam.add(SearchUserParam(userListType.keyword))
            val getUsersInputData = GetUsersInputData(userListType, getUsersParam)
            return getUsersUseCase.run(getUsersInputData).res
        }

        override fun loadCache() {
            viewModelScope.launch {
                val res = cachedUserListUseCase.run(GetCachedUserListInputData((userListType)))
                items.addAll(res.users.data)
                super.loadCache()
            }
        }

        override fun storeItems() {
            viewModelScope.launch {
                runCatching {
                    cacheUserUseCase.run(CacheUserInputData(items, userListType))
                }
            }
        }

        private fun updateRelationship(
            targetUser: User,
            relationship: Relationship
        ) {
            viewModelScope.launch {
                runCatching {
                    updateRelationshipUseCase.run(
                        UpdateRelationshipInputData(
                            targetUser.id,
                            relationship
                        )
                    )
                }.onSuccess {
                    updateUser.postValue(it.res.data)
                }
            }
        }

        fun unBlock(targetUser: User) {
            targetUser.youFollow = false
            targetUser.youBlocked = false
            updateRelationship(targetUser, Relationship.UnBlock)
        }

        fun follow(targetUser: User) {
            targetUser.youFollow = true
            updateRelationship(targetUser, Relationship.Follow)
        }

        fun unFollow(targetUser: User) {
            targetUser.youFollow = false
            updateRelationship(targetUser, Relationship.UnFollow)
        }

        class Factory(
            private val userListType: UserListType,
            private val getUsersUseCase: GetUsersUseCase,
            private val cachedUserListUseCase: GetCachedUserListUseCase,
            private val cacheUserUseCase: CacheUserUseCase,
            private val updateRelationshipUseCase: UpdateRelationshipUseCase

        ) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return UserListViewModel(
                    userListType,
                    getUsersUseCase,
                    cachedUserListUseCase,
                    cacheUserUseCase,
                    updateRelationshipUseCase
                ) as T
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.updateUser.observe(this, updateUserObserver)
    }

    override fun onActionButtonClick(user: User) {
        if (user.me) return
        when {
            user.youBlocked -> viewModel.unBlock(user)
            user.youFollow -> viewModel.unFollow(user)
            !user.youFollow -> viewModel.follow(user)
        }
    }

    @AndroidEntryPoint
    class SearchUserListFragment : UserListFragment() {
        override val userListType by lazy {
            UserListType.Search(keyword)
        }

        private val keyword by lazy {
            arguments?.getString(BundleKey.Keyword.name, "").orEmpty()
        }


        private enum class BundleKey { Keyword }
        companion object {
            fun newInstance(keyword: String) = SearchUserListFragment().apply {
                arguments = Bundle().apply {
                    putString(BundleKey.Keyword.name, keyword)
                }
            }
        }
    }
}
