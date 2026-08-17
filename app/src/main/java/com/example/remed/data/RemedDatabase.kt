package com.example.remed.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Medication::class, WaterLog::class], version = 1, exportSchema = false)
abstract class ReMedDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun waterDao(): WaterDao

    companion object {
        @Volatile
        private var INSTANCE: ReMedDatabase? = null

        fun getDatabase(context: Context): ReMedDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReMedDatabase::class.java,
                    "remed_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
