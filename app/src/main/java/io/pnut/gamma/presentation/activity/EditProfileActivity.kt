package io.pnut.gamma.presentation.activity

import io.pnut.gamma.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.activity.OnBackPressedCallback
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.transition.platform.MaterialArcMotion
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import io.pnut.gamma.domain.entity.User
import io.pnut.gamma.presentation.fragment.EditProfileFragment

class EditProfileActivity : BaseActivity(), EditProfileFragment.Callback {
    override fun onRequestToFinish() {
        supportFinishAfterTransition()
    }

    override fun onSaved(user: User) {
        val data = Intent().apply {
            putExtra("User", user)
        }
        setResult(RESULT_OK, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupAnimation()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        replaceFragment(savedInstanceState == null)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!editProfileFragment.requestToFinish()) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private val userId by lazy {
        intent.getStringExtra(BundleKey.UserId.name).orEmpty()
    }


    private fun setupAnimation() {
        setEnterSharedElementCallback(object : MaterialContainerTransformSharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                super.onMapSharedElements(names, sharedElements)
                println("$names, $sharedElements")
            }
        })
        window.sharedElementsUseOverlay = false
        findViewById<View>(R.id.content).transitionName =
            getString(R.string.shared_element_edit_profile)
        val duration = resources.getInteger(R.integer.default_anim_duration).toLong()
        window.sharedElementEnterTransition = MaterialContainerTransform().also {
            it.fitMode = MaterialContainerTransform.FIT_MODE_AUTO
            it.pathMotion = MaterialArcMotion()
            it.interpolator = FastOutSlowInInterpolator()
            it.isElevationShadowEnabled = false
            it.startShapeAppearanceModel =
                ShapeAppearanceModel.builder().setAllCornerSizes(ShapeAppearanceModel.PILL).build()
            it.endShapeAppearanceModel =
                ShapeAppearanceModel.builder().setAllCornerSizes(0f).build()
            it.addTarget(R.id.content)
            it.duration = duration
        }
        window.sharedElementReturnTransition = MaterialContainerTransform().also {
            it.fitMode = MaterialContainerTransform.FIT_MODE_AUTO
            it.pathMotion = MaterialArcMotion()
            it.interpolator = FastOutSlowInInterpolator()
            it.isElevationShadowEnabled = false
            it.startShapeAppearanceModel =
                ShapeAppearanceModel.builder().setAllCornerSizes(0f).build()
            it.endShapeAppearanceModel =
                ShapeAppearanceModel.builder().setAllCornerSizes(ShapeAppearanceModel.PILL).build()
            it.addTarget(R.id.content)
            it.duration = duration
        }
    }

    private val editProfileFragment by lazy { EditProfileFragment.newInstance(userId) }


    private fun replaceFragment(firstTime: Boolean) {
        if (!firstTime) return
        supportFragmentManager.beginTransaction().replace(R.id.edit_profile_placeholder, editProfileFragment)
            .commit()
    }

    private enum class BundleKey {
        UserId
    }

    companion object {
        fun newIntent(context: Context, userId: String) =
            Intent(context, EditProfileActivity::class.java).also {
                it.putExtra(BundleKey.UserId.name, userId)
            }
    }
}