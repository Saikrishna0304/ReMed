package com.example.remed.data

import androidx.room.Entity

@Entity(tableName = "water_logs", primaryKeys = ["userId", "date"])
data class WaterLog(
    val userId: String = "",
    val date: String = "",
    val amount: Int = 0
)
