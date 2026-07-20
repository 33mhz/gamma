package io.pnut.gamma.presentation.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import io.pnut.gamma.R
import io.pnut.gamma.presentation.util.ThemeColorUtil
import androidx.core.graphics.drawable.toDrawable


class ThemeColorPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : Preference(context, attrs) {
    var themeColor: ThemeColorUtil.ThemeColor? = null
        get() = ThemeColorUtil.ThemeColor.fromString(getPersistedString(""))
        set(value) {
            persistString(value?.name.orEmpty())
            field = value
            notifyChanged()
        }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val colorPreviewImageView =
            holder.findViewById(R.id.colorPreviewImageView) as? ImageView ?: return
        val color = themeColor?.getColor(context) ?: context.getColor(R.color.colorPrimary)
        colorPreviewImageView.setImageDrawable(color.toDrawable())
    }
}
