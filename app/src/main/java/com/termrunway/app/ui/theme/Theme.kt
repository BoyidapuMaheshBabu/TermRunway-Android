package com.termrunway.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = RunwayGreen,
    onPrimary = Color.White,
    primaryContainer = RunwayGreenContainer,
    onPrimaryContainer = RunwayText,
    secondary = RunwayMuted,
    background = RunwayBackground,
    surface = RunwaySurface,
    onBackground = RunwayText,
    onSurface = RunwayText,
    error = RunwayError,
    errorContainer = RunwayErrorContainer
)

private val DarkColors = darkColorScheme(
    primary = RunwayGreenDark,
    onPrimary = Color(0xFF123722),
    primaryContainer = RunwayGreenContainerDark,
    onPrimaryContainer = Color(0xFFD5F3DF),
    background = Color(0xFF101511),
    surface = Color(0xFF151B16),
    onBackground = Color(0xFFE2E9E2),
    onSurface = Color(0xFFE2E9E2),
    error = Color(0xFFFFB4AB)
)

@Composable
fun TermRunwayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
