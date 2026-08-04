package io.pnut.gamma.presentation.activity

import android.content.Intent
import android.net.Uri
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.core.app.ActivityScenario
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.pnut.gamma.GammaApplication
import io.pnut.gamma.domain.model.io.GetAccountListOutputData
import io.pnut.gamma.domain.model.io.VerifyTokenInputData
import io.pnut.gamma.domain.model.io.VerifyTokenOutputData
import io.pnut.gamma.domain.repository.IAccountRepository
import io.pnut.gamma.domain.repository.IPnutRepository
import io.pnut.gamma.domain.repository.IPreferenceRepository
import io.pnut.gamma.domain.usecases.GetAccountListUseCase
import io.pnut.gamma.domain.usecases.VerifyTokenUseCase
import io.pnut.gamma.testutil.IntentUtil
import io.pnut.gamma.sample.Tokens
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import javax.inject.Inject
import com.google.common.truth.Truth.assertThat

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class VerifyTokenActivityTest {

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
  lateinit var verifyTokenUseCase: VerifyTokenUseCase
  @Inject
  lateinit var getAccountListUseCase: GetAccountListUseCase

  private val successIntent = Intent().also {
    it.data = Uri.parse("gamma://authenticate#access_token=token")
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

      runBlocking {
          Mockito.`when`(verifyTokenUseCase.run(VerifyTokenInputData("token")))
              .thenReturn(VerifyTokenOutputData(Tokens.token))
          Mockito.`when`(getAccountListUseCase.run(Unit))
              .thenReturn(GetAccountListOutputData(emptyList()))
      }
  }

  @Test
  fun succeedToVerifyToken() {
    IntentUtil.assertIntent(MainActivity::class) {
      ActivityScenario.launch<VerifyTokenActivity>(successIntent).use { scenario ->
        waitForDestroyed(scenario)
      }
    }
  }

  @Test
  fun failedToVerifyToken() {
    val failedIntent = Intent().also {
      it.data = Uri.parse("gamma://authenticate#error=access_denied")
    }
    ActivityScenario.launch<VerifyTokenActivity>(failedIntent).use { scenario ->
      waitForDestroyed(scenario)
    }
  }

  private fun waitForDestroyed(scenario: ActivityScenario<VerifyTokenActivity>) {
    var count = 0
    while (scenario.state != Lifecycle.State.DESTROYED && count < 20) {
      Thread.sleep(100)
      count++
    }
    assertThat(scenario.state).isEqualTo(Lifecycle.State.DESTROYED)
  }
}
