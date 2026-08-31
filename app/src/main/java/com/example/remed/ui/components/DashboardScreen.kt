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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
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
import com.example.remed.R
import com.example.remed.data.Medication
import com.example.remed.data.WaterLog
import com.example.remed.ui.MedicationViewModel
import com.example.remed.ui.WaterViewModel

import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import com.example.remed.data.WaterSettings

import com.example.remed.ui.AuthViewModel
import com.example.remed.ui.StepViewModel
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.graphicsLayer
import com.example.remed.data.StepLog
import com.example.remed.data.StepSettings

import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import com.example.remed.ui.components.MedicationEditScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    medViewModel: MedicationViewModel,
    waterViewModel: WaterViewModel,
    stepViewModel: StepViewModel,
    onScanPrescription: () -> Unit,
    onSelectFromGallery: () -> Unit,
    drawerState: DrawerState,
    onMenuClick: () -> Unit
) {
    val medications by medViewModel.allMedications.collectAsState()
    val waterLog by waterViewModel.waterLog.collectAsState()
    val waterSettings by waterViewModel.waterSettings.collectAsState()
    val stepLog by stepViewModel.stepLog.collectAsState()
    val stepSettings by stepViewModel.stepSettings.collectAsState()
    val scannedMedication by medViewModel.scannedMedication.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()
    val family by authViewModel.family.collectAsState()
    val isGuest by authViewModel.isGuestMode.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableStateOf("prescription") }

    var editingScannedMedication by remember { mutableStateOf<Medication?>(null) }

    var showManualAddDialog by remember { mutableStateOf(false) }
    var manualName by remember { mutableStateOf("") }
    var manualDosage by remember { mutableStateOf("") }
    var manualFrequency by remember { mutableStateOf("") }

    val timeSlots = listOf(
        "Early Morning (6am - 9am)" to 7,
        "Morning (9am - 12pm)" to 10,
        "Afternoon (12pm - 3pm)" to 13,
        "Evening (3pm - 6pm)" to 16,
        "Night (6pm - 9pm)" to 20
    )
    var selectedTimeSlot by remember { mutableStateOf(timeSlots[0]) }
    var expanded by remember { mutableStateOf(false) }

    if (showManualAddDialog) {
        AlertDialog(
            onDismissRequest = { showManualAddDialog = false },
            title = { Text("Add Medication Manually") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = manualName,
                        onValueChange = { manualName = it },
                        label = { Text("Medication Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = manualDosage,
                        onValueChange = { manualDosage = it },
                        label = { Text("Dosage (e.g. 500mg)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = manualFrequency,
                        onValueChange = { manualFrequency = it },
                        label = { Text("Frequency (e.g. Daily)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            value = selectedTimeSlot.first,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Reminder Time") },
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
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (manualName.isNotBlank()) {
                            val calendar = java.util.Calendar.getInstance().apply {
                                set(java.util.Calendar.HOUR_OF_DAY, selectedTimeSlot.second)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                // If the time has already passed today, schedule for tomorrow
                                if (timeInMillis < System.currentTimeMillis()) {
                                    add(java.util.Calendar.DAY_OF_YEAR, 1)
                                }
                            }

                            medViewModel.insert(
                                Medication(
                                    name = manualName,
                                    dosage = manualDosage.ifBlank { "As prescribed" },
                                    frequency = manualFrequency.ifBlank { "Daily" },
                                    scheduledTime = calendar.timeInMillis
                                )
                            )
                            showManualAddDialog = false
                            manualName = ""
                            manualDosage = ""
                            manualFrequency = ""
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (scannedMedication != null || editingScannedMedication != null) {
        val medToEdit = editingScannedMedication ?: scannedMedication!!
        
        Surface(modifier = Modifier.fillMaxSize()) {
            MedicationEditScreen(
                initialMedication = medToEdit,
                onSave = { updatedMed ->
                    medViewModel.insert(updatedMed)
                    medViewModel.clearScannedMedication()
                    editingScannedMedication = null
                },
                onCancel = {
                    medViewModel.clearScannedMedication()
                    editingScannedMedication = null
                }
            )
        }
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
                userProfile = userProfile,
                family = family,
                isGuest = isGuest,
                onPrescriptionClick = {
                    selectedTab = "prescription"
                    scope.launch { drawerState.close() }
                },
                onHydrationClick = {
                    selectedTab = "hydration"
                    scope.launch { drawerState.close() }
                },
                onStepClick = {
                    selectedTab = "steps"
                    scope.launch { drawerState.close() }
                },
                onSignOutClick = {
                    authViewModel.signOut()
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            when(selectedTab) {
                                "prescription" -> "Prescriptions"
                                "hydration" -> "Hydration"
                                else -> "Step Counter"
                            }
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.remed_logo),
                                contentDescription = "Menu",
                                modifier = Modifier.size(40.dp),
                                tint = androidx.compose.ui.graphics.Color.Unspecified
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                if (selectedTab == "prescription") {
                    FloatingActionButton(onClick = { showManualAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Medication")
                    }
                }
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
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = "Steps") },
                        label = { Text("Steps") },
                        selected = selectedTab == "steps",
                        onClick = { selectedTab = "steps" }
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
                when (selectedTab) {
                    "prescription" -> {
                        Text("Prescription Scanner", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onScanPrescription,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Icon(Icons.Default.LocalPharmacy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Scan", style = MaterialTheme.typography.labelMedium)
                            }
                            Button(
                                onClick = onSelectFromGallery,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text("Gallery", style = MaterialTheme.typography.labelMedium)
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
                    }
                    "hydration" -> {
                        Text("Hydration Tracking", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(16.dp))

                        WaterCard(
                            log = waterLog,
                            settings = waterSettings,
                            onAdd = { amount -> waterViewModel.addWater(amount) },
                            onRemove = { amount -> waterViewModel.removeWater(amount) },
                            onUpdateSettings = { goal, quick, interval -> waterViewModel.updateSettings(goal, quick, interval) }
                        )
                    }
                    "steps" -> {
                        Text("Step Counter", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        StepCard(
                            log = stepLog,
                            settings = stepSettings,
                            onUpdateGoal = { goal: Int -> stepViewModel.updateGoal(goal) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepCard(
    log: StepLog?,
    settings: StepSettings,
    onUpdateGoal: (Int) -> Unit
) {
    var showGoalDialog by remember { mutableStateOf(false) }
    val progress = ((log?.count ?: 0) / settings.dailyGoal.toFloat()).coerceIn(0f, 1f)
    val primaryColor = MaterialTheme.colorScheme.primary

    if (showGoalDialog) {
        var goalText by remember { mutableStateOf(settings.dailyGoal.toString()) }
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Set Daily Step Goal") },
            text = {
                TextField(
                    value = goalText,
                    onValueChange = { goalText = it.filter { c -> c.isDigit() } },
                    label = { Text("Daily Steps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateGoal(goalText.toIntOrNull() ?: 5000)
                    showGoalDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Step Counter",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Every step counts towards a healthier you!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { showGoalDialog = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = primaryColor)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Aesthetic Shoe and Meter
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                // Circular Progress Meter
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    // Background track
                    drawArc(
                        color = primaryColor.copy(alpha = 0.1f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )
                    // Progress arc
                    drawArc(
                        color = primaryColor,
                        startAngle = 135f,
                        sweepAngle = 270f * progress,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )
                }

                // Walking Shoe in the middle
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                        // Motion Lines
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val lineLength = 15.dp.toPx()
                            val spacing = 8.dp.toPx()
                            // Three lines below the shoe
                            for (i in 0..2) {
                                drawLine(
                                    color = primaryColor.copy(alpha = 0.4f),
                                    start = androidx.compose.ui.geometry.Offset(
                                        x = size.width / 2 - (i - 1) * spacing,
                                        y = size.height / 2 + 35.dp.toPx()
                                    ),
                                    end = androidx.compose.ui.geometry.Offset(
                                        x = size.width / 2 - (i - 1) * spacing - lineLength,
                                        y = size.height / 2 + 45.dp.toPx()
                                    ),
                                    strokeWidth = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                                .graphicsLayer(
                                    rotationZ = -15f, // Tilted up "walking" look
                                    translationY = -10f
                                ),
                            tint = primaryColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "${log?.count ?: 0}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor
                    )
                    Text(
                        "of ${settings.dailyGoal} steps",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress percentage
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = primaryColor,
                trackColor = primaryColor.copy(alpha = 0.1f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${(progress * 100).toInt()}% of your goal",
                style = MaterialTheme.typography.bodySmall,
                color = primaryColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WaterCard(
    log: WaterLog?,
    settings: WaterSettings,
    onAdd: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onUpdateSettings: (Int, Int, Long) -> Unit
) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    val progress = ((log?.amount ?: 0) / settings.dailyGoal.toFloat()).coerceIn(0f, 1f)

    if (showSettingsDialog) {
        WaterSettingsDialog(
            currentSettings = settings,
            onDismiss = { showSettingsDialog = false },
            onConfirm = { goal, quick, interval ->
                onUpdateSettings(goal, quick, interval)
                showSettingsDialog = false
            }
        )
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Daily Hydration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Keep it up! You're doing great.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { showSettingsDialog = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Aesthetic Progress Indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                // Background Circle
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {}
                
                // Progress "Water" Effect (Simplified)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.9f * progress)
                        .align(Alignment.BottomCenter)
                        .background(
                            color = Color(0xFF2196F3).copy(alpha = 0.6f),
                            shape = CircleShape
                        )
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${log?.amount ?: 0}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "of ${settings.dailyGoal} ml",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onRemove(settings.quickAddAmount) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Undo")
                }
                Button(
                    onClick = { onAdd(settings.quickAddAmount) },
                    modifier = Modifier.weight(2f),
                    shape = MaterialTheme.shapes.medium,
                    elevation = androidx.compose.material3.ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add ${settings.quickAddAmount}ml")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onAdd(500) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("+500ml")
                }
                OutlinedButton(
                    onClick = { onAdd(1000) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("+1L")
                }
            }
        }
    }
}

@Composable
fun WaterSettingsDialog(
    currentSettings: WaterSettings,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Long) -> Unit
) {
    var goal by remember { mutableStateOf(currentSettings.dailyGoal.toString()) }
    var quickAdd by remember { mutableStateOf(currentSettings.quickAddAmount.toString()) }
    var interval by remember { mutableStateOf(currentSettings.reminderInterval.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hydration Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = goal,
                    onValueChange = { goal = it.filter { c -> c.isDigit() } },
                    label = { Text("Daily Goal (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = quickAdd,
                    onValueChange = { quickAdd = it.filter { c -> c.isDigit() } },
                    label = { Text("Quick Add Amount (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = interval,
                    onValueChange = { interval = it.filter { c -> c.isDigit() } },
                    label = { Text("Reminder Interval (min, 0 to disable)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        goal.toIntOrNull() ?: 2000,
                        quickAdd.toIntOrNull() ?: 250,
                        interval.toLongOrNull() ?: 0L
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MedicationItem(med: Medication, onTaken: () -> Unit) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val scheduledTimeStr = remember(med.scheduledTime) { timeFormatter.format(Date(med.scheduledTime)) }
    val endDateStr = remember(med.endDate) { dateFormatter.format(Date(med.endDate)) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalPharmacy,
                        contentDescription = null,
                        tint = if (med.isTaken) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        med.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (med.isTaken) TextDecoration.LineThrough else null,
                        color = if (med.isTaken) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    "${med.dosage} • ${med.quantity} tab(s) • ${med.frequency}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Next: $scheduledTimeStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.width(12.dp))
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Until: $endDateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            IconButton(
                onClick = onTaken,
                enabled = !med.isTaken,
                modifier = Modifier
                    .background(
                        if (med.isTaken) MaterialTheme.colorScheme.surfaceVariant 
                        else MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    )
                    .size(40.dp)
            ) {
                Icon(
                    if (med.isTaken) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = "Mark Taken",
                    tint = if (med.isTaken) MaterialTheme.colorScheme.outline 
                           else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
