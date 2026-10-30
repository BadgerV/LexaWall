package com.lexawall.feature.flashcards

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lexawall.core.components.GhostButton
import com.lexawall.core.components.OverlineTag
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.ui.DmMono
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LexaWallTheme

@Composable
fun FlashcardsSessionSummaryScreen(
    onStartQuiz: () -> Unit,
    onBackToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexaColors.BackgroundDark),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Overline
            OverlineTag(text = "STACK COMPLETE")
            Spacer(Modifier.height(24.dp))

            // Headline
            Text(
                text = "11 felt familiar",
                style = LexaType.headlineLarge,
                color = LexaColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(48.dp))

            // Stats
            Column(
                modifier = Modifier.fillMaxWidth(0.75f),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Knew it
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(LexaColors.Success.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check, null,
                                tint = LexaColors.Success,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "Knew it",
                            style = LexaType.bodyLarge,
                            color = LexaColors.TextSecondary
                        )
                    }
                    Text(
                        text = "11",
                        style = LexaType.mono,
                        color = LexaColors.TextPrimary
                    )
                }

                HorizontalDivider(color = LexaColors.SurfaceHighlight.copy(alpha = 0.5f))

                // Still learning
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFC107).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.School, null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "Still learning",
                            style = LexaType.bodyLarge,
                            color = LexaColors.TextSecondary
                        )
                    }
                    Text(
                        text = "4",
                        style = LexaType.mono,
                        color = LexaColors.TextPrimary
                    )
                }

                HorizontalDivider(color = LexaColors.SurfaceHighlight.copy(alpha = 0.5f))
            }
        }

        // Bottom buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PrimaryButton(
                text = "Start today's quiz",
                onClick = onStartQuiz,
                showArrow = true
            )
            GhostButton(
                text = "Back to home",
                onClick = onBackToHome
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FlashcardsSessionSummaryScreenPreview() {
    LexaWallTheme {
        FlashcardsSessionSummaryScreen(onStartQuiz = {}, onBackToHome = {})
    }
}

