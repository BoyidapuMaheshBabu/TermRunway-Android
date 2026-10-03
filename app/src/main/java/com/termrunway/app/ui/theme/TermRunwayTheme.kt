package com.termrunway.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val RunwayNavy = Color(0xFF0B1020)
val RunwayNavy2 = Color(0xFF11182D)
val RunwayMint = Color(0xFF5EE7B2)
val RunwayBlue = Color(0xFF8BB7FF)
val RunwayRed = Color(0xFFFF8B8B)
val RunwayAmber = Color(0xFFFFCC72)
val RunwayText = Color(0xFFEAF1FF)
val RunwayMuted = Color(0xFF9BA8C3)

private val LightColors = lightColorScheme(
    primary = Color(0xFF116A50),
    onPrimary = Color.White,
    secondary = Color(0xFF315C8F),
    tertiary = Color(0xFF725B15),
    background = Color(0xFFF7F9FC),
    surface = Color.White,
    surfaceVariant = Color(0xFFE8EDF5),
    onBackground = Color(0xFF182033),
    onSurface = Color(0xFF182033)
)

private val DarkColors = darkColorScheme(
    primary = RunwayMint,
    onPrimary = RunwayNavy,
    secondary = RunwayBlue,
    tertiary = RunwayAmber,
    background = RunwayNavy,
    surface = RunwayNavy2,
    surfaceVariant = Color(0xFF1A2440),
    onBackground = RunwayText,
    onSurface = RunwayText,
    error = RunwayRed
)

@androidx.compose.runtime.Composable
fun TermRunwayTheme(
    darkTheme: Boolean,
    content: @androidx.compose.runtime.Composable () -> Unit
) {
    androidx.compose.material3.MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = androidx.compose.material3.Typography(),
        shapes = androidx.compose.material3.Shapes(),
        content = content
    )
}
