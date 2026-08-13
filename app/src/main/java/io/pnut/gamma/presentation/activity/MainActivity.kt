package io.pnut.gamma.presentation.activity

import io.pnut.gamma.R
import android.app.ActivityOptions
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.broadcast.ErrorReceiver
import io.pnut.gamma.broadcast.PostReceiver
import io.pnut.gamma.databinding.AccountListBinding
import io.pnut.gamma.databinding.ActivityMainBinding
import io.pnut.gamma.databinding.NavigationDrawerHeaderBinding
import io.pnut.gamma.domain.entity.Post
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.domain.model.Account
import io.pnut.gamma.domain.model.io.UpdateDefaultAccountInputData
import io.pnut.gamma.domain.usecases.GetAccountListUseCase
import io.pnut.gamma.domain.usecases.GetAuthenticatedUserUseCase
import io.pnut.gamma.domain.usecases.GetCurrentAccountUseCase
import io.pnut.gamma.domain.usecases.UpdateDefaultAccountUseCase
import io.pnut.gamma.presentation.adapter.AccountListAdapter
import io.pnut.gamma.presentation.fragment.ChannelsFragment
import io.pnut.gamma.presentation.fragment.PrivateMessagesFragment
import io.pnut.gamma.presentation.fragment.ChannelMessagesFragment
import io.pnut.gamma.presentation.fragment.ExploreFragment
import io.pnut.gamma.presentation.fragment.HomeFragment
import io.pnut.gamma.presentation.fragment.ProfileFragment
import io.pnut.gamma.presentation.fragment.SearchFragment
import io.pnut.gamma.presentation.util.BindingUtil
import io.pnut.gamma.presentation.util.LoginUtil
import io.pnut.gamma.presentation.util.SnackbarCallback
import io.pnut.gamma.presentation.util.Util
import io.pnut.gamma.presentation.viewmodel.MainActivityViewModel
import io.pnut.gamma.service.PostWorker
import io.pnut.gamma.util.ErrorIntent
import io.pnut.gamma.util.LogUtil
import io.pnut.gamma.util.oneLine
import io.pnut.gamma.util.showAsError
import javax.inject.Inject
import androidx.core.view.size
import androidx.core.view.get
import io.pnut.gamma.presentation.util.FragmentHelper
import android.os.Build
import androidx.fragment.app.Fragment
import io.pnut.gamma.presentation.fragment.ExplorePostsFragment
import io.pnut.gamma.presentation.fragment.ExploreRoomsFragment

