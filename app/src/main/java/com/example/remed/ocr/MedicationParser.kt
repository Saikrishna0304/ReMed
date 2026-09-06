package com.example.remed.ocr

import com.example.remed.data.Medication

object MedicationParser {
    /**
     * Parses raw text from OCR/Handwriting into a Medication object.
     * Tries to identify Name, Dosage, and Frequency.
     */
    fun parseText(text: String): Medication? {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return null

        var name: String? = null
        var dosage: String? = null
        var frequency: String? = null

        // 1. Look for explicit labels
        lines.forEach { line ->
            val lower = line.lowercase()
            when {
                name == null && (lower.startsWith("medication:") || lower.startsWith("name:") || lower.startsWith("rx:")) ->
                    name = line.substringAfter(":").trim()
                dosage == null && lower.startsWith("dosage:") ->
                    dosage = line.substringAfter(":").trim()
                frequency == null && lower.startsWith("frequency:") ->
                    frequency = line.substringAfter(":").trim()
            }
        }

        // 2. Fallback heuristic parsing
        val finalName = name ?: lines[0].removeSuffix(":").trim()

        if (dosage == null) {
            // Find a line that looks like a dosage (contains numbers + units)
            val dosageRegex = Regex(".*\\d+\\s*(mg|ml|tablet|pill|g|mcg|unit).*", RegexOption.IGNORE_CASE)
            dosage = lines.find { it.matches(dosageRegex) } ?: if (lines.size > 1) lines[1] else null
        }

        val lowerText = text.lowercase()
        if (frequency == null) {
            frequency = when {
                lowerText.contains("twice") || lowerText.contains("2 times") || lowerText.contains("2x") || lowerText.contains("bid") || lowerText.contains("bd") ->
                    "Twice Daily (Morning & Evening)"
                lowerText.contains("thrice") || lowerText.contains("three times") || lowerText.contains("3 times") || lowerText.contains("3x") || lowerText.contains("tid") ->
                    "Three Times Daily"
                lowerText.contains("once") || lowerText.contains("1 time") || lowerText.contains("daily") || lowerText.contains("qd") ->
                    "Once Daily"
                else -> {
                    val freqRegex = Regex(".*(daily|times|day|morning|night|evening|hour|every).*", RegexOption.IGNORE_CASE)
                    lines.find { it.matches(freqRegex) } ?: "Daily"
                }
            }
        }

        return Medication(
            name = finalName,
            dosage = dosage ?: "As prescribed",
            frequency = frequency,
            scheduledTime = System.currentTimeMillis()
        )
    }
}
