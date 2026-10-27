package io.pnut.gamma.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.pnut.gamma.R
import io.pnut.gamma.domain.repository.IPreferenceRepository
import io.pnut.gamma.util.LogUtil
import io.pnut.gamma.util.MndpScheduler

@HiltWorker
class MndpNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val preferenceRepository: IPreferenceRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        LogUtil.d("MndpNotificationWorker doWork triggered")

        if (!preferenceRepository.mndpNotification) {
            LogUtil.d("MNDP notification preference is disabled, skipping")
            return Result.success()
        }

        try {
            showNotification()
        } catch (e: Exception) {
            LogUtil.e("Error showing MNDP notification: ${e.message}")
        }

        if (preferenceRepository.mndpNotification) {
            MndpScheduler.scheduleNextNotification(applicationContext)
        }

        return Result.success()
    }

    private fun showNotification() {
        val context = applicationContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                LogUtil.d("POST_NOTIFICATIONS permission not granted, skipping notification")
                return
            }
        }

        val notificationManager =
            ContextCompat.getSystemService(context, NotificationManager::class.java) ?: return

        val channelId = CHANNEL_ID
        val channelName = context.getString(R.string.mndp_notification_channel_name)
        val channelDescription = context.getString(R.string.mndp_notification_channel_description)
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = channelDescription
        }
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(Intent.ACTION_VIEW, "https://mndp.tv".toUri())
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle(context.getString(R.string.mndp_notification_title))
            .setContentText(context.getString(R.string.mndp_notification_text))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "mndp_notification_channel"
        const val NOTIFICATION_ID = 815
    }
}
