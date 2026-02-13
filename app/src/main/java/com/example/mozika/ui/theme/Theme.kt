package com.example.mozika.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ==================== COULEURS SONIC FLOW ====================
val CyanPrimary = Color(0xFF22D3EE)
val CyanAlpha15 = Color(0xFF22D3EE).copy(alpha = 0.15f)
val CyanAlpha20 = Color(0xFF22D3EE).copy(alpha = 0.20f)
val CyanAlpha12 = Color(0xFF22D3EE).copy(alpha = 0.12f)
val BackgroundBlack = Color(0xFF000000)
val CardBlack = Color(0xFF141414)
val SurfaceBlack = Color(0xFF0A0A0A)
val TextGray = Color(0xFF888888)
val TextGrayLight = Color(0xFF666666)
// ============================================================

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimary,        // Utiliser Cyan au lieu de Purple80
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = BackgroundBlack,
    surface = CardBlack,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = CyanPrimary,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun SonicFlowTheme(
    darkTheme: Boolean = true, // Par défaut dark theme pour l'app musique
    dynamicColor: Boolean = false, // Désactiver les couleurs dynamiques pour garder le cyan
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}