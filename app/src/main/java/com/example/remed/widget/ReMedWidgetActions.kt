package com.example.remed.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.example.remed.RemedApplication
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddWaterAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val app = context.applicationContext as RemedApplication
        val repository = app.repository
        val authRepo = app.authRepository
        val userId = authRepo.currentUser?.uid ?: "GUEST_USER"
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        repository.updateWaterIntake(userId, today, 250)
        ReMedWidget().updateAll(context)
    }
}

class MarkMedicationTakenAction : ActionCallback {
    companion object {
        val medIdKey = ActionParameters.Key<Int>("med_id")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val medId = parameters[medIdKey] ?: return
        val app = context.applicationContext as RemedApplication
        val repository = app.repository
        val authRepo = app.authRepository
        val userId = authRepo.currentUser?.uid ?: "GUEST_USER"

        val medList = app.database.medicationDao().getMedicationsList(userId)
        val targetMed = medList.find { it.id == medId }
        if (targetMed != null) {
            repository.updateMedication(targetMed.copy(isTaken = true, lastTakenTimestamp = System.currentTimeMillis()))
        }
        ReMedWidget().updateAll(context)
    }
}
