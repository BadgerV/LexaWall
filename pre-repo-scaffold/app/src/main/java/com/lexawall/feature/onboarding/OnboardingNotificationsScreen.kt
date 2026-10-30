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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lexawall.core.components.GhostButton
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LexaWallTheme

@Composable
fun OnboardingNotificationsScreen(
    onAllow: () -> Unit,
    onNoThanks: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexaColors.BackgroundDark),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(LexaColors.SurfaceDark)
        ) {
            // Hero area with gradient + bell icon badge
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
            ) {
                // Bell icon badge top-right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LexaColors.Primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = LexaColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Remind you about the daily quiz?",
                    style = LexaType.headlineSmall,
                    color = LexaColors.TextPrimary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "One notification a day. Optional.",
                    style = LexaType.bodyMedium,
                    color = LexaColors.TextSecondary
                )
                Spacer(Modifier.height(28.dp))
                PrimaryButton(
                    text = "Allow reminders",
                    onClick = onAllow
                )
                Spacer(Modifier.height(8.dp))
                GhostButton(
                    text = "No thanks",
                    onClick = onNoThanks
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingNotificationsScreenPreview() {
    LexaWallTheme {
        OnboardingNotificationsScreen(onAllow = {}, onNoThanks = {})
    }
}

