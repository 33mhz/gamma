package io.pnut.gamma.presentation.activity

import android.app.Activity
import android.content.Intent
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.domain.usecases.SetupTokenUseCase
import io.pnut.gamma.service.ClearCacheWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.runBlocking
import io.pnut.gamma.BuildConfig
import javax.inject.Inject
import kotlin.reflect.KClass


@AndroidEntryPoint
class EntryActivity : BaseActivity(), CoroutineScope by MainScope() {
  @Inject
  lateinit var setupTokenUseCase: SetupTokenUseCase

  override fun onResume() {
    super.onResume()
    proceed()
  }

  private fun proceed() {
    if (!BuildConfig.DEBUG) ClearCacheWorker.enqueue(this)
    findViewById<View>(android.R.id.content).systemUiVisibility =
      View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

    val existDefaultAccount: Boolean = runBlocking {
      val res = setupTokenUseCase.run(Unit)
      res.existDefaultAccount
    }
    val intentClass: KClass<out Activity> =
      if (existDefaultAccount)
        MainActivity::class
      else
        LoginActivity::class

    val intent = Intent(this, intentClass.java)
    startActivity(intent)
    finish()
  }
}
