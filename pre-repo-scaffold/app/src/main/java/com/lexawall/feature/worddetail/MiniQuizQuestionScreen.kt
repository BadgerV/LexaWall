package com.lexawall.feature.worddetail

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.QuizOptionRow
import com.lexawall.core.ui.DmMono
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LexaWallTheme
import com.lexawall.core.ui.PlayfairDisplay
import com.lexawall.data.fake.FakeWordRepository
import com.lexawall.domain.model.QuizOptionState

@Composable
fun MiniQuizQuestionScreen(
    wordId: String,
    onAnswer: (isCorrect: Boolean) -> Unit
) {
    val word = FakeWordRepository().getWordById(wordId)
    val options = listOf(
        word.definition,
        "Extremely large or great in scale.",
        "Happening at the same time."
    )
    val correctIndex = 0
    var selectedOption by remember { mutableIntStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexaColors.BackgroundDark)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(80.dp))

        // Word
        Text(
            text = word.text,
            style = LexaType.wordDisplay.copy(
                fontFamily = PlayfairDisplay,
                fontStyle = FontStyle.Italic,
                fontSize = 40.sp
            ),
            color = LexaColors.TextPrimary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))

        // "— Meaning? —"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "—",
                style = LexaType.bodyLarge,
                color = LexaColors.Primary
            )
            Text(
                text = "  Meaning?  ",
                style = LexaType.bodyLarge,
                color = LexaColors.TextSecondary
            )
            Text(
                text = "—",
                style = LexaType.bodyLarge,
                color = LexaColors.Primary
            )
        }
        Spacer(Modifier.height(40.dp))

        // Options
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEachIndexed { index, optionText ->
                val state = when {
                    selectedOption == -1 -> QuizOptionState.Default
                    index == selectedOption && index == correctIndex -> QuizOptionState.Correct
                    index == selectedOption -> QuizOptionState.Wrong
                    else -> QuizOptionState.Default
                }
                QuizOptionRow(
                    text = optionText,
                    state = state,
                    onClick = {
                        if (selectedOption == -1) {
                            selectedOption = index
                            onAnswer(index == correctIndex)
                        }
                    }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Hint
        Text(
            text = "Doesn't affect streak.",
            style = LexaType.monoSmall.copy(fontFamily = DmMono),
            color = LexaColors.TextMuted,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { 0.5f },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = LexaColors.Primary,
            trackColor = LexaColors.SurfaceHighlight,
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun MiniQuizQuestionScreenPreview() {
    LexaWallTheme {
        MiniQuizQuestionScreen(wordId = "ephemeral", onAnswer = {})
    }
}

