package net.unsweets.gamma.presentation.fragment

import android.os.Bundle
import androidx.core.os.BundleCompat
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import net.unsweets.gamma.R
import net.unsweets.gamma.databinding.ListWithToolbarBinding
import net.unsweets.gamma.domain.entity.User
import net.unsweets.gamma.domain.model.UserListType
import dagger.hilt.android.AndroidEntryPoint


abstract class FollowingFollowerListFragment : UserListFragment() {
    override fun getFragmentLayout(): Int = R.layout.list_with_toolbar
    override fun getRecyclerView(view: View): RecyclerView = ListWithToolbarBinding.bind(view).itemList
    private enum class BundleKey { User }

    protected val user by lazy {
        arguments?.let { BundleCompat.getParcelable(it, BundleKey.User.name, User::class.java) } ?: throw IllegalArgumentException("Must set user")
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = ListWithToolbarBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener { backToPrevFragment() }
        val count = when (userListType) {
            is UserListType.Following -> user.counts.following
            is UserListType.Followers -> user.counts.followers
            else -> null
        }
        val res = when (userListType) {
            is UserListType.Following -> R.string.following_with_name
            is UserListType.Followers -> R.string.followers_with_name
            else -> null
        }
        val title = res?.let { getString(it, user.username) }
        binding.toolbar.title = title
        if (count != null) {
            val subtitle = resources.getQuantityString(R.plurals.user, count, count)
            binding.toolbar.subtitle = subtitle
        }

    }


    @AndroidEntryPoint
    class FollowerListFragment : FollowingFollowerListFragment() {
        override val userListType by lazy {
            UserListType.Followers(user.id)
        }

        companion object {
            fun newInstance(user: User) = FollowerListFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(BundleKey.User.name, user)
                }
            }
        }
    }

    @AndroidEntryPoint
    class FollowingListFragment : FollowingFollowerListFragment() {
        override val userListType by lazy {
            UserListType.Following(user.id)
        }

        companion object {
            fun newInstance(user: User) = FollowingListFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(BundleKey.User.name, user)
                }
            }
        }
    }
}
