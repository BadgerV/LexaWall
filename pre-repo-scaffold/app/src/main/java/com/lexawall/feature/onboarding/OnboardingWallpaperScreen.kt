package com.lexawall.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.GoldDivider
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LexaWallTheme
import com.lexawall.core.ui.DmMono
import com.lexawall.core.ui.PlayfairDisplay

@Composable
fun OnboardingWallpaperScreen(
    onSetWallpaper: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF181510),
                        Color(0xFF0D0B08)
                    )
                )
            )
    ) {
        // Wallpaper content centred
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Part of speech
            Text(
                text = "ADJECTIVE",
                style = LexaType.overline.copy(
                    fontFamily = DmMono,
                    letterSpacing = 2.sp
                ),
                color = LexaColors.Primary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))

            // Word
            Text(
                text = "ephemeral",
                style = LexaType.wordDisplay.copy(
                    fontFamily = PlayfairDisplay,
                    fontStyle = FontStyle.Italic,
                    fontSize = 48.sp
                ),
                color = LexaColors.BrandGold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            // Gold divider
            GoldDivider()
            Spacer(Modifier.height(24.dp))

            // Definition
            Text(
                text = "lasting only a short time",
                style = LexaType.bodyLarge.copy(fontSize = 18.sp),
                color = Color(0xFFD4B870),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))

            // Example
            Text(
                text = "\"fashions are ephemeral, changing with every season\"",
                style = LexaType.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = Color(0xFFD4B870).copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }

        // Sticky bottom footer
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            LexaColors.BackgroundDark.copy(alpha = 0.9f),
                            LexaColors.BackgroundDark
                        )
                    )
                )
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp, top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryButton(
                text = "Set as my wallpaper",
                onClick = onSetWallpaper
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Takes ~10 seconds.",
                style = LexaType.bodySmall,
                color = LexaColors.TextSecondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingWallpaperScreenPreview() {
    LexaWallTheme {
        OnboardingWallpaperScreen(onSetWallpaper = {})
    }
}

