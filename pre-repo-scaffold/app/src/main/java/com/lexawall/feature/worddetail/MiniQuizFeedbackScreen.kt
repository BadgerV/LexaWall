package com.lexawall.feature.worddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.lexawall.core.components.GhostButton
import com.lexawall.core.components.GlowCircle
import com.lexawall.core.components.GoldDivider
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LexaWallTheme
import com.lexawall.data.fake.FakeWordRepository

@Composable
fun MiniQuizFeedbackScreen(
    wordId: String,
    isCorrect: Boolean,
    onBackToWord: () -> Unit
) {
    val word = FakeWordRepository().getWordById(wordId)
    val color = if (isCorrect) LexaColors.Success else LexaColors.ErrorRed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexaColors.BackgroundDark)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(0.3f))

        // Circle icon
        GlowCircle(
            icon = {
                Icon(
                    if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(36.dp)
                )
            },
            glowColor = color
        )
        Spacer(Modifier.height(24.dp))

        // Headline
        Text(
            text = if (isCorrect) "That's it!" else "Not quite.",
            style = LexaType.headlineLarge,
            color = LexaColors.TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        GoldDivider()
        Spacer(Modifier.height(24.dp))

        // Word + definition card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LexaColors.SurfaceDark)
                .padding(20.dp)
        ) {
            Text(
                text = word.text,
                style = LexaType.titleLarge,
                color = LexaColors.Primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = word.definition,
                style = LexaType.bodyLarge,
                color = LexaColors.TextSecondary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "\"${word.exampleSentence}\"",
                style = LexaType.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = LexaColors.TextPrimary
            )
        }

        Spacer(Modifier.weight(0.5f))

        // Ghost link button
        GhostButton(
            text = "Back to word →",
            onClick = onBackToWord
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun MiniQuizFeedbackCorrectPreview() {
    LexaWallTheme {
        MiniQuizFeedbackScreen("ephemeral", true, {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun MiniQuizFeedbackWrongPreview() {
    LexaWallTheme {
        MiniQuizFeedbackScreen("ephemeral", false, {})
    }
}

