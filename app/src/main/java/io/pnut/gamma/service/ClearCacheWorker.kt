package io.pnut.gamma.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters

class ClearCacheWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val cacheDir = applicationContext.externalCacheDir ?: return Result.failure()
        cacheDir.listFiles()?.forEach { it.delete() }
        return Result.success()
    }

    companion object {
        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<ClearCacheWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
