package com.lexawall.feature.quiz

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lexawall.core.components.GlowCircle
import com.lexawall.core.components.GoldDivider
import com.lexawall.core.components.PillBadge
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LexaWallTheme

@Composable
fun QuizCorrectScreen(
    questionIndex: Int,
    onContinue: (nextIndex: Int, isLast: Boolean) -> Unit,
    quizViewModel: QuizViewModel = viewModel()
) {
    val question = quizViewModel.getQuestion(questionIndex) ?: return
    val isLast = quizViewModel.isLastQuestion(questionIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexaColors.BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            // Green glow circle with check
            GlowCircle(
                icon = {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = LexaColors.Success,
                        modifier = Modifier.size(36.dp)
                    )
                },
                glowColor = LexaColors.Success
            )
            Spacer(Modifier.height(24.dp))

            Text(
                text = "That's it.",
                style = LexaType.headlineLarge,
                color = LexaColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            GoldDivider()
            Spacer(Modifier.height(24.dp))

            // Word card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(LexaColors.SurfaceDark)
                    .padding(20.dp)
            ) {
                Text(
                    text = question.word.text,
                    style = LexaType.titleLarge,
                    color = LexaColors.Primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = question.word.definition,
                    style = LexaType.bodyLarge,
                    color = LexaColors.TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "\"${question.word.exampleSentence}\"",
                    style = LexaType.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = LexaColors.TextPrimary
                )
            }
            Spacer(Modifier.height(16.dp))

            PillBadge(
                text = "Next review in 4 days",
                textColor = LexaColors.Success,
                backgroundColor = LexaColors.Success.copy(alpha = 0.15f)
            )
            Spacer(Modifier.height(32.dp))
        }

        // Sticky footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            PrimaryButton(
                text = "Continue",
                onClick = { onContinue(questionIndex + 1, isLast) },
                showArrow = true
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun QuizCorrectScreenPreview() {
    LexaWallTheme {
        QuizCorrectScreen(questionIndex = 0, onContinue = { _, _ -> })
    }
}

