package io.pnut.gamma.presentation.activity

import io.pnut.gamma.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import io.pnut.gamma.databinding.ActivityLoginBinding
import io.pnut.gamma.presentation.util.LoginUtil
import io.pnut.gamma.util.showAsError

import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri

@AndroidEntryPoint
class LoginActivity : BaseActivity() {
    enum class IntentKey {
        Error
    }
    companion object {
        fun getRetryIntent(context: Context, message: String) = Intent(context, LoginActivity::class.java).apply {
            putExtra(IntentKey.Error.name, message)
        }
    }

    private val errorMessage: String? by lazy {
        intent.getStringExtra(IntentKey.Error.name)
    }

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.loginButton.setOnClickListener { launchLoginBrowserActivity() }
        binding.signUpButton.setOnClickListener { launchSignUpBrowserActivity() }
        showSnackBarWhenRaisedError()
    }

    private fun showSnackBarWhenRaisedError() {
        errorMessage?.let {
            Snackbar
                .make(findViewById(android.R.id.content), it, Snackbar.LENGTH_LONG)
                .showAsError()
        }
    }


    private fun launchLoginBrowserActivity() {
        LoginUtil.launchLogin(this)
        finish()
    }

    private fun launchSignUpBrowserActivity() {
        val intent = Intent(Intent.ACTION_VIEW, getString(R.string.sign_up_url).toUri())
        startActivity(intent)
    }

}
