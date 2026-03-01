package com.lexawall.core.ui

import androidx.compose.ui.graphics.Color
import com.lexawall.wallpaper.LexaTheme
import com.lexawall.wallpaper.LexaThemes

object LexaColors {
    private var activeTheme: LexaTheme = LexaThemes.Midnight

    fun applyTheme(theme: LexaTheme) {
        activeTheme = theme
    }

    val Primary: Color get() = Color(activeTheme.accentColor)
    val BackgroundDark: Color get() = Color(activeTheme.backgroundTop)
    val SplashBg: Color get() = Color(activeTheme.backgroundTop)
    val SurfaceDark: Color get() = Color(activeTheme.surfaceColor)
    val SurfaceHighlight: Color get() = Color(activeTheme.accentColorDim).copy(alpha = 0.25f)
    val CardDark: Color get() = Color(activeTheme.surfaceColor)
    val Success = Color(0xFF64DC82)
    val ErrorRed = Color(0xFF666680)
    val TextPrimary = Color(0xFFE8E8E8)
    val TextSecondary = Color(0xFFB8B2A3)
    val TextMuted = Color(0xFF666680)
    val BrandGold: Color get() = Color(activeTheme.accentColor)
    val WhiteAlpha10 = Color(0x1AFFFFFF)

    val SuccessDark = Color(0xFF1B3A25)
    val ErrorDark = Color(0xFF3A1B1B)
    val PrimaryDark = Color(0xFF2A2310)
    val Transparent = Color.Transparent
    val White = Color.White
    val Black = Color.Black

    val MidnightGradientTop = Color(0xFF0D0D1A)
    val MidnightGradientBottom = Color(0xFF1A1A2E)
    val SavannaGradientTop = Color(0xFF2A1F0A)
    val SavannaGradientBottom = Color(0xFF3D2E14)
    val LagoonGradientTop = Color(0xFF0A1F2A)
    val LagoonGradientBottom = Color(0xFF143D3D)
    val HarmattanGradientTop = Color(0xFF1A1510)
    val HarmattanGradientBottom = Color(0xFF2E2A1A)
}
