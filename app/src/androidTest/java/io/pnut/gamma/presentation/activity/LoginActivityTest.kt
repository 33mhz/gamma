package io.pnut.gamma.presentation.activity

import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.truth.content.IntentSubject.assertThat
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.R as Rm
import com.google.common.collect.Iterables
import io.pnut.gamma.presentation.activity.LoginActivity
import io.pnut.gamma.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class LoginActivityTest {
  @get:Rule
  val intentsTestRule = IntentsTestRule(LoginActivity::class.java, true, false)
  private val intent = Intent(Intent.ACTION_MAIN).also {
    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  }

  @Test
  fun openBrowserWhenClickLoginButton() {
    intentsTestRule.launchActivity(intent)
    Espresso.onView(ViewMatchers.withId(R.id.loginButton)).perform(ViewActions.click())
    val intent = Iterables.getOnlyElement(Intents.getIntents())
    val scopes = arrayOf(
      "BASIC",
      "STREAM",
      "WRITE_POST",
      "FOLLOW",
      "UPDATE_PROFILE",
      "PRESENCE",
      "messages:io.pnut.core.chat",
      "messages:io.pnut.core.pm",
      "files:io.pnut.delta"
    ).joinToString(",")

    assertThat(intent).hasAction(Intent.ACTION_VIEW)
    assertThat(intent).hasData(Uri.parse("https://pnut.io/oauth/authorize?client_id=WYEwPba2HqCw0g3ygspd2CSNfxAGlMyS&redirect_uri=gamma://authenticate&scope=${scopes}&response_type=token&simple_login=1"))
  }

  @Test
  fun openBrowserWhenClickSignUpButton() {
    intentsTestRule.launchActivity(intent)
    Espresso.onView(ViewMatchers.withId(R.id.signUpButton)).perform(ViewActions.click())
    val intent = Iterables.getOnlyElement(Intents.getIntents())
    assertThat(intent).hasAction(Intent.ACTION_VIEW)
    assertThat(intent).hasData(Uri.parse("https://pnut.io/join"))
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