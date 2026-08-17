package com.example.remed.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dosage: String,
    val frequency: String, // e.g., "3 times daily"
    val scheduledTime: Long, // timestamp for the next dose
    val isTaken: Boolean = false
)
