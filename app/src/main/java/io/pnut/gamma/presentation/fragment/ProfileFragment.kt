package io.pnut.gamma.presentation.fragment


import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.os.BundleCompat
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.launch
import io.pnut.gamma.R
import io.pnut.gamma.databinding.FragmentProfileBinding
import io.pnut.gamma.domain.Relationship
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.model.io.GetProfileInputData
import io.pnut.gamma.domain.model.io.UpdateRelationshipInputData
import io.pnut.gamma.domain.usecases.GetProfileUseCase
import io.pnut.gamma.domain.usecases.UpdateRelationshipUseCase
import io.pnut.gamma.presentation.activity.EditProfileActivity
import io.pnut.gamma.presentation.activity.PhotoViewActivity
import io.pnut.gamma.presentation.adapter.ProfilePagerAdapter
import io.pnut.gamma.presentation.util.BindingUtil
import io.pnut.gamma.presentation.util.EntityOnTouchListener
import com.bumptech.glide.Glide
import io.pnut.gamma.presentation.util.ShareUtil
import io.pnut.gamma.presentation.util.Util
import io.pnut.gamma.util.SingleLiveEvent
import java.util.Calendar
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.util.Constants
import kotlin.math.abs

@AndroidEntryPoint
class ProfileFragment : BaseFragment() {
    private enum class BundleKey {
        ID, IconUrl, User, IconTransitionName
    }

    private val userPostsRssUrl: String by lazy {
        Constants.API_BASE_URL + "feed/rss/users/$userId/posts"
    }
    private val fetchingUserObserve = Observer<Boolean> {
        binding.swipeRefreshLayout.isRefreshing = it
    }

    @Inject
    lateinit var getProfileUseCase: GetProfileUseCase

    @Inject
    lateinit var updateRelationshipUseCase: UpdateRelationshipUseCase

    private val viewModel: ProfileViewModel by lazy {
        ViewModelProvider(
            this,
            ProfileViewModel.Factory(
                requireActivity().application,
                getProfileUseCase,
                updateRelationshipUseCase,
                userId
            )
        )[ProfileViewModel::class.java]
    }

    private val userId: String by lazy {
        arguments?.getString(BundleKey.ID.name, "") ?: ""
    }

    private lateinit var binding: FragmentProfileBinding

    private val entityOnTouchListener: View.OnTouchListener = EntityOnTouchListener()

