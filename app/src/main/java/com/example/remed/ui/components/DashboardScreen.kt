package com.example.remed.ui.components

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import com.example.remed.data.Medication
import com.example.remed.data.WaterLog
import com.example.remed.ui.MedicationViewModel
import com.example.remed.ui.WaterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    medViewModel: MedicationViewModel,
    waterViewModel: WaterViewModel,
    onScanPrescription: () -> Unit,
    onSelectFromGallery: () -> Unit,
    drawerState: DrawerState,
    onMenuClick: () -> Unit
) {
    val medications by medViewModel.allMedications.collectAsState()
    val waterLog by waterViewModel.waterLog.collectAsState()
    val scannedMedication by medViewModel.scannedMedication.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableStateOf("prescription") }

    if (scannedMedication != null) {
        AlertDialog(
            onDismissRequest = { medViewModel.clearScannedMedication() },
            title = { Text("Confirm Scanned Medication") },
            text = {
                Column {
                    Text("Name: ${scannedMedication?.name}")
                    Text("Dosage: ${scannedMedication?.dosage}")
                    Text("Frequency: ${scannedMedication?.frequency}")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scannedMedication?.let { medViewModel.insert(it) }
                        medViewModel.clearScannedMedication()
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { medViewModel.clearScannedMedication() }) {
                    Text("Cancel")
                }
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission Accepted
        } else {
            // Permission Denied
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    // pass
                }
                else -> {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                selectedTab = selectedTab,
                onPrescriptionClick = {
                    selectedTab = "prescription"
                    scope.launch { drawerState.close() }
                },
                onHydrationClick = {
                    selectedTab = "hydration"
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (selectedTab == "prescription") "Prescriptions" else "Hydration") },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.LocalPharmacy, contentDescription = "Prescription") },
                        label = { Text("Prescription") },
                        selected = selectedTab == "prescription",
                        onClick = { selectedTab = "prescription" }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.WaterDrop, contentDescription = "Hydration") },
                        label = { Text("Hydration") },
                        selected = selectedTab == "hydration",
                        onClick = { selectedTab = "hydration" }
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                if (selectedTab == "prescription") {
                    Text("Prescription Scanner", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onScanPrescription,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.LocalPharmacy, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Scan (Rx)")
                        }
                        Button(
                            onClick = onSelectFromGallery,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Gallery")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Medication Schedule", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(medications) { med ->
                            MedicationItem(med) { medViewModel.markAsTaken(med) }
                        }
                    }
                } else {
                    Text("Hydration Tracking", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    WaterCard(
                        log = waterLog,
                        onAdd = { waterViewModel.addWater(250) },
                        onRemove = { waterViewModel.removeWater(250) },
                        onSetReminder = { interval -> waterViewModel.setWaterReminderInterval(interval) },
                        onCancelReminders = { waterViewModel.cancelWaterReminders() }
                    )
                }
            }
        }
    }
}

@Composable
fun WaterCard(log: WaterLog?, onAdd: () -> Unit, onRemove: () -> Unit, onSetReminder: (Long) -> Unit, onCancelReminders: () -> Unit) {
    var showIntervalDialog by remember { mutableStateOf(false) }
    var interval by remember { mutableStateOf("") }
    val context = LocalContext.current

    val onSetReminderClick = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                showIntervalDialog = true
            } else {
                Intent().also { intent ->
                    intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    intent.data = Uri.fromParts("package", context.packageName, null)
                    context.startActivity(intent)
                }
            }
        } else {
            showIntervalDialog = true
        }
    }

    if (showIntervalDialog) {
        AlertDialog(
            onDismissRequest = { showIntervalDialog = false },
            title = { Text("Set Reminder Interval") },
            text = {
                TextField(
                    value = interval,
                    onValueChange = { interval = it },
                    label = { Text("Interval in minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intervalLong = interval.toLongOrNull()
                        if (intervalLong != null) {
                            onSetReminder(intervalLong)
                        }
                        showIntervalDialog = false
                    }
                ) {
                    Text("Set")
                }
            },
            dismissButton = {
                TextButton(onClick = { showIntervalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Hydration Status", style = MaterialTheme.typography.titleMedium)
            Text("${log?.amount ?: 0} / 1000 ml", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            
            LinearProgressIndicator(
                progress = { ((log?.amount ?: 0) / 1000f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAdd, modifier = Modifier.weight(1f)) { Text("Add 250ml") }
                Button(onClick = onRemove, modifier = Modifier.weight(1f)) { Text("Remove") }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onSetReminderClick() }, modifier = Modifier.weight(1f)) {
                    Text("Set Reminder")
                }
                Button(onClick = onCancelReminders, modifier = Modifier.weight(1f)) {
                    Text("Stop Reminders")
                }
            }
        }
    }
}

@Composable
fun MedicationItem(med: Medication, onTaken: () -> Unit) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val scheduledTimeStr = remember(med.scheduledTime) { timeFormatter.format(Date(med.scheduledTime)) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    med.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (med.isTaken) TextDecoration.LineThrough else null,
                    color = if (med.isTaken) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${med.dosage} • ${med.frequency} • $scheduledTimeStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Checkbox(
                checked = med.isTaken,
                onCheckedChange = { if (it) onTaken() },
                enabled = !med.isTaken
            )
        }
    }
}
