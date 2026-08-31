package com.example.remed.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_logs WHERE userId = :userId AND date = :date")
    fun getLogForDate(userId: String, date: String): Flow<WaterLog?>

    @Query("SELECT * FROM water_logs WHERE userId = :userId AND date = :date")
    suspend fun getLog(userId: String, date: String): WaterLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WaterLog)

    @Update
    suspend fun updateLog(log: WaterLog)

    // Water Settings
    @Query("SELECT * FROM water_settings WHERE userId = :userId")
    fun getSettings(userId: String): Flow<WaterSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: WaterSettings)
}
