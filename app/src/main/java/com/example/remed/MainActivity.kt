package com.example.remed

import android.Manifest
import android.animation.ObjectAnimator
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remed.ocr.HandwritingProcessor
import com.example.remed.ocr.PrescriptionScanner
import com.example.remed.ui.AuthViewModel
import com.example.remed.ui.MedicationViewModel
import com.example.remed.ui.StepViewModel
import com.example.remed.ui.WaterViewModel
import com.example.remed.ui.components.DashboardScreen
import com.example.remed.ui.components.FamilySetupScreen
import com.example.remed.ui.components.LoginScreen
import com.example.remed.ui.components.ScannerScreen
import com.example.remed.ui.theme.ReMedTheme
import com.example.remed.ui.theme.ThemeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var medicationViewModel: MedicationViewModel

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            takePicture.launch(null)
        }
    }

    private val requestActivityRecognitionPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Activity recognition permission granted
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
                @Suppress("DEPRECATION")
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
        val splashScreen = installSplashScreen()
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val splashView = splashScreenViewProvider.view
            val alphaAnimator = ObjectAnimator.ofFloat(splashView, View.ALPHA, 1f, 0f)
            alphaAnimator.duration = 300L
            alphaAnimator.interpolator = AccelerateDecelerateInterpolator()
            alphaAnimator.doOnEnd { splashScreenViewProvider.remove() }
            alphaAnimator.start()
        }

        super.onCreate(savedInstanceState)
        ThemeManager.init(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestActivityRecognitionPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val app = application as RemedApplication
        val repository = app.repository
        val authRepository = app.authRepository

        setContent {
            ReMedTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showIntroSplash by remember { mutableStateOf(true) }

                    if (showIntroSplash) {
                        IntroSplashScreen(
                            onAnimationComplete = { showIntroSplash = false }
                        )
                    } else {
                        val authViewModel: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return AuthViewModel(authRepository) as T
                            }
                        })

                        val factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                val viewModel = when {
                                    modelClass.isAssignableFrom(MedicationViewModel::class.java) -> {
                                        MedicationViewModel(
                                            app,
                                            repository,
                                            authViewModel.userId,
                                            PrescriptionScanner(app),
                                            HandwritingProcessor(app)
                                        )
                                    }
                                    modelClass.isAssignableFrom(WaterViewModel::class.java) -> {
                                        WaterViewModel(app, repository, authViewModel.userId)
                                    }
                                    modelClass.isAssignableFrom(StepViewModel::class.java) -> {
                                        StepViewModel(app, repository, authViewModel.userId)
                                    }
                                    else -> {
                                        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                                    }
                                }
                                @Suppress("UNCHECKED_CAST")
                                return viewModel as T
                            }
                        }

                        val currentUser by authViewModel.currentUser.collectAsState()
                        val userProfile by authViewModel.userProfile.collectAsState()
                        val family by authViewModel.family.collectAsState()
                        val isGuestMode by authViewModel.isGuestMode.collectAsState()

                        if (currentUser == null && !isGuestMode) {
                            LoginScreen(authViewModel = authViewModel, onLoginSuccess = { /* Managed by collectAsState */ })
                        } else if (!isGuestMode && (userProfile == null || family == null)) {
                            FamilySetupScreen(authViewModel = authViewModel)
                        } else {
                            medicationViewModel = viewModel(factory = factory)
                            val waterViewModel: WaterViewModel = viewModel(factory = factory)
                            val stepViewModel: StepViewModel = viewModel(factory = factory)
                            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                            val scope = rememberCoroutineScope()

                            var currentScreen by remember { mutableStateOf("dashboard") }

                            if (currentScreen == "dashboard") {
                                DashboardScreen(
                                    authViewModel = authViewModel,
                                    medViewModel = medicationViewModel,
                                    waterViewModel = waterViewModel,
                                    stepViewModel = stepViewModel,
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
    }
}

@Composable
fun IntroSplashScreen(
    onAnimationComplete: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "introAlpha"
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.85f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "introScale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1200)
        startAnimation = false
        delay(500)
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer(
                alpha = alphaAnim,
                scaleX = scaleAnim,
                scaleY = scaleAnim
            )
        ) {
            Surface(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.remed_logo),
                        contentDescription = "ReMed Logo",
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ReMed",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
