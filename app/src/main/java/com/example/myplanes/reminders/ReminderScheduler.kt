package com.example.myplanes.reminders

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun schedule(context: Context, taskId: Long, reminderAtMillis: Long) {
        val delay = reminderAtMillis - System.currentTimeMillis()
        if (delay <= 0) return

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_TASK_ID to taskId))
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            workName(taskId),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(context: Context, taskId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(taskId))
    }

    private fun workName(taskId: Long) = "reminder_$taskId"
}