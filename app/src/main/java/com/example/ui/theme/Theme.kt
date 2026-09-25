package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AppleMusicRed,
    onPrimary = AppleTextPrimary,
    primaryContainer = AppleMusicRedDark,
    onPrimaryContainer = AppleTextPrimary,
    secondary = AppleMusicPink,
    onSecondary = AppleTextPrimary,
    tertiary = AppleMusicHiResGold,
    onTertiary = AppleBlack,
    background = AppleBackgroundDark,
    onBackground = AppleTextPrimary,
    surface = AppleSurfaceDark,
    onSurface = AppleTextPrimary,
    surfaceVariant = AppleCardDark,
    onSurfaceVariant = AppleTextSecondary,
    outline = AppleBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = AppleMusicRed,
    onPrimary = AppleTextPrimary,
    primaryContainer = AppleMusicPink,
    onPrimaryContainer = AppleTextPrimary,
    secondary = AppleMusicPurple,
    onSecondary = AppleTextPrimary,
    tertiary = AppleMusicHiResAmber,
    onTertiary = AppleBlack,
    background = AppleBackgroundLight,
    onBackground = AppleTextPrimaryLight,
    surface = AppleSurfaceLight,
    onSurface = AppleTextPrimaryLight,
    surfaceVariant = AppleBorderLight,
    onSurfaceVariant = AppleTextSecondaryLight,
    outline = AppleBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek Apple Music Dark Mode
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