    private val eventObserver = Observer<Event>(::eventHandling)
    private val userObserver = Observer<User> {
        if (it == null || it.content.coverImage.isDefault) return@Observer
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.event.observe(this, eventObserver)
        viewModel.user.observe(this, userObserver)
        viewModel.fetchingUser.observe(this, fetchingUserObserve)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        arguments?.let { bundle ->
            val iconUrl = bundle.getString(BundleKey.IconUrl.name, "")
            if (iconUrl != null && iconUrl.isNotBlank()) {
                fixTransition(iconUrl)
            }
            val user = BundleCompat.getParcelable(bundle, BundleKey.User.name, User::class.java)
//            val iconTransitionName = bundle.getString(BundleKey.IconTransitionName.name)
//            binding.circleImageView.transitionName = iconTransitionName
            viewModel.user.value = user
        }

        viewModel.user.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            binding.toolbar.title = it.name
            binding.toolbar.subtitle = "@${it.username}"
            binding.handleNameTextView.text = it.name
            binding.verifiedDomainTextView.text = it.verified?.domain
            binding.profileDescriptionTextView.text = it.content.getSpannableStringBuilder(requireContext())
            binding.followingCountButton.text = resources.getQuantityString(R.plurals.following, it.counts.following, it.counts.following)
            binding.followerCountButton.text = resources.getQuantityString(R.plurals.follower, it.counts.followers, it.counts.followers)
        }
        viewModel.iconUrl.observe(viewLifecycleOwner) {
            BindingUtil.glideAvatarSrc(binding.circleImageView, it)
        }
        viewModel.usernameWithAt.observe(viewLifecycleOwner) {
            binding.screenNameTextView.text = it
        }
        viewModel.since.observe(viewLifecycleOwner) {
            binding.sinceTextView.text = getString(R.string.since, it)
        }
        viewModel.relation.observe(viewLifecycleOwner) {
            if (it != null && it > 0) binding.relationTextView.setText(it) else binding.relationTextView.text = ""
        }
        viewModel.toolbarTextColor.observe(viewLifecycleOwner) {
            binding.toolbar.setTitleTextColor(it)
            binding.toolbar.setSubtitleTextColor(it)
        }
        viewModel.toolbarBgColor.observe(viewLifecycleOwner) {
            binding.toolbar.setBackgroundColor(it)
        }
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.userMainActionButton.isEnabled = !it
            BindingUtil.setLoadingIndicator(binding.userMainActionButton, it)
        }
        viewModel.mainActionButtonText.observe(viewLifecycleOwner) {
            binding.userMainActionButton.text = it
        }
        viewModel.actionButtonTextColor.observe(viewLifecycleOwner) {
            binding.userMainActionButton.setTextColor(it)
        }
        viewModel.actionButtonTintColor.observe(viewLifecycleOwner) {
            BindingUtil.setBackgroundTint(binding.userMainActionButton, it)
        }
        viewModel.verifiedDomainVisibility.observe(viewLifecycleOwner) {
            binding.verifiedDomainTextView.visibility = it
        }

        binding.toolbar.let { toolbar ->
            toolbar.setNavigationOnClickListener { backToPrevFragment() }
            toolbar.setOnMenuItemClickListener { onMenuItemClick(it) }
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchUser()
        }
        binding.profileDescriptionTextView.setOnTouchListener(entityOnTouchListener)
        val pagerAdapter = ProfilePagerAdapter(requireContext(), childFragmentManager, userId)
        binding.profileViewPager.adapter = pagerAdapter
        binding.profileViewPagerTab.setupWithViewPager(binding.profileViewPager)

        binding.coverImageView.setOnClickListener { viewModel.showCover() }
        binding.userMainActionButton.setOnClickListener { viewModel.mainAction() }
        binding.verifiedDomainTextView.setOnClickListener { viewModel.openVerifiedDomain() }
        binding.followingCountButton.setOnClickListener { viewModel.openFollowingList() }
        binding.followerCountButton.setOnClickListener { viewModel.openFollowerList() }
        binding.circleImageView.setOnClickListener { viewModel.showAvatar() }

        toolbarSetup(binding.appBar, binding.swipeRefreshLayout)
//        setEnterSharedElementCallback(object : SharedElementCallback() {
//            override fun onMapSharedElements(
//                names: List<String>,
//                sharedElements: MutableMap<String, View>
//            ) {
//                binding.circleImageView.clipToOutline = true
//                sharedElements[names[0]] = binding.circleImageView
//            }
//        })

        val coverUrl = User.getCoverUrl(userId)
        Glide.with(this).load(coverUrl)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(20)))
            .into(binding.coverImageView)

