package com.example.remed.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.remed.data.Medication
import java.util.concurrent.TimeUnit

class AlarmScheduler(private val context: Context) {
    private val workManager = WorkManager.getInstance(context)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleMedicationReminder(medication: Medication) {
        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            putExtra("med_id", medication.id)
            putExtra("med_name", medication.name)
            putExtra("dosage", medication.dosage)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medication.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            medication.scheduledTime,
            pendingIntent
        )
    }

    fun scheduleFollowUp(medicationId: Int, medName: String) {
        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            putExtra("med_id", medicationId)
            putExtra("med_name", medName)
            putExtra("is_follow_up", true)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId + 100000, // Unique ID for follow-up
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule for 5 minutes later
        val fiveMinutesLater = System.currentTimeMillis() + (5 * 60 * 1000)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            fiveMinutesLater,
            pendingIntent
        )
    }

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
