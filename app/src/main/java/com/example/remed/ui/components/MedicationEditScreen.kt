package com.example.remed.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.remed.data.Medication
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationEditScreen(
    initialMedication: Medication,
    onSave: (List<Medication>) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initialMedication.name) }
    var dosage by remember { mutableStateOf(initialMedication.dosage) }
    var quantity by remember { mutableStateOf(initialMedication.quantity.toString()) }
    var durationMonths by remember { mutableStateOf(initialMedication.durationMonths.toString()) }

    val frequencyOptions = listOf(
        "1 Time Daily (Once a day)" to 1,
        "2 Times Daily (Twice a day)" to 2,
        "3 Times Daily (Thrice a day)" to 3,
        "4 Times Daily (Four times a day)" to 4
    )

    val initialDoseCount = when {
        initialMedication.frequency.contains("2") || initialMedication.frequency.contains("twice", ignoreCase = true) -> 2
        initialMedication.frequency.contains("3") || initialMedication.frequency.contains("thrice", ignoreCase = true) || initialMedication.frequency.contains("three", ignoreCase = true) -> 3
        initialMedication.frequency.contains("4") || initialMedication.frequency.contains("four", ignoreCase = true) -> 4
        else -> 1
    }

    var selectedFrequency by remember { mutableStateOf(frequencyOptions.find { it.second == initialDoseCount } ?: frequencyOptions[0]) }
    var expandedFreq by remember { mutableStateOf(false) }

    // Parse initial time
    val initialCal = Calendar.getInstance().apply { timeInMillis = initialMedication.scheduledTime }
    val initHour = initialCal.get(Calendar.HOUR_OF_DAY)
    val initMin = initialCal.get(Calendar.MINUTE)

    var dose1Time by remember { mutableStateOf(Pair(if (initHour > 0) initHour else 8, initMin)) } // Default 8:00 AM
    var dose2Time by remember { mutableStateOf(Pair(14, 0)) } // Default 2:00 PM
    var dose3Time by remember { mutableStateOf(Pair(20, 0)) } // Default 8:00 PM
    var dose4Time by remember { mutableStateOf(Pair(22, 0)) } // Default 10:00 PM

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ReMed", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Review and adjust medication details:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Medication Name") },
                leadingIcon = { Icon(Icons.Default.LocalPharmacy, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage (e.g. 500mg)") },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                    label = { Text("Tablets/Dose") },
                    leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                )
            }

            // Frequency Selector
            ExposedDropdownMenuBox(
                expanded = expandedFreq,
                onExpandedChange = { expandedFreq = !expandedFreq },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedFrequency.first,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Frequency (Times Per Day)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFreq) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                ExposedDropdownMenu(
                    expanded = expandedFreq,
                    onDismissRequest = { expandedFreq = false }
                ) {
                    frequencyOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt.first) },
                            onClick = {
                                selectedFrequency = opt
                                expandedFreq = false
                            }
                        )
                    }
                }
            }

            // Dynamic Custom Time Pickers
            Text(
                "Custom Reminder Times for Each Dose:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Dose 1 Time
            CustomTimePickerField(
                label = "1st Dose Time",
                hour = dose1Time.first,
                minute = dose1Time.second,
                onTimeSelected = { h, m -> dose1Time = Pair(h, m) }
            )

            // Dose 2 Time (If >= 2 times a day)
            if (selectedFrequency.second >= 2) {
                CustomTimePickerField(
                    label = "2nd Dose Time",
                    hour = dose2Time.first,
                    minute = dose2Time.second,
                    onTimeSelected = { h, m -> dose2Time = Pair(h, m) }
                )
            }

            // Dose 3 Time (If >= 3 times a day)
            if (selectedFrequency.second >= 3) {
                CustomTimePickerField(
                    label = "3rd Dose Time",
                    hour = dose3Time.first,
                    minute = dose3Time.second,
                    onTimeSelected = { h, m -> dose3Time = Pair(h, m) }
                )
            }

            // Dose 4 Time (If >= 4 times a day)
            if (selectedFrequency.second >= 4) {
                CustomTimePickerField(
                    label = "4th Dose Time",
                    hour = dose4Time.first,
                    minute = dose4Time.second,
                    onTimeSelected = { h, m -> dose4Time = Pair(h, m) }
                )
            }

            OutlinedTextField(
                value = durationMonths,
                onValueChange = { durationMonths = it.filter { c -> c.isDigit() } },
                label = { Text("Duration (Months)") },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val count = selectedFrequency.second
                        val selectedTimes = listOf(dose1Time, dose2Time, dose3Time, dose4Time).take(count)

                        val createdMeds = selectedTimes.mapIndexed { index, (h, m) ->
                            val calendar = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, h)
                                set(Calendar.MINUTE, m)
                                set(Calendar.SECOND, 0)
                                if (timeInMillis < System.currentTimeMillis()) {
                                    add(Calendar.DAY_OF_YEAR, 1)
                                }
                            }
                            val doseLabel = if (count > 1) " (Dose ${index + 1})" else ""
                            initialMedication.copy(
                                name = "$name$doseLabel",
                                dosage = dosage,
                                frequency = selectedFrequency.first,
                                quantity = quantity.toIntOrNull() ?: 1,
                                durationMonths = durationMonths.toIntOrNull() ?: 1,
                                scheduledTime = calendar.timeInMillis
                            )
                        }
                        onSave(createdMeds)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = name.isNotBlank()
                ) {
                    Text("Save Medication")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTimePickerField(
    label: String,
    hour: Int,
    minute: Int,
    onTimeSelected: (Int, Int) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    val formattedTime = remember(hour, minute) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
    }

    Surface(
        onClick = { showTimePicker = true },
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = formattedTime,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.primary,
                disabledTrailingIconColor = MaterialTheme.colorScheme.primary
            ),
            label = { Text(label) },
            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
            trailingIcon = { Icon(Icons.Default.Edit, contentDescription = "Change Time") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            title = {
                Text(
                    text = "Select $label",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }
}
