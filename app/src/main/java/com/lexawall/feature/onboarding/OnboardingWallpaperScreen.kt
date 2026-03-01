package com.lexawall.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.*
import com.lexawall.core.ui.*
import com.lexawall.core.ui.components.LexaMascotAnimated

@Composable
fun OnboardingWallpaperScreen(onSetWallpaper: () -> Unit, onSkip: () -> Unit = {}) {
    Box(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(Brush.verticalGradient(listOf(Color(0xFF181510), Color(0xFF0D0B08))))
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            LexaMascotAnimated(size = 48.dp)
            Spacer(Modifier.height(22.dp))
            Text(
                "I'll live on your wallpaper.",
                style = LexaType.wordDisplay.copy(
                    fontFamily = PlayfairDisplay,
                    fontStyle = FontStyle.Italic,
                    fontSize = 44.sp
                ),
                color = LexaColors.BrandGold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Every day, one word. You'll see it every time you unlock your phone.",
                style = LexaType.bodyMedium.copy(fontSize = 15.sp),
                color = Color(0xFFAAA49A),
                textAlign = TextAlign.Center
            )
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, LexaColors.BackgroundDark.copy(alpha = 0.9f), LexaColors.BackgroundDark))).padding(horizontal = 24.dp).padding(bottom = 40.dp, top = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            PrimaryButton("Let's do this →", onSetWallpaper); Spacer(Modifier.height(12.dp))
            Text("Takes ~10 seconds.", style = LexaType.bodySmall, color = LexaColors.TextSecondary.copy(alpha = 0.7f), textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Text(
                "I'll learn without the wallpaper",
                style = LexaType.bodySmall.copy(fontSize = 13.sp),
                color = LexaColors.TextSecondary.copy(alpha = 0.35f),
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable(onClick = onSkip)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { OnboardingWallpaperScreen(onSetWallpaper = {}) } }