@AndroidEntryPoint
class MainActivity : BaseActivity(), BaseActivity.HaveDrawer, PostReceiver.Callback,
    AccountListAdapter.Listener, ErrorReceiver.Callback {
    override fun onReceiveError(message: String) {
        Snackbar.make(binding.coordinatorLayout, message, Snackbar.LENGTH_LONG).showAsError()
    }

    override fun onDeletePostReceive(post: Post) {
        showActionResultSnackBar(post, Action.Delete)
    }

    override fun onReportPostReceive() {
        showSnackBar(getString(R.string.report_successful), duration = Snackbar.LENGTH_LONG)
    }

    override fun onAccountClick(account: Account) {
        if (currentAccount == account) return closeDrawer()
        updateDefaultAccountUseCase.run(UpdateDefaultAccountInputData(account.id))
        val restartIntent = Intent(this, MainActivity::class.java)
        finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, R.anim.scale_up, R.anim.scale_down)
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.scale_up, R.anim.scale_down)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.scale_up, R.anim.scale_down)
        }
        startActivity(restartIntent)
    }

    override fun onAddAccount() {
        closeDrawer()
        LoginUtil.launchLogin(this, isEphemeral = true)
    }

    override fun onRepostReceive(post: Post) {
        showActionResultSnackBar(
            post,
            Action.Repost,
            SnackbarCallback(R.string.undo) {
                val currentState = post.youReposted ?: true
                PostWorker.enqueueRepost(this, post.id, !currentState)
            }
        )
    }

    override fun onStarReceive(post: Post) {
        showActionResultSnackBar(
            post,
            Action.Star,
            SnackbarCallback(R.string.undo) {
                val currentState = post.youBookmarked ?: true
                PostWorker.enqueueStar(this, post.id, !currentState)
            }
        )
    }

    private enum class Action { Star, Repost, Delete }

    @Inject
    lateinit var getAccountListUseCase: GetAccountListUseCase
    @Inject
    lateinit var updateDefaultAccountUseCase: UpdateDefaultAccountUseCase
    @Inject
    lateinit var getCurrentAccountUseCase: GetCurrentAccountUseCase

    private val currentAccount: Account? by lazy {
        getCurrentAccountUseCase.run(Unit).account
    }
    private val accounts
        get() = getAccountListUseCase.run(Unit).accounts.filterNot { it == currentAccount }

    private fun showActionResultSnackBar(
        post: Post,
        action: Action,
        snackbarCallback: SnackbarCallback? = null
    ) {
        val actionNameRes = when (action) {
            Action.Star -> if (post.mainPost.youBookmarked == true) R.string.stars else R.string.unstar
            Action.Repost -> if (post.mainPost.youReposted == true) R.string.repost else R.string.delete_repost
            Action.Delete -> R.string.delete
        }
        val actionName: String = getString(actionNameRes)
        val username: String = post.mainPost.user?.username ?: return
        val content: String = post.content?.text ?: return
        val message: String =
            getString(R.string.action_result_snackbar_template, actionName, username, content)
        showSnackBar(message, snackbarCallback)
    }

    override fun onPostReceive(post: Post) {
        val text = post.content?.text ?: return
        showSnackBar(getString(R.string.posted, text))
    }

    private fun showSnackBar(text: String, snackbarCallback: SnackbarCallback? = null, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(binding.coordinatorLayout, text, duration).oneLine().apply {
            setAnchorView(R.id.fab)
            if (snackbarCallback != null) setAction(
                snackbarCallback.actionResId,
                snackbarCallback.callback
            )
        }.show()
    }

    private lateinit var binding: ActivityMainBinding

    private val accountListView by lazy {
        val accountListBinding = AccountListBinding.inflate(layoutInflater, binding.navigationView, false)
        accountListBinding.accountList.adapter = AccountListAdapter(accounts, this, true)
        accountListBinding.accountList.layoutManager = LinearLayoutManager(this)
        accountListBinding.root
    }


    private val showAccountMenuObserver = Observer<Boolean> { showAccountMenu ->
        val res = if (showAccountMenu) R.drawable.ic_arrow_drop_up_to_down else R.drawable.ic_arrow_drop_down_to_up
        val headerView: View = binding.navigationView.getHeaderView(0)
        val headerBinding: NavigationDrawerHeaderBinding = NavigationDrawerHeaderBinding.bind(headerView)
        
        headerBinding.switchAccountIndicatorImageView.let { imageView ->
            val avd: AnimatedVectorDrawableCompat = AnimatedVectorDrawableCompat.create(this, res) ?: return@let
            imageView.setImageDrawable(avd)
            avd.start()
        }
        when (showAccountMenu) {
            true -> {
                binding.navigationView.getHeaderView(ACCOUNT_HEADER_POSITION).visibility =
                    View.VISIBLE
                setMenuItemVisibilities(false)
            }
            false -> {
                binding.navigationView.getHeaderView(ACCOUNT_HEADER_POSITION).visibility = View.GONE
                setMenuItemVisibilities(true)
            }
        }
    }

    private fun setMenuItemVisibilities(visible: Boolean) {
        setMenuItemVisibilities(binding.navigationView.menu, visible)
    }

    private fun setMenuItemVisibilities(menu: Menu, visible: Boolean) {
        for (i in 0 until menu.size) {
            val item = menu[i]
            item.isVisible = visible
            if (item.hasSubMenu()) {
                item.subMenu?.let { setMenuItemVisibilities(it, visible) }
            }
        }
    }

    private val drawerToggle: ActionBarDrawerToggle by lazy {
        object : ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.bottomAppBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ) {
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                viewModel.showAccountMenu.value = false
            }
        }
    }

    override fun supportFinishAfterTransition() {
        super.supportFinishAfterTransition()
        LogUtil.d("supportFinishAfterTransition")
        binding.fab.invalidate()
        binding.bottomAppBar.performHide()
    }

    override fun postponeEnterTransition() {
        super.postponeEnterTransition()
        LogUtil.d("supportFinishAfterTransition")
    }
    @Inject
    lateinit var getAuthenticatedUserUseCase: GetAuthenticatedUserUseCase
    private val viewModel: MainActivityViewModel by lazy {
        ViewModelProvider(this, MainActivityViewModel.Factory(getAuthenticatedUserUseCase))[MainActivityViewModel::class.java]
    }

    private val postReceiver by lazy {
        PostReceiver(this)
    }

    private val errorReceiver by lazy {
        ErrorReceiver(this)
    }

    private val receiverManager by lazy {
        LocalBroadcastManager.getInstance(this)
    }


    private val eventObserver = Observer<Event> {
        when (it) {
            is Event.ComposePost -> openComposePostDialog()
            is Event.OpenMyProfile -> openMyProfile(it.user)
            is Event.Failed -> LogUtil.e(it.t.message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementsUseOverlay = false
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel.event.observe(this, eventObserver)
        viewModel.showAccountMenu.observe(this, showAccountMenuObserver)

        setupNavigationView()
        setupFragment(savedInstanceState == null)
        handleIntent(intent)
        setupNavigation()
        setupBottomAppBar()

        binding.fab.setOnClickListener {
            viewModel.composePost()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerVisible(GravityCompat.START)) {
                    closeDrawer()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        syncMenu()
    }

    private fun setupBottomAppBar() {
        binding.bottomAppBar.menu.findItem(R.id.menuSearch)?.actionView?.setOnClickListener {
            showSearchFragment()
        }

        binding.bottomAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menuSearch -> {
                    showSearchFragment()
                    true
                }
                else -> false
            }
        }
    }

    private fun showSearchFragment() {
        val existFragment =
            FragmentHelper.addFragment(
                supportFragmentManager,
                SearchFragment.newInstance(),
                SearchFragment::class.java.simpleName
            )
        (existFragment as? SearchFragment)?.focusToEditText()
    }


    override fun onStart() {
        super.onStart()
        receiverManager.registerReceiver(postReceiver, PostWorker.getIntentFilter())
        receiverManager.registerReceiver(errorReceiver, ErrorIntent.getIntentFilter())
    }

    override fun onStop() {
        super.onStop()
        receiverManager.unregisterReceiver(postReceiver)
        receiverManager.unregisterReceiver(errorReceiver)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val channelId = intent.getStringExtra("CHANNEL_ID")
        val channelTitle = intent.getStringExtra("CHANNEL_TITLE") ?: "PM"
        val usernames = intent.getStringArrayListExtra("USERNAMES")
        if (channelId != null) {
            val fragment = ChannelMessagesFragment.newInstance(channelId, channelTitle, io.pnut.gamma.domain.model.ChannelType.PM.value, usernames)
            FragmentHelper.addFragment(supportFragmentManager, fragment, channelId)
        }
    }

    private fun setupFragment(firstStart: Boolean) {
        if (firstStart) {
            val homeFragment = HomeFragment.newInstance()

            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentPlaceholder, homeFragment)
                .commit()
        }
        supportFragmentManager.addOnBackStackChangedListener(::syncMenu)
    }


    private fun setupNavigationView() {
        val header = binding.navigationView.getHeaderView(0)
        NavigationDrawerHeaderBinding.bind(header).let { headerBinding ->
            viewModel.user.observe(this) { user ->
                user?.let {
                    headerBinding.navigationDrawerHandleNameTextView.text = it.name
                    headerBinding.navigationDrawerScreenNameTextView.text = getString(R.string.user_name_format, it.username)

                    BindingUtil.glideSrc(headerBinding.navigationDrawerHeaderImageView, it.content.coverImage.url)
                    BindingUtil.glideAvatarSrc(headerBinding.navigationDrawerAvatarImageView, it.content.avatarImage.url)
                }
            }
            
            headerBinding.root.setOnClickListener {
                viewModel.openMyProfile()
            }
            
            headerBinding.navigationDrawerHeaderClickTarget.setOnClickListener {
                viewModel.toggleNavigationViewMenu()
            }
        }
        binding.navigationView.setNavigationItemSelectedListener(::onOptionsItemSelected)
        binding.navigationView.addHeaderView(accountListView)
    }

    private fun syncMenu() {
        uncheckMenuItem(binding.navigationView.menu)
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentPlaceholder)
        
        val fabIcon = when (fragment) {
            is PrivateMessagesFragment -> R.drawable.ic_mail_black_24dp
            is ChannelMessagesFragment if fragment.isPm -> R.drawable.ic_mail_black_24dp
            is ChannelMessagesFragment -> R.drawable.ic_forum_black_24dp
            else -> R.drawable.ic_create_black_24dp
        }
        binding.fab.setImageResource(fabIcon)

        val drawerFragment = fragment as? Util.DrawerContentFragment ?: return
        binding.navigationView.menu.findItem(drawerFragment.menuItemId)?.let {
            it.isVisible = true
            it.isChecked = true
        }
    }

    private fun uncheckMenuItem(menu: Menu) {
        val size = menu.size
        for (i in 0 until size) {
            val item = menu[i]
            if (item.hasSubMenu()) {
                item.subMenu?.let { uncheckMenuItem(it) }
            } else {
                item.isChecked = false
            }
        }
    }

    private fun openComposePostDialog() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentPlaceholder)
        if (fragment is PrivateMessagesFragment || fragment is ChannelMessagesFragment && fragment.isPm) {
            val usernames = (fragment as? ChannelMessagesFragment)?.getUsernames()
            val intent = ComposeMessageActivity.newIntentForNewPm(this, usernames)
            val options = ActivityOptions.makeSceneTransitionAnimation(this, binding.fab, getString((R.string.shared_element_compose)))
            startActivity(intent, options.toBundle())
            return
        }

        if (fragment is ChannelMessagesFragment) {
            val intent = ComposeMessageActivity.newIntent(this, channelId = fragment.channelId, channelTitle = fragment.title)
            val options = ActivityOptions.makeSceneTransitionAnimation(this, binding.fab, getString((R.string.shared_element_compose)))
            startActivity(intent, options.toBundle())
            return
        }

        val intent = ComposePostActivity.newIntent(this)
        val options = ActivityOptions.makeSceneTransitionAnimation(this, binding.fab, getString((R.string.shared_element_compose)))
        startActivity(intent, options.toBundle())
    }

    private fun setupNavigation() {
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    private fun openMyProfile(user: User) {
        closeDrawer()

        Handler(Looper.getMainLooper()).postDelayed({
            val fragment = ProfileFragment.newInstance(user.id, user.content.avatarImage.url, user)
            FragmentHelper.addFragment(supportFragmentManager, fragment, user.id)
            uncheckMenuItem(binding.navigationView.menu)
        }, 200)
    }

    override fun closeDrawer() = binding.drawerLayout.closeDrawer(GravityCompat.START)

    override fun openDrawer() = binding.drawerLayout.openDrawer(GravityCompat.START)

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }


    private fun goToHome() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (drawerToggle.onOptionsItemSelected(item))
            true
        else {
            when (item.itemId) {
                R.id.home -> goToHome()
                R.id.explorePosts,
                R.id.exploreRooms,
                R.id.global -> showExploreStream(item.itemId)
//                    R.id.file -> goToFiles()
                R.id.privateMessages -> goToPrivateMessages()
                R.id.channels -> goToChannels()
                R.id.settings -> goToSettings()
            }
            closeDrawer()
            true
        }
    }

    private fun showExploreStream(menuId: Int) {
        val tag = menuId.toString()
        val cache = supportFragmentManager.findFragmentByTag(tag)
        val fragment = cache ?: fragmentMap[menuId]?.let { it() } ?: return
        FragmentHelper.addFragment(supportFragmentManager, fragment, tag)
    }

    private fun goToChannels() {
        val tag = ChannelsFragment::class.java.simpleName
        val cache = supportFragmentManager.findFragmentByTag(tag)
        val fragment = cache ?: ChannelsFragment.newInstance()
        FragmentHelper.addFragment(supportFragmentManager, fragment, tag)
    }

    private fun goToPrivateMessages() {
        val tag = PrivateMessagesFragment::class.java.simpleName
        val cache = supportFragmentManager.findFragmentByTag(tag)
        val fragment = cache ?: PrivateMessagesFragment.newInstance()
        FragmentHelper.addFragment(supportFragmentManager, fragment, tag)
    }

    private fun goToSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

//    private fun goToFiles() {
//        val intent = Intent(this, FilesActivity::class.java)
//        startActivity(intent)
//    }

    sealed class Event {
        object ComposePost : Event()
        data class OpenMyProfile(val user: User) : Event()
        data class Failed(val t: Throwable) : Event()
    }

    private val fragmentMap = mapOf<Int, () -> Fragment>(
        R.id.explorePosts to { ExplorePostsFragment.newInstance() },
        R.id.exploreRooms to { ExploreRoomsFragment.newInstance() },
        R.id.global to { ExploreFragment.GlobalFragment.newInstance() }
    )

    companion object {
        private const val ACCOUNT_HEADER_POSITION = 1
    }
}
