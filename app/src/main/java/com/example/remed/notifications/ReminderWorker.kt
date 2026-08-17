package com.example.remed.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.showWaterReminderNotification()
        return Result.success()
    }
}
