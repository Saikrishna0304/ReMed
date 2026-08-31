package com.example.remed.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.remed.R

class NotificationHelper(private val context: Context) {
    companion object {
        const val MED_CHANNEL_ID = "med_reminders"
        const val WATER_CHANNEL_ID = "water_reminders"
        const val WATER_REMINDER_NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val medChannel = NotificationChannel(
                MED_CHANNEL_ID,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            val waterChannel = NotificationChannel(
                WATER_CHANNEL_ID,
                "Water Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(medChannel)
            manager.createNotificationChannel(waterChannel)
        }
    }

    fun showWaterReminderNotification() {
        val drank250Intent = Intent(context, WaterReminderReceiver::class.java).apply {
            action = "com.example.remed.ACTION_DRANK_WATER"
            putExtra("notification_id", WATER_REMINDER_NOTIFICATION_ID)
            putExtra("amount", 250)
        }
        val drank250PendingIntent = PendingIntent.getBroadcast(context, 1, drank250Intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val drank500Intent = Intent(context, WaterReminderReceiver::class.java).apply {
            action = "com.example.remed.ACTION_DRANK_WATER"
            putExtra("notification_id", WATER_REMINDER_NOTIFICATION_ID)
            putExtra("amount", 500)
        }
        val drank500PendingIntent = PendingIntent.getBroadcast(context, 3, drank500Intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val dismissIntent = Intent(context, WaterReminderReceiver::class.java).apply {
            action = "com.example.remed.ACTION_DISMISS"
            putExtra("notification_id", WATER_REMINDER_NOTIFICATION_ID)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(context, 2, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


        val notification = NotificationCompat.Builder(context, WATER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("Hydration Time! 💧")
            .setContentText("Stay hydrated! How much water did you just drink?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_water_drop, "+250ml", drank250PendingIntent)
            .addAction(R.drawable.ic_water_drop, "+500ml", drank500PendingIntent)
            .addAction(R.drawable.ic_water_drop, "Later", dismissPendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(WATER_REMINDER_NOTIFICATION_ID, notification)
    }
}
