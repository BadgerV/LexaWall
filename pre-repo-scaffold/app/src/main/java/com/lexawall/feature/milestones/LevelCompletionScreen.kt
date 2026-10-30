package com.lexawall.feature.milestones

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LexaWallTheme
import com.lexawall.core.ui.PlayfairDisplay

@Composable
fun LevelCompletionScreen(
    onContinue: () -> Unit
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
            // Decorative icon circle with gold glow
            Box(contentAlignment = Alignment.Center) {
                // Glow
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(LexaColors.Primary.copy(alpha = 0.12f))
                )
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(LexaColors.SurfaceDark)
                        .border(2.dp, LexaColors.Primary.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = LexaColors.Primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Spacer(Modifier.height(32.dp))

            // "You mastered B2." with B2 in gold
            Text(
                text = buildAnnotatedString {
                    append("You mastered ")
                    withStyle(SpanStyle(color = LexaColors.Primary)) {
                        append("B2")
                    }
                    append(".")
                },
                style = LexaType.headlineLarge.copy(fontFamily = PlayfairDisplay),
                color = LexaColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = buildAnnotatedString {
                    append("You're ")
                    withStyle(SpanStyle(color = LexaColors.TextPrimary)) {
                        append("C1")
                    }
                    append(" now.")
                },
                style = LexaType.titleLarge,
                color = LexaColors.TextMuted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(40.dp))

            // Two stat tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatTile(
                    value = "2,450",
                    label = "Words",
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = "98%",
                    label = "Accuracy",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Bottom button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            PrimaryButton(
                text = "Continue",
                onClick = onContinue,
                showArrow = true
            )
        }
    }
}

@Composable
private fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(LexaColors.WhiteAlpha10)
            .border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = LexaType.titleLarge,
            color = LexaColors.Primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label.uppercase(),
            style = LexaType.overlineSmall,
            color = LexaColors.TextMuted
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LevelCompletionScreenPreview() {
    LexaWallTheme {
        LevelCompletionScreen(onContinue = {})
    }
}

