package com.example.remed.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.remed.data.ReMedDatabase
import com.example.remed.data.ReMedRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WaterReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notification_id", -1)
        
        // Dismiss the notification immediately when any action is clicked
        if (notificationId != -1) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notificationId)
        }

        val action = intent.action
        if (action == "com.example.remed.ACTION_DRANK_WATER") {
            val db = ReMedDatabase.getDatabase(context)
            val repository = ReMedRepository(db.medicationDao(), db.waterDao())
            
            @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
            GlobalScope.launch {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                repository.updateWaterIntake(today, 100)
            }
        }
    }
}
