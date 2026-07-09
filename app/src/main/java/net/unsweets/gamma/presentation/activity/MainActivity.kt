package net.unsweets.gamma.presentation.activity

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
import net.unsweets.gamma.R
import net.unsweets.gamma.broadcast.ErrorReceiver
import net.unsweets.gamma.broadcast.PostReceiver
import net.unsweets.gamma.databinding.AccountListBinding
import net.unsweets.gamma.databinding.ActivityMainBinding
import net.unsweets.gamma.databinding.NavigationDrawerHeaderBinding
import net.unsweets.gamma.domain.entity.Post
import net.unsweets.gamma.domain.entity.User
import net.unsweets.gamma.domain.model.Account
import net.unsweets.gamma.domain.model.io.UpdateDefaultAccountInputData
import net.unsweets.gamma.domain.usecases.GetAccountListUseCase
import net.unsweets.gamma.domain.usecases.GetAuthenticatedUserUseCase
import net.unsweets.gamma.domain.usecases.GetCurrentAccountUseCase
import net.unsweets.gamma.domain.usecases.UpdateDefaultAccountUseCase
import net.unsweets.gamma.presentation.adapter.AccountListAdapter
import net.unsweets.gamma.presentation.fragment.BaseListFragment
import net.unsweets.gamma.presentation.fragment.ChannelListFragment
import net.unsweets.gamma.presentation.fragment.ChannelsFragment
import net.unsweets.gamma.presentation.fragment.ExploreFragment
import net.unsweets.gamma.presentation.fragment.HomeFragment
import net.unsweets.gamma.presentation.fragment.ProfileFragment
import net.unsweets.gamma.presentation.fragment.SearchFragment
import net.unsweets.gamma.presentation.util.BindingUtil
import net.unsweets.gamma.presentation.util.FragmentHelper.addFragment
import net.unsweets.gamma.presentation.util.LoginUtil
import net.unsweets.gamma.presentation.util.SnackbarCallback
import net.unsweets.gamma.presentation.util.Util
import net.unsweets.gamma.presentation.viewmodel.MainActivityViewModel
import net.unsweets.gamma.service.PostService
import net.unsweets.gamma.util.ErrorIntent
import net.unsweets.gamma.util.LogUtil
import net.unsweets.gamma.util.oneline
import net.unsweets.gamma.util.showAsError
import javax.inject.Inject
import androidx.core.view.size
import androidx.core.view.get

