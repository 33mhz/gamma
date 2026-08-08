package io.pnut.gamma.presentation.util

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel
import io.pnut.gamma.GammaApplication
import io.pnut.gamma.R
import io.pnut.gamma.domain.model.preference.ShapeOfAvatar

object BindingUtil {
    @BindingAdapter("glideAvatarSrc")
    @JvmStatic
    fun glideAvatarSrc(view: ImageView, url: String?) {
        if (url.isNullOrEmpty()) {
            view.setImageDrawable(null)
            return
        }

        val currentDrawable = view.drawable
        val preferenceRepository = (view.context.applicationContext as GammaApplication).preferenceRepository
        val shape = preferenceRepository.shapeOfAvatar

        if (view is ShapeableImageView) {
            val shapeAppearanceRes = when (shape) {
                ShapeOfAvatar.Circle -> R.style.CircleShape
                ShapeOfAvatar.Rounded -> R.style.RoundedShape
                ShapeOfAvatar.Square -> R.style.SquareShape
            }
            view.shapeAppearanceModel =
                ShapeAppearanceModel.builder(view.context, shapeAppearanceRes, 0).build()
        }

        Glide.with(view)
            .load(url)
            .let {
                when (shape) {
                    ShapeOfAvatar.Circle -> it.apply(RequestOptions.circleCropTransform())
                    ShapeOfAvatar.Rounded -> {
                        val radius = view.context.resources.getDimensionPixelSize(R.dimen.size_avatar_rounded)
                        it.transform(CenterCrop(), RoundedCorners(radius))
                    }
                    ShapeOfAvatar.Square -> it
                }
            }
            .let { request ->
                if (currentDrawable != null) request.placeholder(currentDrawable) else request
            }
            .into(view)
    }

    @BindingAdapter("glideSrc")
    @JvmStatic
    fun glideSrc(view: ImageView, url: String?) {
        if (url.isNullOrEmpty()) {
            view.setImageDrawable(null)
            return
        }
        val currentDrawable = view.drawable
        Glide.with(view)
            .load(url)
            .let { request ->
                if (currentDrawable != null) request.placeholder(currentDrawable) else request
            }
            .into(view)
    }

    @BindingAdapter("textId")
    @JvmStatic
    fun TextView.setTextRes(res: Int?) {
        if(res != null && res > 0) this.setText(res) else this.text = ""
    }

    @BindingAdapter("onNavigationClick")
    @JvmStatic
    fun Toolbar.setOnNavigationClick(listener: View.OnClickListener) {
        setNavigationOnClickListener(listener)
    }

    @BindingAdapter("title")
    @JvmStatic
    fun Toolbar.setTitleBinding(newTitle: Function0<String>) {
        title = newTitle()
    }

    @BindingAdapter("subtitle")
    @JvmStatic
    fun Toolbar.setSubTitleBinding(newSubTitle: Function0<String>) {
        subtitle = newSubTitle()
    }

    @BindingAdapter("backgroundTint")
    @JvmStatic
    fun setBackgroundTint(view: MaterialButton, @ColorInt color: Int) {
        view.setBackgroundColor(color)
    }

    @BindingAdapter("loading")
    @JvmStatic
    fun setLoadingIndicator(view: MaterialButton, loading: Boolean) {
        view.icon = if (loading) {
            val progress = CircularProgressDrawable(view.context).apply {
                val gray = view.context.getColor(R.color.colorGrayLighter)
                setTint(gray)
                start()
            }
            progress
        } else {
            null
        }
    }

}

