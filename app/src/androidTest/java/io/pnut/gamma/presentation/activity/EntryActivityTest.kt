package io.pnut.gamma.presentation.activity

import androidx.lifecycle.Lifecycle
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.pnut.gamma.GammaApplication
import io.pnut.gamma.domain.model.io.SetupTokenOutputData
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.repository.IPnutRepository
import io.pnut.gamma.domain.repository.IPreferenceRepository
import io.pnut.gamma.domain.usecases.SetupTokenUseCase
import io.pnut.gamma.testutil.IntentUtil
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import javax.inject.Inject
import androidx.hilt.work.HiltWorkerFactory
import com.google.common.truth.Truth.assertThat

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class EntryActivityTest {

  @get:Rule(order = 0)
  val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1)
  val intentsRule = IntentsRule()

  @Inject
  lateinit var setupTokenUseCase: SetupTokenUseCase

  @Inject
  lateinit var preferenceRepository: IPreferenceRepository
  @Inject
  lateinit var pnutRepository: IPnutRepository
  @Inject
  lateinit var accountRepository: IAccountRepository
  @Inject
  lateinit var workerFactory: HiltWorkerFactory

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

  private fun stubSetupTokenUseCase(result: Boolean) {
      Mockito.`when`(setupTokenUseCase.run(Unit)).thenReturn(SetupTokenOutputData(result))
  }

  @Test
  fun launchMainActivityWhenSomeAccountsExists() {
    stubSetupTokenUseCase(true)
    IntentUtil.assertIntent(MainActivity::class) {
      ActivityScenario.launch(EntryActivity::class.java).use { scenario ->
          waitForDestroyed(scenario)
      }
    }
  }

  @Test
  fun launchLoginActivityWhenHasAccountsDoesNotExists() {
    stubSetupTokenUseCase(false)
    IntentUtil.assertIntent(LoginActivity::class) {
      ActivityScenario.launch(EntryActivity::class.java).use { scenario ->
          waitForDestroyed(scenario)
      }
    }
  }

  private fun waitForDestroyed(scenario: ActivityScenario<EntryActivity>) {
      var count = 0
      while (scenario.state != Lifecycle.State.DESTROYED && count < 20) {
          Thread.sleep(100)
          count++
      }
      assertThat(scenario.state).isEqualTo(Lifecycle.State.DESTROYED)
  }
}
