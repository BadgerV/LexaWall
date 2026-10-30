package com.lexawall.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val DarkColorScheme = darkColorScheme(
    primary = LexaColors.Primary,
    onPrimary = LexaColors.BackgroundDark,
    secondary = LexaColors.BrandGold,
    onSecondary = LexaColors.BackgroundDark,
    background = LexaColors.BackgroundDark,
    onBackground = LexaColors.TextPrimary,
    surface = LexaColors.SurfaceDark,
    onSurface = LexaColors.TextPrimary,
    surfaceVariant = LexaColors.SurfaceHighlight,
    onSurfaceVariant = LexaColors.TextSecondary,
    error = LexaColors.ErrorRed,
    onError = LexaColors.White,
    outline = LexaColors.WhiteAlpha10,
)

val LocalLexaColors = staticCompositionLocalOf { LexaColors }

@Composable
fun LexaWallTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLexaColors provides LexaColors) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = LexaTypography,
            content = content
        )
    }
}

