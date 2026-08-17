package com.example.remed.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remed.data.Medication
import com.example.remed.data.ReMedRepository
import com.example.remed.ocr.HandwritingProcessor
import com.example.remed.ocr.PrescriptionScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicationViewModel(
    private val repository: ReMedRepository,
    private val prescriptionScanner: PrescriptionScanner,
    private val handwritingProcessor: HandwritingProcessor
) : ViewModel() {
    val allMedications: StateFlow<List<Medication>> = repository.allMedications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _scannedMedication = MutableStateFlow<Medication?>(null)
    val scannedMedication: StateFlow<Medication?> = _scannedMedication.asStateFlow()

    fun insert(medication: Medication) = viewModelScope.launch {
        repository.insertMedication(medication)
    }

    fun markAsTaken(medication: Medication) = viewModelScope.launch {
        repository.updateMedication(medication.copy(isTaken = true))
    }

    fun onImageScanned(bitmap: Bitmap) {
        viewModelScope.launch {
            val text = prescriptionScanner.scanImage(bitmap)
            if (text != null) {
                _scannedMedication.value = parsePrescription(text)
            }
        }
    }

    fun onHandwritingScanned(bitmap: Bitmap) {
        viewModelScope.launch {
            val text = handwritingProcessor.processHandwriting(bitmap)
            // For handwriting, we try to parse it, but if it's just a name, we use it directly
            _scannedMedication.value = parsePrescription(text) ?: if (text.isNotBlank() && text != "?") {
                Medication(
                    name = text,
                    dosage = "1 pill",
                    frequency = "daily",
                    scheduledTime = System.currentTimeMillis()
                )
            } else {
                null
            }
        }
    }

    fun clearScannedMedication() {
        _scannedMedication.value = null
    }

    private fun parsePrescription(text: String): Medication? {
        var name: String? = null
        var dosage: String? = null
        var frequency: String? = null

        text.lines().forEach { line ->
            val lowerCaseLine = line.lowercase()
            if (name == null && lowerCaseLine.contains("medication")) {
                name = line.substringAfter(":").trim()
            }
            if (dosage == null && lowerCaseLine.contains("dosage")) {
                dosage = line.substringAfter(":").trim()
            }
            if (frequency == null && lowerCaseLine.contains("frequency")) {
                frequency = line.substringAfter(":").trim()
            }
        }

        return if (name != null) {
            Medication(
                name = name!!,
                dosage = dosage ?: "1 pill",
                frequency = frequency ?: "daily",
                scheduledTime = System.currentTimeMillis()
            )
        } else {
            null
        }
    }
}
