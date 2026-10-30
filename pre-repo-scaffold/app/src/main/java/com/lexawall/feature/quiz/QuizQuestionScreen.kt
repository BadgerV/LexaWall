package com.lexawall.feature.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lexawall.core.components.OverlineTag
import com.lexawall.core.components.ProgressDotsRow
import com.lexawall.core.components.QuizOptionRow
import com.lexawall.core.components.SmallTag
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LexaWallTheme
import com.lexawall.core.ui.PlayfairDisplay
import com.lexawall.domain.model.QuizOptionState

@Composable
fun QuizQuestionScreen(
    questionIndex: Int,
    onCorrect: () -> Unit,
    onWrong: () -> Unit,
    quizViewModel: QuizViewModel = viewModel()
) {
    val uiState by quizViewModel.uiState.collectAsState()
    val question = quizViewModel.getQuestion(questionIndex) ?: return
    val total = quizViewModel.getTotalQuestions()

    var selectedOption by remember(questionIndex) { mutableIntStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexaColors.BackgroundDark)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(48.dp))

        // Progress dots
        ProgressDotsRow(
            total = total,
            current = questionIndex,
            answered = questionIndex
        )
        Spacer(Modifier.height(24.dp))

        // Overline
        Text(
            text = "What does this word mean?",
            style = LexaType.overline,
            color = LexaColors.TextSecondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        // Word
        Text(
            text = question.word.text,
            style = LexaType.displaySmall.copy(
                fontFamily = PlayfairDisplay,
                fontSize = 48.sp
            ),
            color = LexaColors.TextPrimary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))

        // Tags
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallTag(text = question.word.partOfSpeech)
            Spacer(Modifier.padding(horizontal = 4.dp))
            SmallTag(text = question.word.level)
        }

        Spacer(Modifier.height(40.dp))

        // Options
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            question.options.forEachIndexed { index, optionText ->
                val state = when {
                    selectedOption == -1 -> QuizOptionState.Default
                    selectedOption == index && index == question.correctIndex -> QuizOptionState.Correct
                    selectedOption == index && index != question.correctIndex -> QuizOptionState.Wrong
                    index == question.correctIndex && selectedOption != -1 -> QuizOptionState.Correct
                    else -> QuizOptionState.Default
                }

                QuizOptionRow(
                    text = optionText,
                    state = if (selectedOption == -1) {
                        QuizOptionState.Default
                    } else if (index == selectedOption) {
                        if (index == question.correctIndex) QuizOptionState.Selected
                        else QuizOptionState.Wrong
                    } else {
                        QuizOptionState.Default
                    },
                    onClick = {
                        if (selectedOption == -1) {
                            selectedOption = index
                            if (index == question.correctIndex) {
                                onCorrect()
                            } else {
                                onWrong()
                            }
                        }
                    }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Counter
        Text(
            text = "${questionIndex + 1} of $total",
            style = LexaType.mono,
            color = LexaColors.TextMuted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun QuizQuestionScreenPreview() {
    LexaWallTheme {
        QuizQuestionScreen(
            questionIndex = 0,
            onCorrect = {},
            onWrong = {}
        )
    }
}

