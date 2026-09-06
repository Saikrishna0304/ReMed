package com.example.remed.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.remed.data.Family
import com.example.remed.data.UserProfile
import com.example.remed.ui.theme.AppTheme
import com.example.remed.ui.theme.ThemeManager
import java.io.File
import java.io.FileOutputStream

@Composable
fun DrawerContent(
    selectedTab: String,
    userProfile: UserProfile?,
    family: Family?,
    isGuest: Boolean,
    onUpdateProfile: (String, String?, String?, Int?, Float?, Float?) -> Unit = { _, _, _, _, _, _ -> },
    onPrescriptionClick: () -> Unit,
    onHydrationClick: () -> Unit,
    onStepClick: () -> Unit,
    onKeepTrackClick: () -> Unit = {},
    onSignOutClick: () -> Unit
) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    ModalDrawerSheet {
        Spacer(Modifier.height(16.dp))

        // User Info Header with Profile Avatar
        Surface(
            onClick = { showProfileDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    ProfileAvatar(
                        photoUrl = userProfile?.photoUrl,
                        size = 48.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isGuest) "Offline Mode" else (userProfile?.name?.ifBlank { "User" } ?: "User"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val extraDetails = mutableListOf<String>()
                        userProfile?.gender?.let { if (it.isNotBlank()) extraDetails.add(it) }
                        userProfile?.age?.let { extraDetails.add("$it yrs") }
                        userProfile?.bmi?.let { extraDetails.add("BMI: ${String.format(Locale.getDefault(), "%.1f", it)}") }

                        if (extraDetails.isNotEmpty()) {
                            Text(
                                text = extraDetails.joinToString(" • "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                IconButton(onClick = { showProfileDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (isGuest) {
            Text(
                text = "Data is saved locally only",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 28.dp, bottom = 12.dp)
            )
        } else if (family != null) {
            Text(
                text = "Family: ${family.name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 28.dp, bottom = 4.dp)
            )
            Text(
                text = "Join Code: ${family.joinCode}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 28.dp, bottom = 12.dp)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        NavigationDrawerItem(
            label = { Text("Prescriptions") },
            selected = selectedTab == "prescription",
            onClick = onPrescriptionClick,
            icon = { Icon(Icons.Default.LocalPharmacy, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("Hydration") },
            selected = selectedTab == "hydration",
            onClick = onHydrationClick,
            icon = { Icon(Icons.Default.WaterDrop, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("Steps") },
            selected = selectedTab == "steps",
            onClick = onStepClick,
            icon = { Icon(Icons.Default.DirectionsWalk, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        val isParent = (userProfile?.role == "parent") || (family != null && family.adminId == userProfile?.uid)

        if (isParent) {
            NavigationDrawerItem(
                label = { Text("Family") },
                selected = selectedTab == "keep_track",
                onClick = onKeepTrackClick,
                icon = { Icon(Icons.Default.Group, contentDescription = "Family") },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Settings Option
        NavigationDrawerItem(
            label = { Text("Settings") },
            selected = false,
            onClick = { showSettingsDialog = true },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text(if (isGuest) "Sign In to Backup" else "Sign Out") },
            selected = false,
            onClick = onSignOutClick,
            icon = {
                Icon(
                    if (isGuest) Icons.Default.CloudUpload else Icons.Default.ExitToApp,
                    contentDescription = null
                )
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        Spacer(Modifier.height(12.dp))
    }

    if (showSettingsDialog) {
        SettingsDialog(onDismiss = { showSettingsDialog = false })
    }

    if (showProfileDialog) {
        EditProfileDialog(
            userProfile = userProfile,
            onDismiss = { showProfileDialog = false },
            onSave = { name, photoUrl, gender, age, weight, height ->
                onUpdateProfile(name, photoUrl, gender, age, weight, height)
            }
        )
    }
}

@Composable
fun ProfileAvatar(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val context = LocalContext.current
    var bitmap by remember(photoUrl) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(photoUrl) {
        if (!photoUrl.isNullOrEmpty()) {
            try {
                if (photoUrl.startsWith("data:image") || photoUrl.contains("base64,")) {
                    val base64Data = photoUrl.substringAfter("base64,")
                    val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    bitmap = decodedBitmap.asImageBitmap()
                } else {
                    val uri = Uri.parse(photoUrl)
                    val loadedBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                    bitmap = loadedBitmap.asImageBitmap()
                }
            } catch (e: Exception) {
                bitmap = null
            }
        } else {
            bitmap = null
        }
    }

    Surface(
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = CircleShape
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = "Profile Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Icon",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(size * 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    userProfile: UserProfile?,
    onDismiss: () -> Unit,
    onSave: (String, String?, String?, Int?, Float?, Float?) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(userProfile?.name ?: "") }
    var photoUrl by remember { mutableStateOf(userProfile?.photoUrl) }
    var gender by remember { mutableStateOf(userProfile?.gender ?: "Male") }
    var ageText by remember { mutableStateOf(userProfile?.age?.toString() ?: "") }
    var weightText by remember { mutableStateOf(userProfile?.weightKg?.toString() ?: "") }
    var heightText by remember { mutableStateOf(userProfile?.heightCm?.toString() ?: "") }

    var expandedGender by remember { mutableStateOf(false) }
    val genderOptions = listOf("Male", "Female", "Other")

    val currentWeight = weightText.toFloatOrNull()
    val currentHeight = heightText.toFloatOrNull()
    val liveBmi = remember(currentWeight, currentHeight) {
        if (currentWeight != null && currentHeight != null && currentHeight > 0f && currentWeight > 0f) {
            val hM = currentHeight / 100f
            currentWeight / (hM * hM)
        } else null
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val loadedBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                
                val scaledBitmap = Bitmap.createScaledBitmap(loadedBitmap, 200, 200, true)
                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val bytes = outputStream.toByteArray()
                val base64Str = Base64.encodeToString(bytes, Base64.NO_WRAP)
                photoUrl = "data:image/jpeg;base64,$base64Str"
            } catch (e: Exception) {
                photoUrl = it.toString()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Edit Health Profile",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Profile Avatar
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                ) {
                    ProfileAvatar(photoUrl = photoUrl, size = 80.dp)
                }

                OutlinedButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Change Photo", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                // Gender & Age Row
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedGender,
                        onExpandedChange = { expandedGender = !expandedGender },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gender") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        )
                        ExposedDropdownMenu(
                            expanded = expandedGender,
                            onDismissRequest = { expandedGender = false }
                        ) {
                            genderOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        gender = opt
                                        expandedGender = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it.filter { c -> c.isDigit() } },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                // Weight & Height Row
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                // Live Calculated BMI Badge
                if (liveBmi != null) {
                    val category = when {
                        liveBmi < 18.5f -> "Underweight"
                        liveBmi < 25.0f -> "Normal weight"
                        liveBmi < 30.0f -> "Overweight"
                        else -> "Obese"
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "BMI: ${String.format(Locale.getDefault(), "%.1f", liveBmi)} ($category)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name.trim(),
                        photoUrl,
                        gender,
                        ageText.toIntOrNull(),
                        weightText.toFloatOrNull(),
                        heightText.toFloatOrNull()
                    )
                    onDismiss()
                },
                enabled = name.isNotBlank()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentTheme = ThemeManager.currentTheme
    var activeSubMenu by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (activeSubMenu) {
                                    "appearance" -> "Appearance"
                                    "about" -> "About ReMed"
                                    "version" -> "Version Info"
                                    else -> "Settings"
                                },
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    if (activeSubMenu != null) {
                                        activeSubMenu = null
                                    } else {
                                        onDismiss()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (activeSubMenu) {
                        "appearance" -> {
                            Text(
                                text = "Choose your preferred theme appearance:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            ThemeOptionItem(
                                title = "☀️ Light Theme",
                                subtitle = "Clean and bright appearance",
                                selected = currentTheme == AppTheme.LIGHT,
                                onClick = {
                                    ThemeManager.setTheme(context, AppTheme.LIGHT)
                                }
                            )

                            ThemeOptionItem(
                                title = "🌅 Solarized Light Theme",
                                subtitle = "Warm cream palette for eye comfort",
                                selected = currentTheme == AppTheme.SOLARIZED_LIGHT,
                                onClick = {
                                    ThemeManager.setTheme(context, AppTheme.SOLARIZED_LIGHT)
                                }
                            )

                            ThemeOptionItem(
                                title = "🌙 Dark Theme",
                                subtitle = "Comfortable for low light",
                                selected = currentTheme == AppTheme.DARK,
                                onClick = {
                                    ThemeManager.setTheme(context, AppTheme.DARK)
                                }
                            )

                            ThemeOptionItem(
                                title = "🖤 Dark Black Theme",
                                subtitle = "Pure black for AMOLED displays",
                                selected = currentTheme == AppTheme.BLACK,
                                onClick = {
                                    ThemeManager.setTheme(context, AppTheme.BLACK)
                                }
                            )
                        }

                        "about" -> {
                            Surface(
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocalPharmacy,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "ReMed Companion",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "ReMed is a smart family health and medication companion designed to help you track prescriptions with AI scanning, monitor daily hydration, and count steps effortlessly.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        "version" -> {
                            Surface(
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Version",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "v1.0.0",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Build Number",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "100",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Status",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Up to date",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        else -> {
                            SettingsMenuItem(
                                title = "Appearance",
                                subtitle = when (currentTheme) {
                                    AppTheme.LIGHT -> "☀️ Light Theme"
                                    AppTheme.SOLARIZED_LIGHT -> "🌅 Solarized Light"
                                    AppTheme.DARK -> "🌙 Dark Theme"
                                    AppTheme.BLACK -> "🖤 Dark Black Theme"
                                },
                                icon = Icons.Default.Palette,
                                onClick = { activeSubMenu = "appearance" }
                            )

                            SettingsMenuItem(
                                title = "About",
                                subtitle = "App information & features",
                                icon = Icons.Default.Info,
                                onClick = { activeSubMenu = "about" }
                            )

                            SettingsMenuItem(
                                title = "Version",
                                subtitle = "v1.0.0 (Build 100)",
                                icon = Icons.Default.Verified,
                                onClick = { activeSubMenu = "version" }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThemeOptionItem(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick
            )
        }
    }
}
