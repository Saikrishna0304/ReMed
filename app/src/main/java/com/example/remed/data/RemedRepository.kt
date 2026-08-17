package com.example.remed.data

import kotlinx.coroutines.flow.Flow

class ReMedRepository(
    private val medicationDao: MedicationDao,
    private val waterDao: WaterDao
) {
    val allMedications: Flow<List<Medication>> = medicationDao.getAllMedications()

    fun getWaterLog(date: String): Flow<WaterLog?> = waterDao.getLogForDate(date)

    suspend fun insertMedication(medication: Medication) {
        medicationDao.insertMedication(medication)
    }

    suspend fun updateMedication(medication: Medication) {
        medicationDao.updateMedication(medication)
    }

    suspend fun updateWaterIntake(date: String, amount: Int) {
        val log = waterDao.getLog(date)
        if (log != null) {
            val newAmount = log.amount + amount
            waterDao.updateLog(log.copy(amount = if (newAmount < 0) 0 else newAmount))
        } else {
            if (amount > 0) {
                waterDao.insertLog(WaterLog(date = date, amount = amount))
            }
        }
    }
}
