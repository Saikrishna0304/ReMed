package com.example.remed.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.remed.data.ReMedRepository
import com.example.remed.data.WaterLog
import com.example.remed.notifications.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WaterViewModel(application: Application, private val repository: ReMedRepository) : AndroidViewModel(application) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val today = dateFormat.format(Date())
    private val alarmScheduler = AlarmScheduler(application)

    val waterLog: StateFlow<WaterLog?> = repository.getWaterLog(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun addWater(amount: Int) = viewModelScope.launch {
        repository.updateWaterIntake(today, amount)
    }

    fun removeWater(amount: Int) = viewModelScope.launch {
        repository.updateWaterIntake(today, -amount)
    }

    fun setWaterReminderInterval(intervalInMinutes: Long) {
        alarmScheduler.scheduleRepeating(intervalInMinutes)
    }

    fun cancelWaterReminders() {
        alarmScheduler.cancel()
    }
}
