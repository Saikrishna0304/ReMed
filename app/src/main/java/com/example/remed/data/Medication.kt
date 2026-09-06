package com.example.remed.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "",
    val name: String = "",
    val dosage: String = "", // e.g. "500mg"
    val frequency: String = "", // e.g. "Twice daily"
    val quantity: Int = 1, // Number of tablets per dose
    val startDate: Long = System.currentTimeMillis(),
    val durationMonths: Int = 1,
    val scheduledTime: Long = System.currentTimeMillis(), // timestamp for the next dose
    val isTaken: Boolean = false,
    val lastTakenTimestamp: Long? = null
) {
    val endDate: Long 
        get() = startDate + (durationMonths.toLong() * 30 * 24 * 60 * 60 * 1000)
    
    val isExpired: Boolean
        get() = System.currentTimeMillis() > endDate
}
