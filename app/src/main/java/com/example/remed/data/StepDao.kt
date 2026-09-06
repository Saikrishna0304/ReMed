package com.example.remed.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM step_logs WHERE userId = :userId AND date = :date")
    fun getLogForDate(userId: String, date: String): Flow<StepLog?>

    @Query("SELECT * FROM step_logs WHERE userId = :userId ORDER BY date DESC LIMIT 7")
    fun getRecentLogs(userId: String): Flow<List<StepLog>>

    @Query("SELECT * FROM step_logs WHERE userId = :userId AND date = :date")
    suspend fun getLog(userId: String, date: String): StepLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: StepLog)

    @Update
    suspend fun updateLog(log: StepLog)

    @Query("DELETE FROM step_logs WHERE userId = :userId AND date < :cutoffDate")
    suspend fun deleteLogsOlderThan(userId: String, cutoffDate: String)

    @Query("SELECT * FROM step_settings WHERE userId = :userId")
    fun getSettings(userId: String): Flow<StepSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: StepSettings)
}
