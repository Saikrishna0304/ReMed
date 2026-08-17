package com.example.remed.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class AlarmScheduler(private val context: Context) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleRepeating(intervalInMinutes: Long) {
        val repeatInterval = if (intervalInMinutes < 15) 15 else intervalInMinutes

        val reminderWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            repeatInterval, 
            TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "water_reminder_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderWorkRequest
        )
    }

    fun cancel() {
        workManager.cancelUniqueWork("water_reminder_work")
    }
}
