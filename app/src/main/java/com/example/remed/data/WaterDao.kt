package com.example.remed.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_logs WHERE date = :date")
    fun getLogForDate(date: String): Flow<WaterLog?>

    @Query("SELECT * FROM water_logs WHERE date = :date")
    suspend fun getLog(date: String): WaterLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WaterLog)

    @Update
    suspend fun updateLog(log: WaterLog)
}
