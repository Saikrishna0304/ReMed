package com.example.remed.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.remed.ocr.PrescriptionScanner
import com.example.remed.ui.MedicationViewModel

@Composable
fun ScannerScreen(
    viewModel: MedicationViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    var isScanning by remember { mutableStateOf(false) }
    var useHandwriting by remember { mutableStateOf(false) }
    val handwritingLines = remember { mutableStateListOf<Line>() }

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

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (useHandwriting) {
                HandwritingCanvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    lines = handwritingLines
                )
            } else {
                Text("Camera View Placeholder", modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (useHandwriting) {
                OutlinedButton(
                    onClick = { handwritingLines.clear() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }
            
            Button(
                onClick = {
                    isScanning = true
                    if (useHandwriting) {
                        // In a real scenario, we'd get the actual pixel dimensions of the Box
                        // Here we approximate or use a fixed size for the TFLite model
                        val bitmap = captureCanvasToBitmap(
                            handwritingLines, 
                            720, // Approximate width
                            1280 // Approximate height
                        )
                        viewModel.onHandwritingScanned(bitmap)
                        onNavigateBack()
                    } else {
                        // Camera scan logic would go here
                        isScanning = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isScanning && (!useHandwriting || handwritingLines.isNotEmpty())
            ) {
                Text(if (isScanning) "Processing..." else "Process")
            }
        }
    }
}