//        binding.circleImageView.setShape(preferenceRepository.shapeOfAvatar)
//        binding.circleImageView.setBackgroundResource(preferenceRepository.shapeOfAvatar.drawableRes)

        return binding.root
    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuMessage -> openMessageFragment()
            R.id.menuBlock -> toggleBlock()
            R.id.menuMute -> toggleMute()
            R.id.menuShare -> share()
            R.id.menuShareUserPostsRss -> shareUserPostsRss()
            else -> return false
        }
        return true
    }

    private fun shareUserPostsRss() {
        activity?.let { ShareUtil.launchShareUrlIntent(it, userPostsRssUrl) }
    }

    private fun share() {
        val username = viewModel.user.value?.username ?: return
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, User.getCanonicalUrl(username))
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.share)))
    }

    private fun toggleMute() {
    }

    private fun toggleBlock() {
    }

    private fun openMessageFragment() {
    }

    private fun fixTransition(iconUrl: String) {
        Glide.with(requireContext())
            .load(iconUrl)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    startPostponedEnterTransition()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    startPostponedEnterTransition()
                    return false
                }
            })
            .onlyRetrieveFromCache(true)
            .into(binding.circleImageView)

    }

    private fun toolbarSetup(appBarLayout: AppBarLayout, swipeRefreshLayout: SwipeRefreshLayout) {
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBar: AppBarLayout, offset: Int ->
            val per = abs(offset).toFloat() / appBar.totalScrollRange.toFloat() * 255
            val textColor = viewModel.toolbarTextColor.value ?: return@OnOffsetChangedListener
            val bgColor = viewModel.toolbarBgColor.value ?: return@OnOffsetChangedListener
            swipeRefreshLayout.isEnabled = per == 0f
            viewModel.toolbarTextColor.postValue(
                ColorUtils.setAlphaComponent(
                    textColor,
                    per.toInt()
                )
            )
            viewModel.toolbarBgColor.postValue(ColorUtils.setAlphaComponent(bgColor, per.toInt()))
        })
        viewModel.toolbarBgColor.value =
            ContextCompat.getColor(requireContext(), R.color.colorStatusBar)

    }

    private fun openFollowerList(user: User) {
        val fragment = FollowingFollowerListFragment.FollowerListFragment.newInstance(user)
        addFragment(fragment, "follower")
    }

    private fun openFollowingList(user: User) {
        val fragment = FollowingFollowerListFragment.FollowingListFragment.newInstance(user)
        addFragment(fragment, "following")
    }

    private fun eventHandling(it: Event?) {
        if (it == null) return
        when (it) {
            is Event.FollowingList -> openFollowingList(it.user)
            is Event.FollowerList -> openFollowerList(it.user)
            is Event.EditProfile -> showEditProfileDialog()
            is Event.ShowAvatar -> it.url?.let { url -> showAvatar(url) }
            is Event.ShowCover -> it.url?.let { url -> showCover(url) }
            is Event.OpenVerifiedDomain -> openVerifiedDomain(it.url)
        }
    }

    private fun openVerifiedDomain(url: String) {
        context?.let { Util.openCustomTabUrl(it, url) }
    }

    private enum class TransitionName { Avatar, Cover }

    private fun showCover(url: String) {
        PhotoViewActivity.startActivity(
            activity,
            url,
            binding.coverImageView
        )
        initExitSharedElementCallback()

    }

    private fun initExitSharedElementCallback() = requireActivity().setExitSharedElementCallback(
        MaterialContainerTransformSharedElementCallback()
    )


    private fun showAvatar(url: String) {
        binding.circleImageView.transitionName = TransitionName.Avatar.name
        PhotoViewActivity.startActivity(
            requireActivity(),
            url,
            binding.circleImageView,
            transitionName = TransitionName.Avatar.name
        )
        initExitSharedElementCallback()
    }

    private val updateProfileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                viewModel.user.value = EditProfileFragment.parseResultIntent(it)
            }
        }
    }

