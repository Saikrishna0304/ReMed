package com.example.remed.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppTheme {
    LIGHT,
    DARK,
    BLACK,
    SOLARIZED_LIGHT
}

object ThemeManager {
    private const val PREFS_NAME = "remed_theme_prefs"
    private const val KEY_THEME = "app_theme"

    var currentTheme by mutableStateOf(AppTheme.LIGHT)
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedThemeName = prefs.getString(KEY_THEME, AppTheme.LIGHT.name)
        currentTheme = try {
            AppTheme.valueOf(savedThemeName ?: AppTheme.LIGHT.name)
        } catch (e: Exception) {
            AppTheme.LIGHT
        }
    }

    fun setTheme(context: Context, theme: AppTheme) {
        currentTheme = theme
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }
}
