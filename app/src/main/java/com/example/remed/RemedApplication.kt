package com.example.remed

import android.util.Log
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import com.example.remed.data.AuthRepository
import com.example.remed.data.ReMedDatabase
import com.example.remed.data.ReMedRepository

class RemedApplication : MultiDexApplication(), Configuration.Provider {
    val database by lazy { ReMedDatabase.getDatabase(this) }
    val repository by lazy { ReMedRepository(database.medicationDao(), database.waterDao(), database.stepDao()) }
    val authRepository by lazy { AuthRepository() }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
}
