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
        val insertedId = medicationDao.insertMedication(medication)
        val finalMed = medication.copy(id = insertedId.toInt())

        // Backup to Firebase Firestore under "prescriptions" sub-collection
        if (finalMed.userId != "GUEST_USER" && finalMed.userId.isNotBlank()) {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(finalMed.userId)
                    .collection("prescriptions")
                    .document(insertedId.toString())
                    .set(finalMed)
                    .await()
            } catch (_: Exception) {
                // Offline fallback
            }
        }
        return insertedId
    }

    suspend fun updateMedication(medication: Medication) {
        medicationDao.updateMedication(medication)

        // Sync update to Firebase Firestore
        if (medication.userId != "GUEST_USER" && medication.userId.isNotBlank()) {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(medication.userId)
                    .collection("prescriptions")
                    .document(medication.id.toString())
                    .set(medication)
                    .await()
            } catch (_: Exception) {
                // Offline fallback
            }
        }
    }

    suspend fun deleteMedication(medication: Medication) {
        medicationDao.deleteMedication(medication)

        // Sync deletion to Firebase Firestore
        if (medication.userId != "GUEST_USER" && medication.userId.isNotBlank()) {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(medication.userId)
                    .collection("prescriptions")
                    .document(medication.id.toString())
                    .delete()
                    .await()
            } catch (_: Exception) {
                // Offline fallback
            }
        }
    }

    suspend fun syncPrescriptionsFromFirebase(userId: String): List<Medication> {
        if (userId == "GUEST_USER" || userId.isBlank()) return emptyList()
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore
                .collection("users")
                .document(userId)
                .collection("prescriptions")
                .get()
                .await()

            val remoteMeds = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Medication::class.java)
            }

            // 1. Restore remote medications into local Room DB
            remoteMeds.forEach { med ->
                medicationDao.insertMedication(med)
            }

            // 2. Upload any local Room DB medications for this user that are not in Firestore yet
            val localMeds = medicationDao.getMedicationsList(userId)
            val remoteIds = remoteMeds.map { it.id }.toSet()
            localMeds.filter { it.id !in remoteIds }.forEach { localMed ->
                try {
                    firestore.collection("users")
                        .document(userId)
                        .collection("prescriptions")
                        .document(localMed.id.toString())
                        .set(localMed)
                        .await()
                } catch (_: Exception) {}
            }

            medicationDao.getMedicationsList(userId)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun updateWaterIntake(userId: String, date: String, amount: Int) {
        val log = waterDao.getLog(userId, date)
        val newAmount = if (log != null) {
            val a = log.amount + amount
            if (a < 0) 0 else a
        } else {
            if (amount > 0) amount else 0
        }
        val updatedLog = WaterLog(userId = userId, date = date, amount = newAmount)

        if (log != null) {
            waterDao.updateLog(updatedLog)
        } else if (newAmount > 0) {
            waterDao.insertLog(updatedLog)
        }

        if (userId != "GUEST_USER" && userId.isNotBlank()) {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("water_logs")
                    .document(date)
                    .set(updatedLog)
                    .await()
            } catch (_: Exception) {
            }
        }
    }

    fun getWaterSettings(userId: String): Flow<WaterSettings?> = waterDao.getSettings(userId)

    suspend fun updateWaterSettings(settings: WaterSettings) {
        waterDao.insertSettings(settings)

        if (settings.userId != "GUEST_USER" && settings.userId.isNotBlank()) {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(settings.userId)
                    .collection("water_settings")
                    .document("settings")
                    .set(settings)
                    .await()
            } catch (_: Exception) {
            }
        }
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

        if (settings.userId != "GUEST_USER" && settings.userId.isNotBlank()) {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(settings.userId)
                    .collection("step_settings")
                    .document("settings")
                    .set(settings)
                    .await()
            } catch (_: Exception) {
            }
        }
    }
}
