package io.pnut.gamma.presentation.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
//import dagger.android.support.DaggerAppCompatActivity
import io.pnut.gamma.R
import io.pnut.gamma.domain.model.UriInfo
import io.pnut.gamma.presentation.fragment.ComposePostFragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import dagger.hilt.android.AndroidEntryPoint
import io.pnut.gamma.presentation.util.ThemeColorUtil

@AndroidEntryPoint
class ShareActivity : AppCompatActivity() {
    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    private val text by lazy {
        intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
    }
    private val intentExtraDataList by lazy {
        when (intent.action) {
            Intent.ACTION_SEND -> {
                normalizeMediaFileUriList(
                    arrayListOf(
                        IntentCompat.getParcelableExtra(
                            intent,
                            Intent.EXTRA_STREAM,
                            Uri::class.java
                        )
                    )
                )
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                normalizeMediaFileUriList(
                    IntentCompat.getParcelableArrayListExtra(
                        intent,
                        Intent.EXTRA_STREAM,
                        Uri::class.java
                    )
                )
            }
            else -> null
        }
    }

    private fun normalizeMediaFileUriList(uriList: List<Uri?>?): ArrayList<UriInfo>? {
        if (uriList == null) return null
        return ArrayList(uriList.filterNotNull().map { UriInfo(it) })
    }

    private val composePostFragment by lazy {
        ComposePostFragment.newInstance(
            ComposePostFragment.ComposePostFragmentOption(
            initialText = text,
            intentExtraDataList = intentExtraDataList
        ))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeColorUtil.currentDarkThemeMode(this)
        ThemeColorUtil.applyTheme(this)
        setContentView(R.layout.activity_compose_post)
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.compose_post_placeholder, composePostFragment).commit()
        }
    }
}
