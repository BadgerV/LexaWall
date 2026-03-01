package com.lexawall.core.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val PlayfairDisplay = FontFamily.Serif
val DmSans = FontFamily.SansSerif
val DmMono = FontFamily.Monospace

object LexaType {

    val displayLarge = TextStyle(
        fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic, fontSize = 48.sp,
        lineHeight = 56.sp, letterSpacing = (-0.5).sp
    )
    val displayMedium = TextStyle(
        fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic, fontSize = 40.sp,
        lineHeight = 48.sp, letterSpacing = (-0.25).sp
    )
    val displaySmall = TextStyle(
        fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold,
        fontSize = 36.sp, lineHeight = 44.sp,
    )
    val headlineLarge = TextStyle(
        fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 40.sp,
    )
    val headlineMedium = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 36.sp,
    )
    val headlineSmall = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 32.sp,
    )
    val titleLarge = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp,
    )
    val titleMedium = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 24.sp,
    )
    val titleSmall = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp,
    )
    val bodyLarge = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp,
    )
    val bodyMedium = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp,
    )
    val bodySmall = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp,
    )
    val labelLarge = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.Bold,
        fontSize = 16.sp, lineHeight = 24.sp,
    )
    val labelMedium = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp,
    )
    val labelSmall = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp,
    )
    val overline = TextStyle(
        fontFamily = DmMono, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 2.sp,
    )
    val overlineSmall = TextStyle(
        fontFamily = DmMono, fontWeight = FontWeight.Normal,
        fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 1.5.sp,
    )
    val mono = TextStyle(
        fontFamily = DmMono, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp,
    )
    val monoSmall = TextStyle(
        fontFamily = DmMono, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp,
    )
    val wordDisplay = TextStyle(
        fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic, fontSize = 44.sp,
        lineHeight = 52.sp, letterSpacing = (-0.5).sp,
    )
    val wordMedium = TextStyle(
        fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic, fontSize = 36.sp, lineHeight = 44.sp,
    )
    val scoreDisplay = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.ExtraBold,
        fontSize = 72.sp, lineHeight = 80.sp, letterSpacing = (-2).sp,
    )
}

val LexaTypography = Typography(
    displayLarge = LexaType.displayLarge,
    displayMedium = LexaType.displayMedium,
    displaySmall = LexaType.displaySmall,
    headlineLarge = LexaType.headlineLarge,
    headlineMedium = LexaType.headlineMedium,
    headlineSmall = LexaType.headlineSmall,
    titleLarge = LexaType.titleLarge,
    titleMedium = LexaType.titleMedium,
    titleSmall = LexaType.titleSmall,
    bodyLarge = LexaType.bodyLarge,
    bodyMedium = LexaType.bodyMedium,
    bodySmall = LexaType.bodySmall,
    labelLarge = LexaType.labelLarge,
    labelMedium = LexaType.labelMedium,
    labelSmall = LexaType.labelSmall,
)
