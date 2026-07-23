package io.pnut.gamma.domain.model

import android.content.Context
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import io.pnut.gamma.R

@Parcelize
data class PollDeadline(
    val day: Int,
    val hour: Int,
    val min: Int
) : Parcelable {
    val isAvailableHour
        get() = MAX_DURATION - day2min(day) > 0
    val isAvailableMin
        get() = MAX_DURATION - (day2min(day) + hour2min(hour)) > 0
    @IgnoredOnParcel
    val duration = day2min(day) + hour2min(hour) + min

    fun toFormatString(context: Context): String {
        val dayStr = day.takeIf { it > 0 }
            ?.let { context.resources.getQuantityString(R.plurals.poll_day_template, it, it) }
        val hourStr = hour.takeIf { it > 0 }
            ?.let { context.resources.getQuantityString(R.plurals.poll_hour_template, it, it) }
        val minStr = min.takeIf { it > 0 }
            ?.let { context.resources.getQuantityString(R.plurals.poll_minute_template, it, it) }
        val res = StringBuilder()
        if (!dayStr.isNullOrBlank()) res.append(dayStr)
        if (!hourStr.isNullOrBlank()) res.append(hourStr)
        if (!minStr.isNullOrBlank()) res.append(minStr)
        return res.toString()
    }

    fun toInt(): Int {
        val duration = day2min(day) + hour2min(hour) + min
        val fallback = 1 // 1min
        return if (duration > 0) duration else fallback
    }

    companion object {
        private fun day2min(day: Int) = day * 24 * 60
        private fun hour2min(hour: Int) = hour * 60
        const val MAX_DURATION = 20160 // min; equals to 14 days
        const val MIN_DURATION = 1

        val defaultValue
            get() = PollDeadline(1, 0, 0)

        fun fromInt(durationMinInt: Int): PollDeadline {
            val day = durationMinInt / (24 * 60)
            val remainMin = durationMinInt - day2min(day)
            val hour = remainMin / 60
            val min = remainMin - hour2min(hour)
            return PollDeadline(day, hour, min)
        }
    }
}