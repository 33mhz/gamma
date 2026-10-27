package io.pnut.gamma.util

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import io.pnut.gamma.service.MndpNotificationWorker
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object MndpScheduler {
    const val WORK_NAME = "MNDPNotificationWork"
    val CENTRAL_ZONE: ZoneId = ZoneId.of("America/Chicago")

    fun calculateNextOccurrence(now: ZonedDateTime): ZonedDateTime {
        val targetTime = LocalTime.of(20, 15) // 8:15 PM
        var next = now.with(targetTime).withNano(0)

        val currentDayOfWeek = now.dayOfWeek
        val daysUntilMonday = (DayOfWeek.MONDAY.value - currentDayOfWeek.value + 7) % 7

        if (daysUntilMonday > 0) {
            next = next.plusDays(daysUntilMonday.toLong())
        } else {
            // Today is Monday
            if (now.toLocalTime().isAfter(targetTime) || now.toLocalTime() == targetTime) {
                next = next.plusDays(7)
            }
        }
        return next
    }

    fun scheduleNextNotification(context: Context) {
        val now = ZonedDateTime.now(CENTRAL_ZONE)
        val nextTime = calculateNextOccurrence(now)
        val delayMillis = maxOf(0L, Duration.between(now, nextTime).toMillis())

        LogUtil.d("Scheduling next MNDP notification for $nextTime (in ${delayMillis / 1000}s)")

        val request = OneTimeWorkRequestBuilder<MndpNotificationWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun ensureScheduled(context: Context) {
        val now = ZonedDateTime.now(CENTRAL_ZONE)
        val nextTime = calculateNextOccurrence(now)
        val delayMillis = maxOf(0L, Duration.between(now, nextTime).toMillis())

        val request = OneTimeWorkRequestBuilder<MndpNotificationWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun cancelNotification(context: Context) {
        LogUtil.d("Cancelling MNDP notification work")
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
