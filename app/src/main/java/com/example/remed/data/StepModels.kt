package com.example.remed.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_logs", primaryKeys = ["userId", "date"])
data class StepLog(
    val userId: String = "",
    val date: String = "",
    val count: Int = 0
)

@Entity(tableName = "step_settings")
data class StepSettings(
    @PrimaryKey val userId: String = "",
    val dailyGoal: Int = 5000
)
