package com.example.remed.ui

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.remed.data.AuthRepository
import com.example.remed.data.ReMedRepository
import com.example.remed.data.StepLog
import com.example.remed.data.StepSettings
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
class StepViewModel(
    application: Application,
    private val repository: ReMedRepository,
    private val userIdFlow: StateFlow<String?>
) : AndroidViewModel(application), SensorEventListener {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val today = dateFormat.format(Date())
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    val stepLog: StateFlow<StepLog?> = userIdFlow
        .flatMapLatest { uid ->
            if (uid != null) repository.getStepLog(uid, today)
            else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val stepSettings: StateFlow<StepSettings> = userIdFlow
        .flatMapLatest { uid ->
            if (uid != null) repository.getStepSettings(uid).map { it ?: StepSettings(userId = uid) }
            else flowOf(StepSettings(userId = ""))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StepSettings(userId = ""))

    init {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            // Step counter gives total steps since last boot. 
            // For simplicity, we'll increment by 1 each time it triggers (actual counter is more complex)
            // But let's assume we just add to our local count if we detect movement.
            // Note: TYPE_STEP_DETECTOR might be better for increments.
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun addSteps(steps: Int) = viewModelScope.launch {
        val uid = userIdFlow.value
        if (uid != null) {
            repository.updateStepCount(uid, today, steps)
        }
    }

    fun updateGoal(goal: Int) = viewModelScope.launch {
        val uid = userIdFlow.value
        if (uid != null) {
            repository.updateStepSettings(StepSettings(userId = uid, dailyGoal = goal))
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }
}
