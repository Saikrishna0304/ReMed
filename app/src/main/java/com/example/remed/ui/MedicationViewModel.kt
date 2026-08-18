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

    suspend fun onImageScanned(bitmap: Bitmap): Boolean {
        val text = prescriptionScanner.scanImage(bitmap)
        return if (text != null) {
            val parsed = parsePrescription(text)
            if (parsed != null) {
                _scannedMedication.value = parsed
                true
            } else false
        } else false
    }

    suspend fun onHandwritingScanned(bitmap: Bitmap): Boolean {
        val text = handwritingProcessor.processHandwriting(bitmap)
        val parsed = parsePrescription(text) ?: if (text.isNotBlank() && text != "?") {
            Medication(
                name = text,
                dosage = "1 pill",
                frequency = "daily",
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

    private fun parsePrescription(text: String): Medication? {
        val lines = text.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return null

        var name: String? = null
        var dosage: String? = null
        var frequency: String? = null

        lines.forEach { line ->
            val lowerCaseLine = line.lowercase()
            when {
                name == null && lowerCaseLine.contains("medication") -> name = line.substringAfter(":").trim()
                dosage == null && lowerCaseLine.contains("dosage") -> dosage = line.substringAfter(":").trim()
                frequency == null && lowerCaseLine.contains("frequency") -> frequency = line.substringAfter(":").trim()
            }
        }

        // Fallback: If no keywords found, assume first line is name, second is dosage
        if (name == null && lines.isNotEmpty()) {
            name = lines[0].trim()
            if (dosage == null && lines.size > 1) {
                dosage = lines[1].trim()
            }
            if (frequency == null && lines.size > 2) {
                frequency = lines[2].trim()
            }
        }

        return name?.let {
            Medication(
                name = it,
                dosage = dosage ?: "1 pill",
                frequency = frequency ?: "daily",
                scheduledTime = System.currentTimeMillis()
            )
        }
    }
}
