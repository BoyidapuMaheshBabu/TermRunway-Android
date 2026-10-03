package com.termrunway.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// TermRunway brand palette derived from the supplied logo:
// deep navy road + electric blue growth bars + teal/green direction.
val RunwayNavy = Color(0xFF082A5C)
val RunwayNavy2 = Color(0xFF0D3975)
val RunwayBlue = Color(0xFF2878F0)
val RunwayMint = Color(0xFF20CF7A)
val RunwayCyan = Color(0xFF10A8B8)
val RunwayRed = Color(0xFFFF6673)
val RunwayAmber = Color(0xFFFFC857)
val RunwayText = Color(0xFFEAF4FF)
val RunwayMuted = Color(0xFF91A7C6)

private val LightColors = lightColorScheme(
    primary = Color(0xFF136BEA),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEBFF),
    onPrimaryContainer = Color(0xFF082A5C),
    secondary = Color(0xFF0B9F9E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7F4F0),
    onSecondaryContainer = Color(0xFF053A3A),
    tertiary = Color(0xFF17A966),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD3F5E3),
    onTertiaryContainer = Color(0xFF053B20),
    background = Color(0xFFF4F8FF),
    surface = Color.White,
    surfaceVariant = Color(0xFFE7EEF8),
    onBackground = Color(0xFF12213A),
    onSurface = Color(0xFF12213A),
    error = Color(0xFFD83A4B),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = RunwayBlue,
    onPrimary = Color(0xFF001B3A),
    primaryContainer = Color(0xFF164A8B),
    onPrimaryContainer = Color(0xFFDCEBFF),
    secondary = RunwayCyan,
    onSecondary = Color(0xFF002124),
    secondaryContainer = Color(0xFF075A63),
    onSecondaryContainer = Color(0xFFD7F8FA),
    tertiary = RunwayMint,
    onTertiary = Color(0xFF00391D),
    tertiaryContainer = Color(0xFF12663F),
    onTertiaryContainer = Color(0xFFD3F5E3),
    background = RunwayNavy,
    surface = RunwayNavy2,
    surfaceVariant = Color(0xFF17457E),
    onBackground = RunwayText,
    onSurface = RunwayText,
    error = RunwayRed,
    onError = Color(0xFF4A0710)
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
