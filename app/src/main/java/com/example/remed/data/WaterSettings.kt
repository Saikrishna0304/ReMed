package com.example.remed.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_settings")
data class WaterSettings(
    @PrimaryKey val userId: String = "",
    val dailyGoal: Int = 2000,
    val quickAddAmount: Int = 250,
    val reminderInterval: Long = 60 // minutes
)
