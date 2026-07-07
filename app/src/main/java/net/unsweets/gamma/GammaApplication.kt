package net.unsweets.gamma

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.runBlocking
import net.unsweets.gamma.domain.repository.IAccountRepository
import net.unsweets.gamma.domain.repository.IPnutRepository
import net.unsweets.gamma.domain.repository.IPreferenceRepository
import net.unsweets.gamma.domain.usecases.SetupTokenUseCase
import net.unsweets.gamma.presentation.activity.LoginActivity
import net.unsweets.gamma.presentation.util.ThemeColorUtil
import javax.inject.Inject


@HiltAndroidApp
open class GammaApplication : Application(), CoroutineScope by MainScope() {

  @Inject
  lateinit var preferenceRepository: IPreferenceRepository
  @Inject
  lateinit var pnutRepository: IPnutRepository
  @Inject
  lateinit var accountRepository: IAccountRepository

  override fun onCreate() {
    super.onCreate()
    updateBaseTheme()
    updateTheme()
    val config = BundledEmojiCompatConfig(this)
      .setReplaceAll(true)
    EmojiCompat.init(config)
      FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true

//        if (!setToken()) return backToLoginActivity() // failed

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
