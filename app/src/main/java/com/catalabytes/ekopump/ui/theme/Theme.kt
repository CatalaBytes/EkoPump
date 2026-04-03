package com.catalabytes.ekopump.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary        = EkoGreen80,
    secondary      = EkoGreenGrey80,
    tertiary       = EkoAmber80,
    background     = SurfaceDark,
    surface        = SurfaceDark,
    onPrimary      = Color(0xFF003909),
    onBackground   = Color(0xFFE1E3DE),
    onSurface      = Color(0xFFE1E3DE)
)

private val LightColorScheme = lightColorScheme(
    primary        = EkoGreen40,
    secondary      = EkoGreenGrey40,
    tertiary       = EkoAmber40,
    background     = SurfaceLight,
    surface        = SurfaceLight,
    onPrimary      = Color(0xFFFFFFFF),
    onBackground   = Color(0xFF1A1C1A),
    onSurface      = Color(0xFF1A1C1A)
)

@Composable
fun EkoPumpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
