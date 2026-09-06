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

    private var initialHardwareStepCount = -1f
    private var lastStepTimeMs = 0L

    // Accelerometer Fallback Peak Detection Variables
    private var filteredGravity = 9.81f
    private var isPeakHigh = false
    private val alpha = 0.8f
    private val minStepIntervalMs = 250L
    private val peakThreshold = 1.8f
    private val resetThreshold = 0.5f

    val stepLog: StateFlow<StepLog?> = userIdFlow
        .flatMapLatest { uid ->
            val activeUid = if (!uid.isNullOrBlank()) uid else "GUEST_USER"
            repository.getStepLog(activeUid, today)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentLogs: StateFlow<List<StepLog>> = userIdFlow
        .flatMapLatest { uid ->
            val activeUid = if (!uid.isNullOrBlank()) uid else "GUEST_USER"
            repository.getRecentStepLogs(activeUid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stepSettings: StateFlow<StepSettings> = userIdFlow
        .flatMapLatest { uid ->
            val activeUid = if (!uid.isNullOrBlank()) uid else "GUEST_USER"
            repository.getStepSettings(activeUid).map { it ?: StepSettings(userId = activeUid) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StepSettings(userId = "GUEST_USER"))

    init {
        registerBestAvailableSensor()
    }

    private fun registerBestAvailableSensor() {
        val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        when {
            // Priority 1: Hardware Step Counter (Most accurate on Android phones)
            stepCounter != null -> {
                sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI)
            }
            // Priority 2: Hardware Step Detector
            stepDetector != null -> {
                sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_UI)
            }
            // Priority 3: Accelerometer Fallback
            accelSensor != null -> {
                sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensorType = event?.sensor?.type ?: return
        val now = System.currentTimeMillis()

        when (sensorType) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalStepsSinceBoot = event.values[0]
                if (initialHardwareStepCount < 0f) {
                    initialHardwareStepCount = totalStepsSinceBoot
                } else {
                    val delta = (totalStepsSinceBoot - initialHardwareStepCount).toInt()
                    if (delta > 0) {
                        initialHardwareStepCount = totalStepsSinceBoot
                        addSteps(delta)
                    }
                }
            }

            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values[0] == 1.0f) {
                    addSteps(1)
                }
            }

            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val totalAccel = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                filteredGravity = alpha * filteredGravity + (1f - alpha) * totalAccel
                val linearAccel = totalAccel - filteredGravity

                if (linearAccel > peakThreshold) {
                    if (!isPeakHigh && (now - lastStepTimeMs) >= minStepIntervalMs) {
                        isPeakHigh = true
                        lastStepTimeMs = now
                        addSteps(1)
                    }
                } else if (linearAccel < resetThreshold) {
                    isPeakHigh = false
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
