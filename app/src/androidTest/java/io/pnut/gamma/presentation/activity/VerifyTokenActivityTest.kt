package io.pnut.gamma.presentation.activity

import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import kotlinx.coroutines.runBlocking
import io.pnut.gamma.domain.model.io.GetAccountListOutputData
import io.pnut.gamma.domain.model.io.VerifyTokenInputData
import io.pnut.gamma.domain.model.io.VerifyTokenOutputData
import io.pnut.gamma.domain.usecases.GetAccountListUseCase
import io.pnut.gamma.domain.usecases.VerifyTokenUseCase
import io.pnut.gamma.testutil.IntentUtil
import io.pnut.gamma.testutil.OverrideModules
import io.pnut.gamma.sample.Tokens
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class VerifyTokenActivityTest {
  @get:Rule
  val activityTestRule = ActivityTestRule(VerifyTokenActivity::class.java, true, false)

  private val successIntent = Intent().also {
    it.data = Uri.parse("gamma://authenticate#access_token=token")
  }

  @Before
  fun setup() {
      OverrideModules { it ->
          it.fakeUseCaseModule.verifyTokenUseCase =
              Mockito.mock(VerifyTokenUseCase::class.java).also {
                  runBlocking {
                      Mockito.`when`(it.run(VerifyTokenInputData("token")))
                          .thenReturn(VerifyTokenOutputData(Tokens.token))
                  }
              }
          it.fakeUseCaseModule.getAccountListUseCase =
              Mockito.mock(GetAccountListUseCase::class.java).also {
                  runBlocking {
                      Mockito.`when`(it.run(Unit))
                          .thenReturn(GetAccountListOutputData(emptyList()))
                  }
              }
      }
  }

//  TODO
//  @Test
//  fun verifyView() {
//    activityTestRule.launchActivity(successIntent)
//    IntentUtil.assertIntent(MainActivity::class) {
//      Espresso.onView(ViewMatchers.withId(R.id.loadingTextView))
//        .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
//      Espresso.onView(ViewMatchers.withId(R.id.progressBar))
//        .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
//    }
//  }

  @Test
  fun succeedToVerifyToken() {
    IntentUtil.assertIntent(MainActivity::class) {
      activityTestRule.launchActivity(successIntent)
    }
    Assert.assertThat(
      activityTestRule.activity.isFinishing,
      Matchers.`is`(true)
    )
  }

  @Test
  fun failedToVerifyToken() {
    val failedIntent = Intent().also {
      it.data = Uri.parse("gamma://authenticate#error=access_denied")
    }
    activityTestRule.launchActivity(failedIntent)
    Assert.assertThat(
      activityTestRule.activity.isFinishing,
      Matchers.`is`(true)
    )
  }
}