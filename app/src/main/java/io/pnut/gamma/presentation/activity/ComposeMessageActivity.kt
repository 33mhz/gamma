package io.pnut.gamma.presentation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.transition.doOnEnd
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.transition.platform.MaterialArcMotion
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import io.pnut.gamma.R
import io.pnut.gamma.domain.entity.Channel
import io.pnut.gamma.domain.entity.Message
import io.pnut.gamma.domain.model.UriInfo
import io.pnut.gamma.presentation.fragment.ComposeMessageFragment
import io.pnut.gamma.presentation.fragment.NewPrivateMessageFragment
import io.pnut.gamma.presentation.util.BackPressedHookable
import io.pnut.gamma.presentation.util.Util
import androidx.core.content.IntentCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComposeMessageActivity : BaseActivity(), ComposeMessageFragment.Callback, NewPrivateMessageFragment.Callback {
    private val isNewPm: Boolean by lazy {
        intent.getBooleanExtra(IntentKey.IsNewPm.name, false)
    }

    private val composeMessageFragment by lazy {
        ComposeMessageFragment.newInstance(
            channelId = intent.getStringExtra(IntentKey.ChannelId.name).orEmpty(),
            initialText = intent.getStringExtra(IntentKey.InitialText.name),
            initialPhoto = IntentCompat.getParcelableArrayListExtra(intent, IntentKey.InitialPhoto.name, UriInfo::class.java),
            replyTarget = IntentCompat.getParcelableExtra(intent, IntentKey.ReplyTarget.name, Message::class.java),
            channelTitle = intent.getStringExtra(IntentKey.ChannelTitle.name),
            channel = IntentCompat.getParcelableExtra(intent, IntentKey.Channel.name, Channel::class.java),
            replyAll = intent.getBooleanExtra(IntentKey.ReplyAll.name, true)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose_post)
        setupAnimation()
        if (savedInstanceState == null) {
            val fragment = if (isNewPm) {
                val usernames = intent.getStringArrayListExtra(IntentKey.Usernames.name)
                NewPrivateMessageFragment.newInstance(usernames)
            } else {
                composeMessageFragment
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.compose_post_placeholder, fragment).commit()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    val fragment = supportFragmentManager.findFragmentById(R.id.compose_post_placeholder)
                    if (fragment is BackPressedHookable) {
                        fragment.onBackPressed()
                    } else {
                        supportFragmentManager.popBackStack()
                    }
                    return
                }

                if (isNewPm) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    return
                }

                if (composeMessageFragment.cancelToCompose()) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupAnimation() {
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementsUseOverlay = false
        findViewById<View>(android.R.id.content).transitionName =
            getString(R.string.shared_element_compose)
        val primaryColor = ContextCompat.getColor(this, R.color.colorPrimary)
        val backgroundColor = ContextCompat.getColor(this, R.color.colorWindowBackground)
        val duration = resources.getInteger(R.integer.default_anim_duration).toLong()
        window.sharedElementEnterTransition = MaterialContainerTransform().also {
            it.fitMode = MaterialContainerTransform.FIT_MODE_AUTO
            it.pathMotion = MaterialArcMotion()
            it.interpolator = FastOutSlowInInterpolator()
            it.containerColor = primaryColor
            it.endContainerColor = backgroundColor
            it.addTarget(android.R.id.content)
            it.duration = duration
            it.doOnEnd {
                val fragment = supportFragmentManager.findFragmentById(R.id.compose_post_placeholder)
                if (fragment is ComposeMessageFragment) {
                    fragment.focusToEditText()
                } else if (fragment is NewPrivateMessageFragment) {
                    fragment.focusToEditText()
                }
            }
        }
        window.sharedElementReturnTransition = MaterialContainerTransform().also {
            it.fitMode = MaterialContainerTransform.FIT_MODE_AUTO
            it.pathMotion = MaterialArcMotion()
            it.interpolator = FastOutSlowInInterpolator()
            it.containerColor = backgroundColor
            it.endContainerColor = primaryColor
            it.addTarget(android.R.id.content)
            it.duration = duration
        }
    }

    private enum class IntentKey {
        ChannelId, InitialText, InitialPhoto, ReplyTarget, IsNewPm, Usernames, ChannelTitle, Channel, ReplyAll
    }

    companion object {
        fun newIntent(
            context: Context,
            channelId: String,
            initialText: String? = null,
            initialPhoto: ArrayList<UriInfo>? = null,
            replyTarget: Message? = null,
            channelTitle: String? = null,
            channel: Channel? = null,
            replyAll: Boolean = true
        ) = Intent(context, ComposeMessageActivity::class.java).also {
            it.putExtra(IntentKey.ChannelId.name, channelId)
            it.putExtra(IntentKey.InitialText.name, initialText)
            it.putExtra(IntentKey.InitialPhoto.name, initialPhoto)
            it.putExtra(IntentKey.ReplyTarget.name, replyTarget)
            it.putExtra(IntentKey.ChannelTitle.name, channelTitle)
            it.putExtra(IntentKey.Channel.name, channel)
            it.putExtra(IntentKey.ReplyAll.name, replyAll)
        }

        fun newIntentForNewPm(context: Context, usernames: ArrayList<String>? = null) = Intent(context, ComposeMessageActivity::class.java).also {
            it.putExtra(IntentKey.IsNewPm.name, true)
            it.putStringArrayListExtra(IntentKey.Usernames.name, usernames)
        }
    }


    override fun onFinish() {
        Util.hideKeyboard(window.decorView) {
            supportFinishAfterTransition()
        }
    }

    override fun addFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_left,
                R.anim.slide_in_right,
                R.anim.slide_out_right
            )
            .replace(R.id.compose_post_placeholder, fragment)
            .addToBackStack(null)
            .commit()
    }
}
