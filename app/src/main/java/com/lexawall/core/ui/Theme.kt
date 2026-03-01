package com.lexawall.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.lexawall.data.local.PrefsManager
import com.lexawall.wallpaper.LexaTheme
import com.lexawall.wallpaper.LexaThemes

private fun darkColorSchemeForTheme() = darkColorScheme(
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
val LocalLexaTheme = staticCompositionLocalOf { LexaThemes.Midnight }
val LocalLexaThemeState = staticCompositionLocalOf<MutableState<LexaTheme>> {
    error("LocalLexaThemeState not provided")
}

@Composable
fun LexaWallTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activeThemeState = remember {
        mutableStateOf(LexaThemes.fromKey(PrefsManager.getActiveTheme(context)))
    }
    LexaColors.applyTheme(activeThemeState.value)

    CompositionLocalProvider(
        LocalLexaColors provides LexaColors,
        LocalLexaTheme provides activeThemeState.value,
        LocalLexaThemeState provides activeThemeState
    ) {
        MaterialTheme(
            colorScheme = darkColorSchemeForTheme(),
            typography = LexaTypography,
            content = {
                key(activeThemeState.value.key) {
                    content()
                }
            }
        )
    }
}
