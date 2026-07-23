package io.pnut.gamma.presentation.view

import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar
import androidx.preference.PreferenceViewHolder
import androidx.preference.R
import androidx.preference.SeekBarPreference
import kotlin.math.round


class SeekBarPreferenceMod @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SeekBarPreference(context, attrs), SeekBar.OnSeekBarChangeListener {
    private val step: Int = seekBarIncrement.let { if (it == 0) 20 else it }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val newProgress = round(progress.toFloat() / step.toFloat()).toInt() * step + min
        value = newProgress
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onBindViewHolder(view: PreferenceViewHolder) {
        super.onBindViewHolder(view)
        val seekBar = view.findViewById(R.id.seekbar) as? SeekBar ?: return
        seekBar.setOnSeekBarChangeListener(this)
    }
}
