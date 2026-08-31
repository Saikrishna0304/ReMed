package com.example.remed.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Medication::class, WaterLog::class, WaterSettings::class, StepLog::class, StepSettings::class], version = 4, exportSchema = false)
abstract class ReMedDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun waterDao(): WaterDao
    abstract fun stepDao(): StepDao

    companion object {
        @Volatile
        private var INSTANCE: ReMedDatabase? = null

        fun getDatabase(context: Context): ReMedDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReMedDatabase::class.java,
                    "remed_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
