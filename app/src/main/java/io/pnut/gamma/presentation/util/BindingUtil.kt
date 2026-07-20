package io.pnut.gamma.presentation.util

import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import io.pnut.gamma.R

object BindingUtil {
    @BindingAdapter("glideAvatarSrc")
    @JvmStatic
    fun glideAvatarSrc(view: ImageView, url: String?) {
        if (url.isNullOrEmpty()) {
            view.setImageDrawable(null)
            return
        }
        val placeholder = view.drawable
        val request = Glide
            .with(view)
            .load(url)
            .apply(RequestOptions.circleCropTransform())

        if (placeholder != null)
            request.placeholder(placeholder)
        request.into(view)
    }

    @BindingAdapter("glideSrc")
    @JvmStatic
    fun glideSrc(view: ImageView, url: String?) {
        if (url.isNullOrEmpty()) {
            view.setImageDrawable(null)
            return
        }
        val placeholder = view.drawable
        val request = Glide
            .with(view)
            .load(url)

        if (placeholder != null)
            request.placeholder(placeholder)
        request.into(view)
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
                setColorFilter(gray, PorterDuff.Mode.SRC_IN)
                start()
            }
            progress
        } else {
            null
        }
    }

}

