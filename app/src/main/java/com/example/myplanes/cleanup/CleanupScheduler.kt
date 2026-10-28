package com.example.myplanes.cleanup

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object CleanupScheduler {

    private const val WORK_NAME = "cleanup_old_tasks"

    // Сколько хранятся выполненные и удалённые задачи.
    // Для проверки можно временно поставить TimeUnit.MINUTES.toMillis(1)
    private val RETENTION_MILLIS = TimeUnit.DAYS.toMillis(7)

    // Задачи, у которых дата выполнения/удаления раньше этого момента, подлежат удалению
    fun cutoffMillis(): Long = System.currentTimeMillis() - RETENTION_MILLIS

    // Безопасно вызывать при каждом запуске: KEEP не создаёт дубликаты
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<CleanupWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}