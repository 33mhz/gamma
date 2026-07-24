package io.pnut.gamma.presentation.activity

import android.content.Intent
import android.net.Uri
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.truth.content.IntentSubject.assertThat
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.R as Rm
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.pnut.gamma.GammaApplication
import io.pnut.gamma.R
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.repository.IPnutRepository
import io.pnut.gamma.domain.repository.IPreferenceRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

  @get:Rule(order = 0)
  val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1)
  val intentsRule = IntentsRule()

  @Inject
  lateinit var preferenceRepository: IPreferenceRepository
  @Inject
  lateinit var pnutRepository: IPnutRepository
  @Inject
  lateinit var accountRepository: IAccountRepository
  @Inject
  lateinit var workerFactory: HiltWorkerFactory

  private val intent = Intent(ApplicationProvider.getApplicationContext(), LoginActivity::class.java).also {
    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  }

  @Before
  fun init() {
    hiltRule.inject()
    val app = ApplicationProvider.getApplicationContext<GammaApplication>()
    app.preferenceRepository = preferenceRepository
    app.pnutRepository = pnutRepository
    app.accountRepository = accountRepository
    app.workerFactory = workerFactory
    app.initApplication()
  }

  @Test
  fun openBrowserWhenClickLoginButton() {
    ActivityScenario.launch<LoginActivity>(intent).use {
      Espresso.onView(ViewMatchers.withId(R.id.loginButton)).perform(ViewActions.click())
      val intent = Intents.getIntents().first { it.action == Intent.ACTION_VIEW }
      val scopes = arrayOf(
        "basic",
        "stream",
        "write_post",
        "follow",
        "update_profile",
        "presence",
        "messages:io.pnut.core.chat",
        "messages:io.pnut.core.pm",
        "files:io.pnut.delta"
      ).joinToString(",")

      assertThat(intent).hasAction(Intent.ACTION_VIEW)
      assertThat(intent).hasData(Uri.parse("https://pnut.io/oauth/authorize?client_id=I-FlBf-QWcE4HNhiIzz0AoaELF_mBkQf&redirect_uri=gamma://authenticate&scope=${scopes}&response_type=token&simple_login=1"))
    }
  }

  @Test
  fun openBrowserWhenClickSignUpButton() {
    ActivityScenario.launch<LoginActivity>(intent).use {
      Espresso.onView(ViewMatchers.withId(R.id.signUpButton)).perform(ViewActions.click())
      val intent = Intents.getIntents().first { it.action == Intent.ACTION_VIEW }
      assertThat(intent).hasAction(Intent.ACTION_VIEW)
      assertThat(intent).hasData(Uri.parse("https://pnut.io/join"))
    }
  }

  @Test
  fun showErrorMessageWhenFailedToAuthenticate() {
    val intent = LoginActivity.getRetryIntent(
      InstrumentationRegistry.getInstrumentation().targetContext,
      "error message"
    )
    ActivityScenario.launch<LoginActivity>(intent)
    Espresso.onView(ViewMatchers.withId(Rm.id.snackbar_text))
      .check(ViewAssertions.matches(ViewMatchers.withText("error message")))
  }
}
