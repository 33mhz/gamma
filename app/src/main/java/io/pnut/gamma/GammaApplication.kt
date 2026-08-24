package io.pnut.gamma

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.repository.IPnutRepository
import io.pnut.gamma.domain.repository.IPreferenceRepository
import io.pnut.gamma.domain.usecases.SetupTokenUseCase
import io.pnut.gamma.presentation.activity.LoginActivity
import io.pnut.gamma.presentation.util.ThemeColorUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.asExecutor
import javax.inject.Inject


open class GammaApplication : Application(), CoroutineScope by MainScope(), Configuration.Provider {

  lateinit var preferenceRepository: IPreferenceRepository
  lateinit var pnutRepository: IPnutRepository
  lateinit var accountRepository: IAccountRepository
  lateinit var workerFactory: HiltWorkerFactory
  
  private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
    if (key == getString(R.string.pref_username_autocomplete_key)) {
      val enabled = preferenceRepository.usernameAutocomplete
      scheduleUserSuggestionSync(forceSync = enabled)
    }
  }

  override val workManagerConfiguration: Configuration
    get() = Configuration.Builder()
      .setWorkerFactory(workerFactory)
      .build()

  fun initApplication() {
    updateBaseTheme()
    updateTheme()
    preferenceRepository.onRegisterChangePreference(preferenceChangeListener)
    runCatching {
      val config = BundledEmojiCompatConfig(this, Dispatchers.IO.asExecutor())
        .setReplaceAll(true)
      EmojiCompat.init(config)
    }
  }

  fun updateBaseTheme() {
    val darkMode = preferenceRepository.darkMode
    AppCompatDelegate.setDefaultNightMode(darkMode.value)
  }

  private fun backToLoginActivity() {
    val newIntent = Intent(applicationContext, LoginActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(newIntent)
  }

  private fun setToken(): Boolean {
    return SetupTokenUseCase(
      pnutRepository,
      accountRepository
    ).run(Unit).existDefaultAccount
  }

  fun updateTheme() {
    ThemeColorUtil.applyTheme(this)
  }

  protected fun scheduleUserSuggestionSync(forceSync: Boolean = false) {
    if (!preferenceRepository.usernameAutocomplete) {
        io.pnut.gamma.util.LogUtil.d("Cancelling user suggestion sync")
        androidx.work.WorkManager.getInstance(this).cancelUniqueWork("UserSuggestionSync")
        return
    }

    io.pnut.gamma.util.LogUtil.d("Scheduling user suggestion sync (forceSync=$forceSync)")
    val constraints = androidx.work.Constraints.Builder()
        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
        .build()

    val periodicRequest = androidx.work.PeriodicWorkRequestBuilder<io.pnut.gamma.service.UserSuggestionWorker>(
        3, java.util.concurrent.TimeUnit.DAYS
    )
        .setConstraints(constraints)
        .build()

    androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "UserSuggestionSync",
        androidx.work.ExistingPeriodicWorkPolicy.KEEP,
        periodicRequest
    )

    if (forceSync) {
        val oneTimeRequest = androidx.work.OneTimeWorkRequestBuilder<io.pnut.gamma.service.UserSuggestionWorker>()
            .setConstraints(constraints)
            .build()
        androidx.work.WorkManager.getInstance(this).enqueue(oneTimeRequest)
    }
  }

  companion object {
    fun getInstance(activity: Activity) = activity.application as GammaApplication
  }
}

@HiltAndroidApp
class GammaHiltApplication : GammaApplication() {
    @Inject
    fun injectDependencies(
        preferenceRepository: IPreferenceRepository,
        pnutRepository: IPnutRepository,
        accountRepository: IAccountRepository,
        workerFactory: HiltWorkerFactory
    ) {
        this.preferenceRepository = preferenceRepository
        this.pnutRepository = pnutRepository
        this.accountRepository = accountRepository
        this.workerFactory = workerFactory
    }

    override fun onCreate() {
        super.onCreate()
        initApplication()
        scheduleUserSuggestionSync()
    }
}
