package com.example.remed.data

import kotlinx.coroutines.flow.Flow

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
        if (log != null) {
            stepDao.updateLog(log.copy(count = log.count + steps))
        } else {
            stepDao.insertLog(StepLog(userId, date, steps))
        }
    }

    fun getStepSettings(userId: String): Flow<StepSettings?> = stepDao.getSettings(userId)

    suspend fun updateStepSettings(settings: StepSettings) {
        stepDao.insertSettings(settings)
    }
}
