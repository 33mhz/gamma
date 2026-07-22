package io.pnut.gamma.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.pnut.gamma.domain.repository.PnutCacheRepository

class ClearStreamCacheWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val cacheDir = PnutCacheRepository.getUserCacheDir(applicationContext) ?: return Result.failure()
        cacheDir.deleteRecursively()
        val broadcast = Intent(ACTION)
        applicationContext.sendBroadcast(broadcast)
        return Result.success()
    }

    class Receiver(val listener: Listener) : BroadcastReceiver() {
        interface Listener {
            fun onClearStreamCache()
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            listener.onClearStreamCache()
        }
    }

    companion object {
        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<ClearStreamCacheWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }

        private const val ACTION = "io.pnut.gamma.service.ClearStreamCacheService"
        val intentFilter = IntentFilter(ACTION)
    }
}
