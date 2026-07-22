package io.pnut.gamma.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bumptech.glide.Glide

class ClearGlideCacheWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Glide.getPhotoCacheDir(applicationContext)?.deleteRecursively()
        val broadcast = Intent(ACTION)
        applicationContext.sendBroadcast(broadcast)
        return Result.success()
    }

    class Receiver(val listener: Listener) : BroadcastReceiver() {
        interface Listener {
            fun onClearGlideCache()
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            listener.onClearGlideCache()
        }
    }

    companion object {
        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<ClearGlideCacheWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }

        private const val ACTION = "io.pnut.gamma.service.ClearGlideCacheService"
        val intentFilter = IntentFilter(ACTION)
    }
}