//    private enum class RequestCode { UpdateProfile }

    private fun showEditProfileDialog() {
        val intent = EditProfileActivity.newIntent(requireContext(), userId)
        val activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(),
            binding.userMainActionButton,
            getString(R.string.shared_element_edit_profile)
        )
        updateProfileLauncher.launch(intent, activityOptions)
    }

    class ProfileViewModel(
        private val app: Application,
        private val getProfileUseCase: GetProfileUseCase,
        private val updateRelationshipUseCase: UpdateRelationshipUseCase,
        private val userId: String?
    ) : AndroidViewModel(app) {
        val event = SingleLiveEvent<Event>()
        val user = MutableLiveData<User>()
        val iconUrl: LiveData<String> = user.map {
            when {
                it != null -> it.getAvatarUrl(null)
                userId != null -> User.getAvatarUrl(userId, null)
                else -> ""
            }
        }
        val usernameWithAt: LiveData<String> = user.map { "@${it?.username}" }
        val since: LiveData<CharSequence?> = user.map {
            val calendar = Calendar.getInstance()
            if (it != null) calendar.time = it.createdAt
            DateFormat.format("yyyy/MM/dd", calendar)
        }
        val relation: LiveData<Int> = user.map {
            if (it == null) return@map 0
            if (it.youFollow && it.followsYou && !it.youCanFollow) {
                // it's me!
                R.string.its_me
            } else if (it.followsYou) {
                R.string.follows_you
            } else {
                0
            }
        }

        val toolbarTextColor = MutableLiveData<Int>().apply { value = Color.WHITE }
        val toolbarBgColor = MutableLiveData<Int>().apply { value = Color.TRANSPARENT }
        val loading = MutableLiveData<Boolean>().apply { value = false }
        val mainActionButtonText: LiveData<String> = user.map {
            when {
                it == null -> ""
                it.me -> app.getString(R.string.edit_profile)
                it.youFollow -> app.getString(R.string.unfollow)
                it.youBlocked -> app.getString(R.string.unblock)
                else -> app.getString(R.string.follow)
            }
        }
        val fetchingUser = MutableLiveData<Boolean>().apply { value = false }
        val actionButtonTextColor: LiveData<Int> = user.map {
            when (it?.youFollow) {
                false -> Util.getAccentColor(app)
                else -> Util.getWindowBackgroundColor(app)
            }
        }
        val actionButtonTintColor: LiveData<Int> = user.map {
            when (it?.youFollow) {
                true -> Util.getAccentColor(app)
                else -> Util.getWindowBackgroundColor(app)
            }
        }
//        val actionButtonRippleColor = Util.getPrimaryColorDark(app)
        val verifiedDomainVisibility: LiveData<Int> = user.map {
            if (it?.verified != null) View.VISIBLE else View.GONE
        }

        init {
            if (user.value == null) fetchUser()
        }

        fun openVerifiedDomain() =
            user.value?.verified?.let { event.emit(Event.OpenVerifiedDomain(it.url)) }

        fun fetchUser() {
            val id = userId ?: user.value?.id ?: return
            fetchingUser.value = true
            viewModelScope.launch {
                runCatching {
                    getProfileUseCase.run(GetProfileInputData(id))
                }.onSuccess {
                    user.postValue(it.res.data)
                }
                fetchingUser.postValue(false)
            }
        }

        fun openFollowerList() {
            val user = user.value ?: return
            event.value = Event.FollowerList(user)
        }

        fun openFollowingList() {
            val user = user.value ?: return
            event.value = Event.FollowingList(user)
        }

        fun mainAction() {
            when {
                user.value?.me == true -> {
                    event.value = Event.EditProfile
                }
                user.value?.youFollow == false -> follow()
                else -> unfollow()
            }
        }

        fun showAvatar() = event.emit(Event.ShowAvatar(user.value?.getAvatarUrl(null)))
        fun showCover() = event.emit(Event.ShowCover(user.value?.content?.coverImage?.url))
        private fun follow() = updateRelationship(true)
        private fun unfollow() = updateRelationship(false)
        private fun updateRelationship(follow: Boolean) {
            viewModelScope.launch {
                runCatching {
                    loading.postValue(true)
                    val user = user.value ?: return@launch
                    val relationship = if (follow) Relationship.Follow else Relationship.UnFollow
                    updateRelationshipUseCase.run(
                        UpdateRelationshipInputData(
                            user.id,
                            relationship
                        )
                    )
                }.onSuccess {
                    user.postValue(it.res.data)
                }
                loading.postValue(false)
            }
        }

        class Factory(
            private val application: Application,
            private val getProfileUseCase: GetProfileUseCase,
            private val updateRelationshipUseCase: UpdateRelationshipUseCase,
            private val userId: String?
        ) :
            ViewModelProvider.AndroidViewModelFactory(application) {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(
                    application,
                    getProfileUseCase,
                    updateRelationshipUseCase,
                    userId
                ) as T
            }
        }
    }

    sealed class Event {
        object EditProfile : Event()
        data class FollowerList(val user: User) : Event()
        data class FollowingList(val user: User) : Event()
        data class ShowAvatar(val url: String?) : Event()
        data class ShowCover(val url: String?) : Event()
        data class OpenVerifiedDomain(val url: String) : Event()
    }

    companion object {
        fun newInstance(
            id: String,
            iconUrl: String? = null,
            user: User? = null,
            iconTransitionName: String? = null
        ): Fragment = ProfileFragment().apply {
            arguments = Bundle().apply {
                putString(BundleKey.ID.name, id)
                putString(BundleKey.IconUrl.name, iconUrl)
                putParcelable(BundleKey.User.name, user)
                putString(BundleKey.IconTransitionName.name, iconTransitionName)
            }
        }
    }
}

