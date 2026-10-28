package com.example.myplanes.cleanup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myplanes.data.AppDatabase

class CleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        AppDatabase.getInstance(applicationContext)
            .taskDao()
            .purgeOlderThan(CleanupScheduler.cutoffMillis())
        return Result.success()
    }
}