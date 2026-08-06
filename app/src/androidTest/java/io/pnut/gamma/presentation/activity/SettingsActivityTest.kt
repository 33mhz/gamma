package io.pnut.gamma.presentation.activity

import androidx.hilt.work.HiltWorkerFactory
import androidx.preference.R
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.pnut.gamma.GammaApplication
import io.pnut.gamma.domain.model.io.GetCurrentAccountOutputData
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.repository.IPnutRepository
import io.pnut.gamma.domain.repository.IPreferenceRepository
import io.pnut.gamma.domain.usecases.GetCurrentAccountUseCase
import io.pnut.gamma.sample.Accounts
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import javax.inject.Inject


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {

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

  @Inject
  lateinit var getCurrentAccountUseCase: GetCurrentAccountUseCase

  @Before
  fun init() {
      hiltRule.inject()
      val app = ApplicationProvider.getApplicationContext<GammaApplication>()
      app.preferenceRepository = preferenceRepository
      app.pnutRepository = pnutRepository
      app.accountRepository = accountRepository
      app.workerFactory = workerFactory
      app.initApplication()

      Mockito.`when`(getCurrentAccountUseCase.run(Unit))
          .thenReturn(GetCurrentAccountOutputData(Accounts.account))
  }

  private fun verifyText(text: String) {
    Espresso.onView(ViewMatchers.withId(R.id.recycler_view))
      .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText(text))))
  }

  @Test
  fun verifyListItemsOnRoot() {
    ActivityScenario.launch(SettingsActivity::class.java).use {
      verifyText("@screenName")
      verifyText("Account")
      verifyText("General")
      verifyText("Stream")
      verifyText("Behavior")
      verifyText("Display")
      verifyText("License")
      verifyText("Gamma version")
    }
  }
}
