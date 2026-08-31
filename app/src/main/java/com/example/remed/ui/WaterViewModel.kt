package com.example.remed.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.remed.data.AuthRepository
import com.example.remed.data.ReMedRepository
import com.example.remed.data.WaterLog
import com.example.remed.data.WaterSettings
import com.example.remed.notifications.AlarmScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class WaterViewModel(
    application: Application, 
    private val repository: ReMedRepository,
    private val userIdFlow: StateFlow<String?>
) : AndroidViewModel(application) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val today = dateFormat.format(Date())
    private val alarmScheduler = AlarmScheduler(application)

    val waterLog: StateFlow<WaterLog?> = userIdFlow
        .flatMapLatest { uid ->
            if (uid != null) repository.getWaterLog(uid, today)
            else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val waterSettings: StateFlow<WaterSettings> = userIdFlow
        .flatMapLatest { uid ->
            if (uid != null) repository.getWaterSettings(uid).map { it ?: WaterSettings(userId = uid) }
            else flowOf(WaterSettings(userId = ""))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WaterSettings(userId = ""))

    fun addWater(amount: Int) = viewModelScope.launch {
        val uid = userIdFlow.value
        if (uid != null) {
            repository.updateWaterIntake(uid, today, amount)
        }
    }

    fun removeWater(amount: Int) = viewModelScope.launch {
        val uid = userIdFlow.value
        if (uid != null) {
            repository.updateWaterIntake(uid, today, -amount)
        }
    }

    fun updateSettings(dailyGoal: Int, quickAddAmount: Int, reminderInterval: Long) = viewModelScope.launch {
        val uid = userIdFlow.value
        if (uid != null) {
            val newSettings = WaterSettings(
                userId = uid,
                dailyGoal = dailyGoal, 
                quickAddAmount = quickAddAmount, 
                reminderInterval = reminderInterval
            )
            repository.updateWaterSettings(newSettings)
            
            // Update reminders if interval changed
            if (reminderInterval > 0) {
                alarmScheduler.scheduleRepeating(reminderInterval)
            } else {
                alarmScheduler.cancel()
            }
        }
    }

    fun setWaterReminderInterval(intervalInMinutes: Long) {
        val currentSettings = waterSettings.value
        updateSettings(currentSettings.dailyGoal, currentSettings.quickAddAmount, intervalInMinutes)
    }

    fun cancelWaterReminders() {
        val currentSettings = waterSettings.value
        updateSettings(currentSettings.dailyGoal, currentSettings.quickAddAmount, 0)
    }
}
