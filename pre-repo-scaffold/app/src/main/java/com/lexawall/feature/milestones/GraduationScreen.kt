package com.lexawall.feature.milestones

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.lexawall.core.ui.PlayfairDisplay

@Composable
fun GraduationScreen(
    onKeepGoing: () -> Unit
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
            // School icon in gold circle
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(LexaColors.Primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    tint = LexaColors.Primary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(Modifier.height(32.dp))

            // Word
            Text(
                text = "ephemeral",
                style = LexaType.wordDisplay.copy(
                    fontFamily = PlayfairDisplay,
                    fontStyle = FontStyle.Italic,
                    fontSize = 44.sp
                ),
                color = LexaColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            GoldDivider()
            Spacer(Modifier.height(24.dp))

            Text(
                text = "You've made this word yours.",
                style = LexaType.headlineSmall,
                color = LexaColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "It's leaving your rotation.",
                style = LexaType.bodyLarge,
                color = LexaColors.TextMuted,
                textAlign = TextAlign.Center
            )
        }

        // Bottom button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            PrimaryButton(
                text = "Keep going",
                onClick = onKeepGoing,
                showArrow = true
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun GraduationScreenPreview() {
    LexaWallTheme {
        GraduationScreen(onKeepGoing = {})
    }
}

