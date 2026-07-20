package io.pnut.gamma.presentation.util

import android.content.Context
import androidx.preference.Preference
import io.pnut.gamma.presentation.view.ThemeColorPreference
import io.pnut.gamma.R

class ColorSummaryProvider(context: Context) :
    Preference.SummaryProvider<ThemeColorPreference> {
    private val defaultMessage = context.getString(R.string.default_text)
    override fun provideSummary(preference: ThemeColorPreference): CharSequence? {
        val themeColor = preference.themeColor
        return themeColor?.name ?: defaultMessage
    }

}
