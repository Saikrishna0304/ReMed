package com.example.remed.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.remed.R
import com.example.remed.RemedApplication
import com.example.remed.data.ReMedDatabase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MedicationReminderReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_TAKEN = "com.example.remed.notifications.ACTION_TAKEN"
        const val ACTION_LATER = "com.example.remed.notifications.ACTION_LATER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getIntExtra("med_id", -1)
        val medName = intent.getStringExtra("med_name") ?: "Medication"
        val dosage = intent.getStringExtra("dosage") ?: ""
        val isFollowUp = intent.getBooleanExtra("is_follow_up", false)
        val resendCount = intent.getIntExtra("resend_count", 0)

        if (medId == -1) return

        when (intent.action) {
            ACTION_TAKEN -> {
                handleTakenAction(context, medId, medName)
                return
            }
            ACTION_LATER -> {
                handleLaterAction(context, medId, medName)
                return
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            val db = ReMedDatabase.getDatabase(context)
            val med = db.medicationDao().getMedicationById(medId)

            // Only show notification if medication exists and hasn't been taken yet
            if (med != null && !med.isTaken) {
                showNotification(context, medId, medName, dosage, isFollowUp)
                
                // Limit the automatic resending to 5 times
                if (resendCount < 5) {
                    AlarmScheduler(context).scheduleFollowUp(
                        medId, 
                        medName, 
                        resendCount = resendCount + 1
                    )
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun handleTakenAction(context: Context, medId: Int, medName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(medName.hashCode())

        GlobalScope.launch {
            val app = context.applicationContext as RemedApplication
            val repository = app.repository
            val med = app.database.medicationDao().getMedicationById(medId)
            
            if (med != null) {
                repository.updateMedication(med.copy(isTaken = true, lastTakenTimestamp = System.currentTimeMillis()))
                // Cancel any pending follow-ups since it's taken
                AlarmScheduler(context).cancelFollowUp(medId)
            }
        }
    }

    private fun handleLaterAction(context: Context, medId: Int, medName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(medName.hashCode())
        
        // Cancel the existing 5-minute "no-reply" follow-up
        AlarmScheduler(context).cancelFollowUp(medId)
        
        // Schedule a new one for 10 minutes from now (as requested for "take later")
        // We reset the resendCount to 0 for a user-initiated snooze
        AlarmScheduler(context).scheduleFollowUp(
            medId, 
            medName, 
            delayMillis = 10 * 60 * 1000, 
            resendCount = 0
        )
    }

    private fun showNotification(context: Context, medId: Int, medName: String, dosage: String, isFollowUp: Boolean) {
        val title = if (isFollowUp) "Still waiting..." else "Medication Reminder"
        val text = if (isFollowUp) 
            "Did you take your $medName? Please update now." 
            else "It's time to take your $medName ($dosage)"

        // Taken Action
        val takenIntent = Intent(context, MedicationReminderReceiver::class.java).apply {
            action = ACTION_TAKEN
            putExtra("med_id", medId)
            putExtra("med_name", medName)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            medId + 200000,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Later Action
        val laterIntent = Intent(context, MedicationReminderReceiver::class.java).apply {
            action = ACTION_LATER
            putExtra("med_id", medId)
            putExtra("med_name", medName)
        }
        val laterPendingIntent = PendingIntent.getBroadcast(
            context,
            medId + 300000,
            laterIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.MED_CHANNEL_ID)
            .setSmallIcon(R.drawable.remed_logo)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(null, true)
            .setAutoCancel(true)
            .addAction(R.drawable.remed_logo, "Taken", takenPendingIntent)
            .addAction(R.drawable.remed_logo, "Will take later", laterPendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(medName.hashCode(), notification)
    }
}
