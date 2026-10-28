package com.example.myplanes.reminders

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myplanes.MainActivity
import com.example.myplanes.R
import com.example.myplanes.data.AppDatabase
import com.example.myplanes.data.Task
import com.example.myplanes.data.isCompleted
import com.example.myplanes.data.isDeleted

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(KEY_TASK_ID, -1L)
        if (taskId == -1L) return Result.failure()

        val task = AppDatabase.getInstance(applicationContext).taskDao().getById(taskId)
            ?: return Result.success() // задачу уже убрала автоочистка

        // Выполненным и удалённым задачам напоминание не нужно
        if (task.isCompleted || task.isDeleted) return Result.success()

        showNotification(task)
        return Result.success()
    }

    // Разрешение проверяем через areNotificationsEnabled()
    @SuppressLint("MissingPermission")
    private fun showNotification(task: Task) {
        val manager = NotificationManagerCompat.from(applicationContext)
        if (!manager.areNotificationsEnabled()) return

        ensureChannel()

        val openApp = PendingIntent.getActivity(
            applicationContext,
            task.id.toInt(),
            Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val text = task.description.ifBlank { task.quadrant.title }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(task.title)
            .setContentText(text)
            .setSubText(task.quadrant.title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        manager.notify(task.id.toInt(), notification)
    }

    private fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Напоминания о задачах",
            NotificationManager.IMPORTANCE_HIGH
        )
        applicationContext.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
        private const val CHANNEL_ID = "task_reminders"
    }
}