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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.lexawall.core.components.ProgressDotsRow
import com.lexawall.core.components.QuizOptionRow
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LexaWallTheme
import com.lexawall.core.ui.PlayfairDisplay
import com.lexawall.data.local.PrefsManager
import com.lexawall.domain.model.QuizOptionState
import kotlinx.coroutines.delay

data class CalibrationQuestion(
    val level: String,
    val word: String,
    val options: List<String>,
    val correctIndex: Int
)

private val calibrationQuestions = listOf(
    CalibrationQuestion(
        level = "A2",
        word = "abundant",
        options = listOf(
            "existing in large quantities",
            "difficult to find",
            "lasting a short time"
        ),
        correctIndex = 0
    ),
    CalibrationQuestion(
        level = "B1",
        word = "ambiguous",
        options = listOf(
            "open to more than one interpretation",
            "very clear and obvious",
            "formally agreed upon"
        ),
        correctIndex = 0
    ),
    CalibrationQuestion(
        level = "B2",
        word = "ephemeral",
        options = listOf(
            "lasting for a very short time",
            "extremely important",
            "widely known by many people"
        ),
        correctIndex = 0
    ),
    CalibrationQuestion(
        level = "C1",
        word = "altruistic",
        options = listOf(
            "showing selfless concern for others",
            "motivated by personal gain",
            "relating to artistic beauty"
        ),
        correctIndex = 0
    ),
    CalibrationQuestion(
        level = "C1",
        word = "serendipity",
        options = listOf(
            "finding something good by chance",
            "a feeling of deep sadness",
            "the study of ancient history"
        ),
        correctIndex = 0
    )
)

@Composable
fun CalibrationQuizScreen(onComplete: () -> Unit) {
    val context = LocalContext.current

    var showIntro by rememberSaveable { mutableStateOf(true) }
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedOption by remember(currentIndex) { mutableIntStateOf(-1) }
    var correctCount by rememberSaveable { mutableIntStateOf(0) }
    val shuffledQuestions = remember {
        calibrationQuestions.shuffled().map { question ->
            val indexedOptions = question.options
                .mapIndexed { optionIndex, option -> optionIndex to option }
                .shuffled()
            val shuffledOptions = indexedOptions.map { it.second }
            val shuffledCorrectIndex = indexedOptions.indexOfFirst { it.first == question.correctIndex }
            question.copy(options = shuffledOptions, correctIndex = shuffledCorrectIndex)
        }
    }

    val currentQuestion = shuffledQuestions[currentIndex]

    if (showIntro) {
        LaunchedEffect(Unit) {
            delay(2000L)
            showIntro = false
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .background(LexaColors.BackgroundDark)
                .padding(horizontal = 24.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "Five quick questions. I just want to know where you are.",
                style = LexaType.headlineMedium.copy(fontFamily = PlayfairDisplay),
                color = LexaColors.BrandGold,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(LexaColors.BackgroundDark)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(48.dp))
        ProgressDotsRow(
            total = shuffledQuestions.size,
            current = currentIndex,
            answered = currentIndex
        )

        Spacer(Modifier.height(28.dp))
        Text(
            text = currentQuestion.level,
            style = LexaType.overline,
            color = LexaColors.Primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))
        Text(
            text = currentQuestion.word,
            style = LexaType.displaySmall.copy(fontFamily = PlayfairDisplay, fontSize = 48.sp),
            color = LexaColors.TextPrimary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(36.dp))
        currentQuestion.options.forEachIndexed { optionIndex, option ->
            val optionState = when {
                selectedOption == -1 -> QuizOptionState.Default
                optionIndex == selectedOption -> {
                    if (optionIndex == currentQuestion.correctIndex) QuizOptionState.Selected else QuizOptionState.Wrong
                }
                else -> QuizOptionState.Default
            }

            QuizOptionRow(
                text = option,
                state = optionState,
                onClick = {
                    if (selectedOption != -1) return@QuizOptionRow

                    selectedOption = optionIndex
                    if (optionIndex == currentQuestion.correctIndex) {
                        correctCount += 1
                    }

                    if (currentIndex == shuffledQuestions.lastIndex) {
                        val level = when {
                            correctCount <= 1 -> "A2"
                            correctCount == 2 -> "B1"
                            correctCount == 3 -> "B2"
                            else -> "C1"
                        }
                        PrefsManager.saveUserLevel(context, level)
                        onComplete()
                    } else {
                        currentIndex += 1
                        selectedOption = -1
                    }
                },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewCalibrationQuizScreen() {
    LexaWallTheme {
        CalibrationQuizScreen(onComplete = {})
    }
}
