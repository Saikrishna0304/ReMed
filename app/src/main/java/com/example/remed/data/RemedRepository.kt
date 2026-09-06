package com.example.remed.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReMedRepository(
    private val medicationDao: MedicationDao,
    private val waterDao: WaterDao,
    private val stepDao: StepDao
) {
    fun getAllMedications(userId: String): Flow<List<Medication>> = 
        medicationDao.getAllMedications(userId)

    fun getWaterLog(userId: String, date: String): Flow<WaterLog?> = 
        waterDao.getLogForDate(userId, date)

    suspend fun insertMedication(medication: Medication): Long {
        return medicationDao.insertMedication(medication)
    }

    suspend fun updateMedication(medication: Medication) {
        medicationDao.updateMedication(medication)
    }

    suspend fun updateWaterIntake(userId: String, date: String, amount: Int) {
        val log = waterDao.getLog(userId, date)
        if (log != null) {
            val newAmount = log.amount + amount
            waterDao.updateLog(log.copy(amount = if (newAmount < 0) 0 else newAmount))
        } else {
            if (amount > 0) {
                waterDao.insertLog(WaterLog(userId = userId, date = date, amount = amount))
            }
        }
    }

    fun getWaterSettings(userId: String): Flow<WaterSettings?> = waterDao.getSettings(userId)

    suspend fun updateWaterSettings(settings: WaterSettings) {
        waterDao.insertSettings(settings)
    }

    // Steps
    fun getStepLog(userId: String, date: String): Flow<StepLog?> = stepDao.getLogForDate(userId, date)

    fun getRecentStepLogs(userId: String): Flow<List<StepLog>> = stepDao.getRecentLogs(userId)

    suspend fun updateStepCount(userId: String, date: String, steps: Int) {
        val log = stepDao.getLog(userId, date)
        val updatedCount = (log?.count ?: 0) + steps
        val updatedLog = StepLog(userId = userId, date = date, count = updatedCount)

        // 1. Save to local Room database
        if (log != null) {
            stepDao.updateLog(updatedLog)
        } else {
            stepDao.insertLog(updatedLog)
        }

        // 2. Perform 30-day retention cleanup (Room & Firestore)
        cleanupOldStepLogs(userId)

        // 3. Sync to Firestore if user is logged in
        if (userId != "GUEST_USER" && userId.isNotBlank()) {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("step_logs")
                    .document(date)
                    .set(updatedLog)
                    .await()
            } catch (_: Exception) {
                // Ignore network exceptions during offline sync
            }
        }
    }

    private suspend fun cleanupOldStepLogs(userId: String) {
        try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -30)
            val cutoffDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            // Delete local logs older than 30 days
            stepDao.deleteLogsOlderThan(userId, cutoffDate)

            // Delete remote Firestore logs older than 30 days
            if (userId != "GUEST_USER" && userId.isNotBlank()) {
                val oldDocs = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("step_logs")
                    .whereLessThan("date", cutoffDate)
                    .get()
                    .await()

                for (doc in oldDocs.documents) {
                    doc.reference.delete().await()
                }
            }
        } catch (_: Exception) {
            // Ignore cleanup errors
        }
    }

    fun getStepSettings(userId: String): Flow<StepSettings?> = stepDao.getSettings(userId)

    suspend fun updateStepSettings(settings: StepSettings) {
        stepDao.insertSettings(settings)
    }
}