@AndroidEntryPoint
class MainActivity : BaseActivity(), BaseActivity.HaveDrawer, PostReceiver.Callback,
    AccountListAdapter.Listener, ErrorReceiver.Callback {
    override fun onReceiveError(message: String) {
        val view = findViewById<View>(android.R.id.content) ?: return
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).showAsError()
    }

    override fun onDeletePostReceive(post: Post) {
        showActionResultSnackBar(post, Action.Delete)
    }

    override fun onAccountClick(account: Account) {
        if (currentAccount == account) return closeDrawer()
        updateDefaultAccountUseCase.run(UpdateDefaultAccountInputData(account.id))
        val restartIntent = intent
        finish()
        overridePendingTransition(R.anim.scale_up, R.anim.scale_down)
        startActivity(restartIntent)
    }

    override fun onAddAccount() {
        closeDrawer()
        val newIntent = LoginUtil.getLoginIntent(this)
        startActivity(newIntent)
    }

    override fun onRepostReceive(post: Post) {
        showActionResultSnackBar(
            post,
            Action.Repost,
            SnackbarCallback(R.string.undo, View.OnClickListener {
                val currentState = post.youReposted ?: true
                PostService.newRepostIntent(this, post.id, !currentState)
            })
        )
    }

    override fun onStarReceive(post: Post) {
        showActionResultSnackBar(
            post,
            Action.Star,
            SnackbarCallback(R.string.undo, View.OnClickListener {
                val currentState = post.youBookmarked ?: true
                PostService.newStarIntent(this, post.id, !currentState)
            })
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
        val actionName = getString(actionNameRes)
        val username = post.mainPost.user?.username ?: return
        val content = post.content?.text ?: return
        val message =
            getString(R.string.action_result_snackbar_template, actionName, username, content)
        showSnackBar(message, snackbarCallback)
    }

    override fun onPostReceive(post: Post) {
        val text = post.content?.text ?: return
        showSnackBar(getString(R.string.posted, text))
    }

    private fun showSnackBar(text: String, snackbarCallback: SnackbarCallback? = null) {
        val view = findViewById<View>(android.R.id.content) ?: return
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT).oneline().apply {
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
        val headerView = binding.navigationView.getHeaderView(0)
        val headerBinding = NavigationDrawerHeaderBinding.bind(headerView)
        
        headerBinding.switchAccountIndicatorImageView.let { imageView ->
            val avd = AnimatedVectorDrawableCompat.create(this, res) ?: return@let
            imageView.setImageDrawable(avd)
            avd.start()
        }
        when (showAccountMenu) {
            true -> {
                binding.navigationView.getHeaderView(ACCOUNTHEADERPOSITION).visibility =
                    View.VISIBLE
                setMenuItemVisibilities(false)
            }
            false -> {
                binding.navigationView.getHeaderView(ACCOUNTHEADERPOSITION).visibility = View.GONE
                setMenuItemVisibilities(true)
            }
        }
    }

    private fun setMenuItemVisibilities(visible: Boolean) {
        binding.navigationView.menu.let {
            for (i in 0 until it.size) {
                it[i].isVisible = visible
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
        LogUtil.e("supportFinishAfterTransition")
        binding.fab.invalidate()
        binding.bottomAppBar.performHide()
    }

    override fun postponeEnterTransition() {
        super.postponeEnterTransition()
        LogUtil.e("supportFinishAfterTransition")
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


    private val eventObserver = Observer<MainActivity.Event> {
        when (it) {
            is MainActivity.Event.ComposePost -> openComposePostDialog()
            is MainActivity.Event.OpenMyProfile -> openMyProfile(it.user)
            is MainActivity.Event.Failed -> LogUtil.e(it.t.message)
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
    }

    private fun setupBottomAppBar() {
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
            addFragment(
                supportFragmentManager,
                SearchFragment.newInstance(),
                SearchFragment::class.java.simpleName
            )
        (existFragment as? SearchFragment)?.focusToEditText()
    }


    override fun onStart() {
        super.onStart()
        receiverManager.registerReceiver(postReceiver, PostService.getIntentFilter())
        receiverManager.registerReceiver(errorReceiver, ErrorIntent.getIntentFilter())
    }

    override fun onStop() {
        super.onStop()
        receiverManager.unregisterReceiver(postReceiver)
        receiverManager.unregisterReceiver(errorReceiver)
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

                    BindingUtil.glideSrc(headerBinding.navigationDrawerHeaderImageView, it.content.coverImage.link)
                    BindingUtil.glideAvatarSrc(headerBinding.navigationDrawerAvatarImageView, it.content.avatarImage.link)
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
        val fragment =
            supportFragmentManager.findFragmentById(R.id.fragmentPlaceholder) as? Util.DrawerContentFragment
                ?: return
        binding.navigationView.menu.findItem(fragment.menuItemId)?.isChecked = true
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
            val fragment = ProfileFragment.newInstance(user.id, user.content.avatarImage.link, user)
            addFragment(supportFragmentManager, fragment, user.id)
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
                R.id.conversations,
                R.id.missedConversations,
                R.id.newcomers,
                R.id.photos,
                R.id.trending,
                R.id.global -> showExploreStream(item.itemId)
//                    R.id.file -> goToFiles()
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
        addFragment(supportFragmentManager, fragment, tag)
    }

    private fun goToChannels() {
        val tag = ChannelsFragment::class.java.simpleName
        val cache = supportFragmentManager.findFragmentByTag(tag)
        val fragment = cache ?: ChannelsFragment.newInstance()
        addFragment(supportFragmentManager, fragment, tag)
    }

    private fun goToSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun goToFiles() {
        val intent = Intent(this, FilesActivity::class.java)
        startActivity(intent)
    }

    sealed class Event {
        object ComposePost : Event()
        data class OpenMyProfile(val user: User) : Event()
        data class Failed(val t: Throwable) : Event()
    }

    private val fragmentMap = mapOf<Int, () -> BaseListFragment<*, *>>(
        R.id.conversations to { ChannelListFragment.privateChannels() },
        R.id.missedConversations to { ExploreFragment.MissedConversationsFragment.newInstance() },
        R.id.newcomers to { ExploreFragment.NewcomersFragment.newInstance() },
        R.id.photos to { ExploreFragment.PhotosFragment.newInstance() },
        R.id.trending to { ExploreFragment.TrendingFragment.newInstance() },
        R.id.global to { ExploreFragment.GlobalFragment.newInstance() }
    )

    companion object {
        private const val ACCOUNTHEADERPOSITION = 1
    }
}
