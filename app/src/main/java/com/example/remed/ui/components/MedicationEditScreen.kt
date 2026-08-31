package com.example.remed.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.remed.data.Medication
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationEditScreen(
    initialMedication: Medication,
    onSave: (Medication) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initialMedication.name) }
    var dosage by remember { mutableStateOf(initialMedication.dosage) }
    var frequency by remember { mutableStateOf(initialMedication.frequency) }
    var quantity by remember { mutableStateOf(initialMedication.quantity.toString()) }
    var durationMonths by remember { mutableStateOf(initialMedication.durationMonths.toString()) }

    val timeSlots = listOf(
        "Early Morning (7am)" to 7,
        "Morning (10am)" to 10,
        "Afternoon (1pm)" to 13,
        "Evening (5pm)" to 17,
        "Night (9pm)" to 21
    )
    var selectedTimeSlot by remember { mutableStateOf(timeSlots[0]) }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Review Prescription") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Confirm medication details from prescription",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Medication Name") },
                leadingIcon = { Icon(Icons.Default.LocalPharmacy, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                    label = { Text("Tablets/Dose") },
                    leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = frequency,
                onValueChange = { frequency = it },
                label = { Text("Frequency (e.g. Twice Daily)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedTimeSlot.first,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Start Time") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        timeSlots.forEach { slot ->
                            DropdownMenuItem(
                                text = { Text(slot.first) },
                                onClick = {
                                    selectedTimeSlot = slot
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = durationMonths,
                    onValueChange = { durationMonths = it.filter { c -> c.isDigit() } },
                    label = { Text("Duration (Months)") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Discard")
                }
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, selectedTimeSlot.second)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            if (timeInMillis < System.currentTimeMillis()) {
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                        }
                        onSave(
                            initialMedication.copy(
                                name = name,
                                dosage = dosage,
                                frequency = frequency,
                                quantity = quantity.toIntOrNull() ?: 1,
                                durationMonths = durationMonths.toIntOrNull() ?: 1,
                                scheduledTime = calendar.timeInMillis
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = name.isNotBlank()
                ) {
                    Text("Save Schedule")
                }
            }
        }
    }
}
