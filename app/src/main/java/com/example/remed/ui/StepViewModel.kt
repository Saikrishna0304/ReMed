package com.example.remed.ui

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import kotlin.math.sqrt

@OptIn(ExperimentalCoroutinesApi::class)
class StepViewModel(
    application: Application,
    private val repository: ReMedRepository,
    private val userIdFlow: StateFlow<String?>
) : AndroidViewModel(application), SensorEventListener {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val today = dateFormat.format(Date())
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var initialStepCount = -1f

    // Accelerometer Pedometer Algorithm variables
    private var smoothAccel = 9.81f
    private var isPeak = false
    private var lastStepTimeMs = 0L

    private val alpha = 0.8f // Exponential Moving Average smoothing factor
    private val stepThreshold = 11.5f // Upper acceleration peak threshold (m/s^2)
    private val resetThreshold = 10.2f // Lower threshold to re-arm peak detector
    private val minStepIntervalMs = 280L // Minimum time between steps (~214 steps/min max)

    val stepLog: StateFlow<StepLog?> = userIdFlow
        .flatMapLatest { uid ->
            if (uid != null) repository.getStepLog(uid, today)
            else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentLogs: StateFlow<List<StepLog>> = userIdFlow
        .flatMapLatest { uid ->
            if (uid != null) repository.getRecentStepLogs(uid)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stepSettings: StateFlow<StepSettings> = userIdFlow
        .flatMapLatest { uid ->
            if (uid != null) repository.getStepSettings(uid).map { it ?: StepSettings(userId = uid) }
            else flowOf(StepSettings(userId = ""))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StepSettings(userId = ""))

    init {
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // Register Accelerometer for universal real-time step tracking
        accelSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }

        // Register Hardware Step Detector if available
        stepDetectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Register Hardware Step Counter if available
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensorType = event?.sensor?.type ?: return
        val now = System.currentTimeMillis()

        when (sensorType) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val accel = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                smoothAccel = alpha * smoothAccel + (1f - alpha) * accel

                if (smoothAccel > stepThreshold) {
                    if (!isPeak && (now - lastStepTimeMs) >= minStepIntervalMs) {
                        isPeak = true
                        lastStepTimeMs = now
                        addSteps(1)
                    }
                } else if (smoothAccel < resetThreshold) {
                    isPeak = false
                }
            }

            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values[0] == 1.0f) {
                    if ((now - lastStepTimeMs) >= minStepIntervalMs) {
                        lastStepTimeMs = now
                        addSteps(1)
                    }
                }
            }

            Sensor.TYPE_STEP_COUNTER -> {
                val totalStepsSinceBoot = event.values[0]
                if (initialStepCount < 0f) {
                    initialStepCount = totalStepsSinceBoot
                } else {
                    val delta = (totalStepsSinceBoot - initialStepCount).toInt()
                    if (delta > 0) {
                        initialStepCount = totalStepsSinceBoot
                        if ((now - lastStepTimeMs) >= minStepIntervalMs) {
                            lastStepTimeMs = now
                            addSteps(delta)
                        }
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun addSteps(steps: Int) = viewModelScope.launch {
        val uid = userIdFlow.value
        if (uid != null && steps > 0) {
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
