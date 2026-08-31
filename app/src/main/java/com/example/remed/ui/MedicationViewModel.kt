package com.example.remed.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.remed.data.AuthRepository
import com.example.remed.data.Medication
import com.example.remed.data.ReMedRepository
import com.example.remed.notifications.AlarmScheduler
import com.example.remed.ocr.HandwritingProcessor
import com.example.remed.ocr.MedicationParser
import com.example.remed.ocr.PrescriptionScanner
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationViewModel(
    application: Application,
    private val repository: ReMedRepository,
    private val userIdFlow: StateFlow<String?>,
    private val prescriptionScanner: PrescriptionScanner,
    private val handwritingProcessor: HandwritingProcessor
) : AndroidViewModel(application) {
    private val alarmScheduler = AlarmScheduler(application)

    val allMedications: StateFlow<List<Medication>> = userIdFlow
        .flatMapLatest { uid ->
            if (uid != null) repository.getAllMedications(uid)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _scannedMedication = MutableStateFlow<Medication?>(null)
    val scannedMedication: StateFlow<Medication?> = _scannedMedication.asStateFlow()

    fun insert(medication: Medication) = viewModelScope.launch {
        val uid = userIdFlow.value
        if (uid != null) {
            val medWithUser = medication.copy(userId = uid)
            val insertedId = repository.insertMedication(medWithUser)
            alarmScheduler.scheduleMedicationReminder(medWithUser.copy(id = insertedId.toInt()))
        }
    }

    fun markAsTaken(medication: Medication) = viewModelScope.launch {
        repository.updateMedication(medication.copy(isTaken = true, lastTakenTimestamp = System.currentTimeMillis()))
    }

    suspend fun onImageScanned(bitmap: Bitmap): Boolean {
        val text = prescriptionScanner.scanImage(bitmap)
        return if (text != null) {
            val parsed = MedicationParser.parseText(text)
            if (parsed != null) {
                _scannedMedication.value = parsed
                true
            } else false
        } else false
    }

    suspend fun onHandwritingScanned(bitmap: Bitmap): Boolean {
        val text = handwritingProcessor.processHandwriting(bitmap)
        val parsed = MedicationParser.parseText(text) ?: if (text.isNotBlank() && text != "?") {
            Medication(
                name = text,
                dosage = "As prescribed",
                frequency = "Daily",
                scheduledTime = System.currentTimeMillis()
            )
        } else null

        return if (parsed != null) {
            _scannedMedication.value = parsed
            true
        } else false
    }

    fun clearScannedMedication() {
        _scannedMedication.value = null
    }
}
