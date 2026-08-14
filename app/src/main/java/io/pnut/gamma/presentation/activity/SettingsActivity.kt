package io.pnut.gamma.presentation.activity

import io.pnut.gamma.R
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.preference.ListPreference
import androidx.preference.PreferenceManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityOptionsCompat
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.oss.licenses.v2.OssLicensesMenuActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.BuildConfig
import io.pnut.gamma.GammaApplication
import io.pnut.gamma.domain.repository.PnutCacheRepository
import io.pnut.gamma.domain.usecases.GetCurrentAccountUseCase
import io.pnut.gamma.domain.usecases.LogoutUseCase
import io.pnut.gamma.presentation.fragment.BasicDialogFragment
import io.pnut.gamma.presentation.fragment.ChoosePrimaryColorDialogFragment
import io.pnut.gamma.presentation.util.ColorSummaryProvider
import com.bumptech.glide.Glide
import io.pnut.gamma.presentation.util.ThemeColorUtil
import io.pnut.gamma.presentation.view.ThemeColorPreference
import io.pnut.gamma.service.ClearGlideCacheWorker
import io.pnut.gamma.service.ClearStreamCacheWorker
import io.pnut.gamma.util.Constants
import javax.inject.Inject
import androidx.core.net.toUri


@AndroidEntryPoint
class SettingsActivity : BaseActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        val args = pref.extras
        val fragmentName = pref.fragment ?: return false
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            fragmentName
        ).apply {
            arguments = args
        }
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_left,
                R.anim.slide_in_right,
                R.anim.slide_out_right
            )
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
        return true
    }

    @Inject
    lateinit var getCurrentAccountUseCase: GetCurrentAccountUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val currentAccount = getCurrentAccountUseCase.run(Unit).account ?: return

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, SettingsFragment.newInstance(currentAccount.screenName))
                .commit()
        }
        supportFragmentManager.addOnBackStackChangedListener {
            syncToolbar()
        }
        syncToolbar()
    }

    private fun syncToolbar() {
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        if (fragment is io.pnut.gamma.presentation.fragment.UserListFragment.SuggestedUserListFragment || 
            fragment is io.pnut.gamma.presentation.fragment.ProfileFragment) {
            supportActionBar?.hide()
        } else {
            supportActionBar?.show()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // settings list fragment
    @AndroidEntryPoint
    class SettingsFragment : BasePreferenceFragment() {
        private val username by lazy {
            "@${arguments?.getString(BundleKey.Username.name, "")}"
        }
        override val rootKey: Int = R.string.pref_settings_root_key

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_headers, rootKey)
            findPreference<Preference>(getString(R.string.pref_user_header_title_key))?.let {
                it.title = username
            }
            findPreference<Preference>(getString(R.string.pref_license_key))?.let {
                it.intent = Intent(context, OssLicensesMenuActivity::class.java)
            }
            findPreference<Preference>((getString(R.string.pref_version_key)))?.let {
                it.summary = BuildConfig.VERSION_NAME
                it.intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Constants.PLAY_STORE_URL.toUri()
                }
            }
            findPreference<Preference>("pref_privacy_policy_key")?.let {
                it.intent = Intent(Intent.ACTION_VIEW).apply {
                    data = "https://github.com/33mhz/gamma/blob/master/PRIVACY.md".toUri()
                }
            }
            findPreference<Preference>("pref_child_safety_key")?.let {
                it.intent = Intent(Intent.ACTION_VIEW).apply {
                    data = "https://github.com/33mhz/gamma/blob/master/CHILD_SAFETY.md".toUri()
                }
            }
        }

        private enum class BundleKey { Username }
        companion object {
            fun newInstance(username: String) = SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(BundleKey.Username.name, username)
                }
            }
        }
    }

    @AndroidEntryPoint
    abstract class BasePreferenceFragment : PreferenceFragmentCompat() {
        @Inject
        lateinit var preferenceRepository: io.pnut.gamma.domain.repository.IPreferenceRepository

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            requireActivity().addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    if (menuItem.itemId == android.R.id.home) {
                        startActivity(Intent(activity, SettingsActivity::class.java))
                        return true
                    }
                    return false
                }
            }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        }

        fun findPreference(@StringRes id: Int): Preference? = findPreference(getString(id))

        override fun onResume() {
            super.onResume()
            activity?.title = findPreference(rootKey)?.title
        }

        abstract val rootKey: Int
    }

    @AndroidEntryPoint
    class DisplayPreferenceFragment : BasePreferenceFragment(),
        ChoosePrimaryColorDialogFragment.Callback {
        override fun updateColor(themeColor: ThemeColorUtil.ThemeColor?) {
            themeColorPreference?.themeColor = themeColor
            recreateActivity()
        }

        override val rootKey: Int = R.string.pref_display_key

        override fun setAsDefault() {
            themeColorPreference?.themeColor = null
            recreateActivity()
        }

        private fun recreateActivity() {
            Handler(Looper.getMainLooper()).post {
                activity?.recreate()
            }
        }

        private fun showDialog() {
            val fragment =
                ChoosePrimaryColorDialogFragment.newInstance(themeColorPreference?.themeColor)
            fragment.show(childFragmentManager, DialogKey.ChoosePrimaryColorDialog.name)
        }

        private enum class DialogKey { ChoosePrimaryColorDialog }

        private val themeColorPreference by lazy {
            findPreference(R.string.pref_change_primary_color_key) as? ThemeColorPreference
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(
                R.xml.pref_display,
                getString(R.string.pref_display_key)
            )
            themeColorPreference?.let {
                context?.let { ctx -> it.summaryProvider = ColorSummaryProvider(ctx) }
                it.setOnPreferenceClickListener {
                    showDialog()
                    true
                }
            }
            (findPreference(R.string.pref_dark_theme_key) as? DropDownPreference)?.let {
                it.setOnPreferenceChangeListener { preference: Preference, _: Any ->
                    if (preference !is DropDownPreference) return@setOnPreferenceChangeListener false
                    activity?.let { activity ->
                        GammaApplication.getInstance(activity).updateBaseTheme()
                    }
                    Handler(Looper.getMainLooper()).post {
                        activity?.recreate()
                    }
                    true
                }
            }
        }
    }

    @AndroidEntryPoint
    class BehaviorPreferenceFragment : BasePreferenceFragment() {
        override val rootKey: Int = R.string.pref_behavior_key

        @Inject
        lateinit var getCurrentAccountUseCase: GetCurrentAccountUseCase

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(
                R.xml.pref_behavior,
                getString(R.string.pref_behavior_key)
            )

            val currentAccount = getCurrentAccountUseCase.run(Unit).account
            if (currentAccount != null && preferenceRepository.hasExceededWelcomeFollowed(currentAccount.id)) {
                findPreference<Preference>(getString(R.string.pref_suggest_user_follows_key))?.apply {
                    isVisible = false
                    isEnabled = false
                }
            }
        }
    }

    @AndroidEntryPoint
    class StreamPreferenceFragment : BasePreferenceFragment(),
        ClearStreamCacheWorker.Receiver.Listener, ClearGlideCacheWorker.Receiver.Listener {
        override fun onClearGlideCache() {
            clearGlideCacheButton?.isEnabled = false
            val contentView = activity?.findViewById<View>(R.id.container) ?: return
            Snackbar.make(contentView, R.string.cache_cleared, Snackbar.LENGTH_SHORT).show()
        }

        override fun onClearStreamCache() {
            clearStreamCacheButton?.isEnabled = false
            val contentView = activity?.findViewById<View>(R.id.container) ?: return
            Snackbar.make(contentView, R.string.cache_cleared, Snackbar.LENGTH_SHORT).show()
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(
                R.xml.pref_stream,
                getString(R.string.pref_stream_key)
            )
        }

        private val clearStreamCacheReceiver by lazy {
            ClearStreamCacheWorker.Receiver(this)
        }

        private val clearGlideCacheReceiver by lazy {
            ClearGlideCacheWorker.Receiver(this)
        }

        private val clearStreamCacheButton by lazy {
            findPreference(R.string.pref_clear_stream_cache_key)
        }

        private val clearGlideCacheButton by lazy {
            findPreference(R.string.pref_clear_glide_cache_key)
        }


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            clearStreamCacheButton?.setOnPreferenceClickListener {
                val fragment = BasicDialogFragment.Builder()
                    .setTitle(R.string.pref_clear_stream_cache_title)
                    .setMessage(R.string.this_operation_cannot_be_undone)
                    .setPositive(R.string.ok)
                    .setNegative(R.string.cancel)
                    .setRequestKey(RequestCode.ClearStreamCache.name)
                    .build()
                fragment.show(childFragmentManager, DialogKey.ClearStreamCache.name)
                false
            }
            clearGlideCacheButton?.setOnPreferenceClickListener {
                val fragment = BasicDialogFragment.Builder()
                    .setTitle(R.string.pref_clear_glide_cache_title)
                    .setMessage(R.string.this_operation_cannot_be_undone)
                    .setPositive(R.string.ok)
                    .setNegative(R.string.cancel)
                    .setRequestKey(RequestCode.ClearGlideCache.name)
                    .build()
                fragment.show(childFragmentManager, DialogKey.ClearGlideCache.name)
                false
            }
            clearGlideCacheButton?.isEnabled =
                context?.let { Glide.getPhotoCacheDir(it)?.exists() } ?: true
            clearStreamCacheButton?.isEnabled =
                context?.let { PnutCacheRepository.getUserCacheDir(it).exists() } ?: true

            childFragmentManager.setFragmentResultListener(RequestCode.ClearStreamCache.name, this) { _, bundle ->
                if (bundle.getInt(BasicDialogFragment.ResponseKey.ResultCode.name) == RESULT_OK) {
                    context?.let { ClearStreamCacheWorker.enqueue(it) }
                }
            }
            childFragmentManager.setFragmentResultListener(RequestCode.ClearGlideCache.name, this) { _, bundle ->
                if (bundle.getInt(BasicDialogFragment.ResponseKey.ResultCode.name) == RESULT_OK) {
                    context?.let { ClearGlideCacheWorker.enqueue(it) }
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onResume() {
            super.onResume()
            activity?.registerReceiver(
                clearStreamCacheReceiver,
                ClearStreamCacheWorker.intentFilter,
                RECEIVER_NOT_EXPORTED
            )
            activity?.registerReceiver(
                clearGlideCacheReceiver,
                ClearGlideCacheWorker.intentFilter,
                RECEIVER_NOT_EXPORTED
            )
        }

        override fun onStop() {
            super.onStop()
            activity?.unregisterReceiver(clearStreamCacheReceiver)
            activity?.unregisterReceiver(clearGlideCacheReceiver)
        }

        private enum class RequestCode { ClearStreamCache, ClearGlideCache }
        private enum class DialogKey { ClearStreamCache, ClearGlideCache }

        override val rootKey: Int = R.string.pref_stream_key
    }

    @AndroidEntryPoint
    class UserPreferenceFragment : BasePreferenceFragment() {
        override val rootKey: Int = R.string.pref_user_key
        @Inject
        lateinit var logoutUseCase: LogoutUseCase

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            findPreference(R.string.pref_logout_user_key)?.apply {
                setOnPreferenceClickListener { logout() }
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_user, rootKey)
        }

        private fun logout(): Boolean {
            val anotherAccountId = logoutUseCase.run(Unit).anotherAccountId
            activity?.finish()
            val intentClass = if (anotherAccountId != null) {
                MainActivity::class
            } else {
                LoginActivity::class
            }
            val newIntent = Intent(activity, intentClass.java).also {
                it.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val options = activity?.let {
                ActivityOptionsCompat.makeCustomAnimation(it, R.anim.scale_up, R.anim.scale_down)
            }
            startActivity(newIntent, options?.toBundle())
            return true
        }
    }

    companion object {

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener =
            Preference.OnPreferenceChangeListener { preference, value ->
                val stringValue = value.toString()

                if (preference is ListPreference) {
                    // For list preferences, look up the correct display value in
                    // the preference's 'entries' list.
                    val index = preference.findIndexOfValue(stringValue)

                    // Set the summary to reflect the new value.
                    preference.setSummary(
                        if (index >= 0)
                            preference.entries[index]
                        else
                            null
                    )

                } else {
                    // For all other preferences, set the summary to the value's
                    // simple string representation.
                    preference.summary = stringValue
                }
                true
            }

        /**
         * Helper method to determine if the device has an extra-large screen. For
         * example, 10" tablets are extra-large.
         */
        private fun isXLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_NORMAL &&
                    context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.

         * @see .sBindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager
                    .getDefaultSharedPreferences(preference.context)
                    .getString(preference.key, "")
            )
        }
    }
}
