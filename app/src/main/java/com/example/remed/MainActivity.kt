package com.example.remed

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remed.ocr.HandwritingProcessor
import com.example.remed.ocr.PrescriptionScanner
import com.example.remed.ui.MedicationViewModel
import com.example.remed.ui.WaterViewModel
import com.example.remed.ui.components.DashboardScreen
import com.example.remed.ui.components.ScannerScreen
import com.example.remed.ui.theme.ReMedTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var medicationViewModel: MedicationViewModel

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            takePicture.launch(null)
        }
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted.
        }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            lifecycleScope.launch {
                medicationViewModel.onImageScanned(bitmap)
            }
        }
    }

    private val selectFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(this.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
            lifecycleScope.launch {
                medicationViewModel.onImageScanned(bitmap)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent().also { intent ->
                    intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    intent.data = Uri.fromParts("package", packageName, null)
                    startActivity(intent)
                }
            }
        }

        val app = application as RemedApplication
        val repository = app.repository

        setContent {
            ReMedTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            val viewModel = when {
                                modelClass.isAssignableFrom(MedicationViewModel::class.java) -> {
                                    MedicationViewModel(
                                        app,
                                        repository,
                                        PrescriptionScanner(this@MainActivity),
                                        HandwritingProcessor(this@MainActivity)
                                    )
                                }
                                modelClass.isAssignableFrom(WaterViewModel::class.java) -> {
                                    WaterViewModel(app, repository)
                                }
                                else -> {
                                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                                }
                            }
                            @Suppress("UNCHECKED_CAST")
                            return viewModel as T
                        }
                    }
                    medicationViewModel = viewModel(factory = factory)
                    val waterViewModel: WaterViewModel = viewModel(factory = factory)
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()

                    var currentScreen by remember { mutableStateOf("dashboard") }

                    if (currentScreen == "dashboard") {
                        DashboardScreen(
                            medViewModel = medicationViewModel,
                            waterViewModel = waterViewModel,
                            onScanPrescription = { currentScreen = "scanner" },
                            onSelectFromGallery = { selectFromGallery.launch("image/*") },
                            drawerState = drawerState,
                            onMenuClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        )
                    } else {
                        ScannerScreen(
                            viewModel = medicationViewModel,
                            onNavigateBack = { currentScreen = "dashboard" }
                        )
                    }
                }
            }
        }
    }
}
