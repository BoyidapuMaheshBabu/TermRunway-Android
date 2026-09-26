package com.termrunway.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BluePrimaryLight,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = BlueContainerLight,
    onPrimaryContainer = Ink,
    secondary = GreenSuccess,
    background = CanvasLight,
    onBackground = Ink,
    surface = SurfaceLight,
    onSurface = Ink,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Muted,
    outline = OutlineLight,
    error = RedWarning
)

private val DarkColors = darkColorScheme(
    primary = BluePrimary,
    onPrimary = Ink,
    primaryContainer = BlueContainer,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
    secondary = GreenSuccess,
    onSecondary = Ink,
    secondaryContainer = GreenSuccessContainer,
    onSecondaryContainer = androidx.compose.ui.graphics.Color.White,
    background = CanvasDark,
    onBackground = androidx.compose.ui.graphics.Color.White,
    surface = SurfaceDark,
    onSurface = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFA6B8CA),
    outline = OutlineDark,
    error = RedWarning,
    errorContainer = RedWarningContainer,
    onErrorContainer = androidx.compose.ui.graphics.Color.White
)

@Composable
fun TermRunwayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = TermRunwayTypography,
        content = content
    )
}
