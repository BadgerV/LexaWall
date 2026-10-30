package com.lexawall.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lexawall.core.components.GhostButton
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.components.TopBarRow
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LexaWallTheme

@Composable
fun OnboardingSetupScreen(
    onBack: () -> Unit,
    onOpenPicker: () -> Unit,
    onNotNow: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexaColors.BackgroundDark)
    ) {
        TopBarRow(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(LexaColors.SurfaceDark)
            ) {
                // Hero area (dark gradient placeholder)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    LexaColors.SurfaceHighlight,
                                    LexaColors.SurfaceDark
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Quick setup",
                        style = LexaType.headlineSmall,
                        color = LexaColors.TextPrimary
                    )
                    Spacer(Modifier.height(20.dp))

                    // Step 1
                    SetupStep(
                        number = 1,
                        text = "Tap Set wallpaper"
                    )
                    Spacer(Modifier.height(16.dp))

                    // Step 2
                    SetupStep(
                        number = 2,
                        text = "Choose Home screen or Both"
                    )
                    Spacer(Modifier.height(28.dp))

                    PrimaryButton(
                        text = "Open picker",
                        onClick = onOpenPicker
                    )
                    Spacer(Modifier.height(8.dp))
                    GhostButton(
                        text = "Not now",
                        onClick = onNotNow
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupStep(
    number: Int,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gold circle number badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(LexaColors.Primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$number",
                style = LexaType.labelMedium,
                color = LexaColors.BackgroundDark,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = LexaType.bodyLarge,
            color = LexaColors.TextPrimary
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingSetupScreenPreview() {
    LexaWallTheme {
        OnboardingSetupScreen(onBack = {}, onOpenPicker = {}, onNotNow = {})
    }
}

