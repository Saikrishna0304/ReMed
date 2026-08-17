package com.example.remed.ocr

import com.example.remed.data.Medication

object MedicationParser {
    fun parseText(text: String): List<Medication> {
        val medications = mutableListOf<Medication>()
        val lines = text.split("\n")
        
        // Simple regex-based parsing attempt
        lines.forEach { line ->
            if (line.contains("mg", ignoreCase = true) || line.contains("tablet", ignoreCase = true)) {
                medications.add(
                    Medication(
                        name = line.trim(),
                        dosage = "As prescribed",
                        frequency = "Manual setup required",
                        scheduledTime = System.currentTimeMillis()
                    )
                )
            }
        }
        return medications
    }
}
