package io.pnut.gamma

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.repository.IPnutRepository
import io.pnut.gamma.domain.repository.IPreferenceRepository
import io.pnut.gamma.domain.usecases.SetupTokenUseCase
import io.pnut.gamma.presentation.activity.LoginActivity
import io.pnut.gamma.presentation.util.ThemeColorUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


open class GammaApplication : Application(), CoroutineScope by MainScope(), Configuration.Provider {

  lateinit var preferenceRepository: IPreferenceRepository
  lateinit var pnutRepository: IPnutRepository
  lateinit var accountRepository: IAccountRepository
  lateinit var workerFactory: HiltWorkerFactory

  override val workManagerConfiguration: Configuration
    get() = Configuration.Builder()
      .setWorkerFactory(workerFactory)
      .build()

  override fun onCreate() {
    super.onCreate()
  }

  fun initApplication() {
    updateBaseTheme()
    updateTheme()
    runCatching {
      val config = BundledEmojiCompatConfig(this)
        .setReplaceAll(true)
      EmojiCompat.init(config)
    }
    runCatching {
      FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
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
    return runBlocking {
      SetupTokenUseCase(
        pnutRepository,
        accountRepository
      ).run(Unit)
    }.existDefaultAccount
  }

  fun updateTheme() {
    ThemeColorUtil.applyTheme(this)
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
    }
}
