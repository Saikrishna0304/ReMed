package com.example.remed.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. Light Color Scheme - Minimalist Greenish-Blueish (Teal/Cyan-Green)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0D9488),           // Vibrant Teal/Cyan-Green
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),  // Soft Seafoam
    onPrimaryContainer = Color(0xFF115E59),
    secondary = Color(0xFF0284C7),         // Ocean Blueish Accent
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF075985),
    background = Color(0xFFF4F9F8),        // Ultra Clean Minimalist Background
    onBackground = Color(0xFF111918),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111918),
    surfaceVariant = Color(0xFFE2ECEB),
    onSurfaceVariant = Color(0xFF3F4E4D)
)

// 2. Dark Color Scheme (Standard Dark Mode - Deep Teal Slate)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2DD4BF),           // Luminous Teal Mint
    onPrimary = Color(0xFF003734),
    primaryContainer = Color(0xFF134E4A),
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = Color(0xFF38BDF8),         // Soft Cyan-Blue Accent
    onSecondary = Color(0xFF003548),
    secondaryContainer = Color(0xFF004D68),
    onSecondaryContainer = Color(0xFFE0F2FE),
    background = Color(0xFF111818),        // Deep Subtle Teal Slate
    onBackground = Color(0xFFE1E9E8),
    surface = Color(0xFF182222),
    onSurface = Color(0xFFE1E9E8),
    surfaceVariant = Color(0xFF243232),
    onSurfaceVariant = Color(0xFFBFCFCE)
)

// 3. Dark Black / AMOLED Color Scheme (True Pitch Black with Teal Mint Accents)
private val BlackColorScheme = darkColorScheme(
    primary = Color(0xFF2DD4BF),           // Luminous Teal Mint
    onPrimary = Color(0xFF003734),
    primaryContainer = Color(0xFF0F3A37),
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF003548),
    secondaryContainer = Color(0xFF162428),
    onSecondaryContainer = Color(0xFFE0F2FE),
    background = Color(0xFF000000),      // Pure Pitch Black
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),         // Pure Pitch Black for AMOLED
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF121B1A),  // Subtle dark teal tint for elevated cards
    onSurfaceVariant = Color(0xFFD6E2E1)
)

// 4. Solarized Light Color Scheme (Warm Cream & Solarized Cyan/Teal)
private val SolarizedLightColorScheme = lightColorScheme(
    primary = Color(0xFF2AA198),           // Solarized Cyan/Teal
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEE8D5),  // Solarized Base2 (Soft Cream Surface)
    onPrimaryContainer = Color(0xFF073642),// Solarized Base03 (Dark Teal Slate)
    secondary = Color(0xFF268BD2),         // Solarized Blue
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF075985),
    background = Color(0xFFFDF6E3),        // Solarized Base3 (Warm Cream Background)
    onBackground = Color(0xFF586E75),      // Solarized Base01
    surface = Color(0xFFFDF6E3),           // Warm Cream Surface
    onSurface = Color(0xFF073642),
    surfaceVariant = Color(0xFFEEE8D5),    // Base2 variant
    onSurfaceVariant = Color(0xFF657B83)   // Base00
)

@Composable
fun ReMedTheme(
    appTheme: AppTheme = ThemeManager.currentTheme,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.DARK -> DarkColorScheme
        AppTheme.BLACK -> BlackColorScheme
        AppTheme.SOLARIZED_LIGHT -> SolarizedLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            val isLight = appTheme == AppTheme.LIGHT || appTheme == AppTheme.SOLARIZED_LIGHT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLight
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
