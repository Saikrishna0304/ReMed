package com.example.remed.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.remed.R
import com.example.remed.data.ReMedDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MedicationReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getIntExtra("med_id", -1)
        val medName = intent.getStringExtra("med_name") ?: "Medication"
        val dosage = intent.getStringExtra("dosage") ?: ""
        val isFollowUp = intent.getBooleanExtra("is_follow_up", false)

        if (medId == -1) return

        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        GlobalScope.launch {
            val db = ReMedDatabase.getDatabase(context)
            val med = db.medicationDao().getMedicationById(medId)

            // Only show notification if medication exists and hasn't been taken yet
            if (med != null && !med.isTaken) {
                showNotification(context, medName, dosage, isFollowUp)
                
                // If this was the first reminder, schedule a follow-up for 5 minutes later
                if (!isFollowUp) {
                    AlarmScheduler(context).scheduleFollowUp(medId, medName)
                }
            }
        }
    }

    private fun showNotification(context: Context, medName: String, dosage: String, isFollowUp: Boolean) {
        val title = if (isFollowUp) "Still waiting..." else "Medication Reminder"
        val text = if (isFollowUp) 
            "Did you take your $medName? It's important to stay on schedule." 
            else "It's time to take your $medName ($dosage)"

        val notification = NotificationCompat.Builder(context, NotificationHelper.MED_CHANNEL_ID)
            .setSmallIcon(R.drawable.remed_logo)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(medName.hashCode(), notification)
    }
}
