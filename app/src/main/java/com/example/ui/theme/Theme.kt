package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SamsungCyan,
    onPrimary = RemoteDarkBg,
    primaryContainer = SamsungBlue,
    onPrimaryContainer = TextPrimary,
    secondary = SamsungBlueLight,
    onSecondary = RemoteDarkBg,
    tertiary = SamsungYellow,
    background = RemoteDarkBg,
    onBackground = TextPrimary,
    surface = RemoteSurface,
    onSurface = TextPrimary,
    surfaceVariant = RemoteSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = RemoteBorder,
    error = SamsungPowerRed,
    onError = TextPrimary
)

@Composable
fun SamRemoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = RemoteDarkBg.toArgb()
                window.navigationBarColor = RemoteDarkBg.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
