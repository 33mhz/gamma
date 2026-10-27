package io.pnut.gamma.presentation.view

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreferenceCompat

class LinkableSwitchPreferenceCompat @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SwitchPreferenceCompat(context, attrs) {

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val titleView = holder.findViewById(android.R.id.title) as? TextView
        if (titleView != null && title != null) {
            titleView.movementMethod = LinkMovementMethod.getInstance()
            titleView.text = HtmlCompat.fromHtml(title.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
        val summaryView = holder.findViewById(android.R.id.summary) as? TextView
        if (summaryView != null && summary != null) {
            summaryView.movementMethod = LinkMovementMethod.getInstance()
            summaryView.text = HtmlCompat.fromHtml(summary.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }
}
