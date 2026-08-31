package com.example.remed.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.remed.ocr.PrescriptionScanner
import com.example.remed.ui.MedicationViewModel
import kotlinx.coroutines.launch

@Composable
fun ScannerScreen(
    viewModel: MedicationViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCameraPermission = isGranted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var isScanning by remember { mutableStateOf(false) }
    var useHandwriting by remember { mutableStateOf(false) }
    var showScanError by remember { mutableStateOf(false) }
    val handwritingLines = remember { mutableStateListOf<Line>() }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var cameraPreviewView by remember { mutableStateOf<PreviewView?>(null) }

    if (showScanError) {
        AlertDialog(
            onDismissRequest = { showScanError = false },
            title = { Text("Scan Unsuccessful") },
            text = { Text("We couldn't clearly read the prescription. Would you like to try again or enter the details manually?") },
            confirmButton = {
                TextButton(onClick = {
                    showScanError = false
                    onNavigateBack()
                }) {
                    Text("Manual Entry")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScanError = false }) {
                    Text("Try Again")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text("Scan Prescription", style = MaterialTheme.typography.titleLarge)
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Handwriting", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(checked = useHandwriting, onCheckedChange = { useHandwriting = it })
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                canvasSize = coordinates.size
            }
        ) {
            if (useHandwriting) {
                HandwritingCanvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    lines = handwritingLines
                )
            } else if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            cameraPreviewView = this
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(surfaceProvider)
                                }
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text("Camera permission is needed for scanning")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant Permission")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.weight(1f),
                enabled = !isScanning
            ) {
                Text("Cancel")
            }

            if (useHandwriting) {
                OutlinedButton(
                    onClick = { handwritingLines.clear() },
                    modifier = Modifier.weight(1f),
                    enabled = !isScanning
                ) {
                    Text("Clear")
                }
            }
            
            Button(
                onClick = {
                    scope.launch {
                        isScanning = true
                        val success = if (useHandwriting) {
                            val bitmap = captureCanvasToBitmap(
                                handwritingLines,
                                if (canvasSize.width > 0) canvasSize.width else 720,
                                if (canvasSize.height > 0) canvasSize.height else 1280
                            )
                            viewModel.onHandwritingScanned(bitmap)
                        } else {
                            cameraPreviewView?.bitmap?.let { bitmap ->
                                viewModel.onImageScanned(bitmap)
                            } ?: false
                        }
                        
                        isScanning = false
                        if (success) {
                            onNavigateBack()
                        } else {
                            showScanError = true
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isScanning && (useHandwriting && handwritingLines.isNotEmpty() || !useHandwriting)
            ) {
                Text(if (isScanning) "Processing..." else "Process")
            }
        }
    }
}
