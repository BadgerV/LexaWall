package com.lexawall.feature.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lexawall.core.components.*
import com.lexawall.core.ui.*
import com.lexawall.data.local.PrefsManager
import com.lexawall.domain.model.QuizOptionState
import kotlinx.coroutines.launch

@Composable
fun QuizQuestionScreen(
    questionIndex: Int,
    onCorrect: () -> Unit,
    onWrong: () -> Unit,
    onBackToHome: () -> Unit = {},
    quizViewModel: QuizViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by quizViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(questionIndex) {
        if (questionIndex == 0) {
            quizViewModel.resetQuizSession()
        }
    }

    if (uiState.questions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .background(LexaColors.BackgroundDark)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Come back tomorrow — more words are on the way.",
                    style = LexaType.bodyLarge,
                    color = LexaColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                GhostButton("Back to home", onBackToHome)
            }
        }
        return
    }

    val question = quizViewModel.getQuestion(questionIndex) ?: return
    val total = quizViewModel.getTotalQuestions()
    val currentWallpaperWordId = remember(context) { PrefsManager.getCurrentWordId(context) }
    val isWallpaperWord = question.word.id == currentWallpaperWordId
    var selectedOption by remember(questionIndex) { mutableIntStateOf(-1) }

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(LexaColors.BackgroundDark)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(48.dp))
        ProgressDotsRow(total = total, current = questionIndex, answered = questionIndex)
        Spacer(Modifier.height(24.dp))
        Text(
            "What does this word mean?",
            style = LexaType.overline,
            color = LexaColors.TextSecondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        if (isWallpaperWord) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .background(LexaColors.Primary.copy(alpha = 0.18f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    "YOUR WALLPAPER WORD",
                    style = LexaType.overlineSmall.copy(fontSize = 10.sp),
                    color = LexaColors.Primary
                )
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(
            question.word.text,
            style = LexaType.displaySmall.copy(fontFamily = PlayfairDisplay, fontSize = 48.sp),
            color = LexaColors.TextPrimary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            SmallTag(question.word.partOfSpeech)
            Spacer(Modifier.padding(horizontal = 4.dp))
            SmallTag(question.word.level)
        }
        Spacer(Modifier.height(40.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.options.forEachIndexed { index, optionText ->
                val state = if (selectedOption == -1) {
                    QuizOptionState.Default
                } else if (index == selectedOption) {
                    if (index == question.correctIndex) QuizOptionState.Correct else QuizOptionState.Wrong
                } else {
                    QuizOptionState.Default
                }

                var scaleTarget by remember(questionIndex, index) { mutableStateOf(1f) }
                val scale by animateFloatAsState(
                    targetValue = scaleTarget,
                    animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing),
                    label = "quizOptionScale"
                )

                var shakeTarget by remember(questionIndex, index) { mutableStateOf(0f) }
                val shakeOffset by animateFloatAsState(
                    targetValue = shakeTarget,
                    animationSpec = tween(durationMillis = 80, easing = LinearOutSlowInEasing),
                    label = "quizOptionShake"
                )

                LaunchedEffect(state, selectedOption) {
                    if (state == QuizOptionState.Correct && selectedOption == index) {
                        scaleTarget = 1.05f
                        kotlinx.coroutines.delay(200)
                        scaleTarget = 1f
                    }
                }
                LaunchedEffect(state, selectedOption, "shake_$index") {
                    if (state == QuizOptionState.Wrong && selectedOption == index) {
                        shakeTarget = 8f
                        kotlinx.coroutines.delay(80)
                        shakeTarget = -8f
                        kotlinx.coroutines.delay(80)
                        shakeTarget = 6f
                        kotlinx.coroutines.delay(80)
                        shakeTarget = -6f
                        kotlinx.coroutines.delay(80)
                        shakeTarget = 0f
                    }
                }

                QuizOptionRow(
                    text = optionText,
                    state = state,
                    modifier = Modifier.graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = shakeOffset
                    ),
                    onClick = {
                        if (selectedOption == -1) {
                            selectedOption = index
                            if (index == question.correctIndex) {
                                quizViewModel.recordAnswer(question.word.id, true)
                                scope.launch {
                                    kotlinx.coroutines.delay(400)
                                    onCorrect()
                                }
                            } else {
                                quizViewModel.recordAnswer(question.word.id, false)
                                scope.launch {
                                    kotlinx.coroutines.delay(400)
                                    onWrong()
                                }
                            }
                        }
                    }
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            "${questionIndex + 1} of $total",
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
private fun Preview() {
    LexaWallTheme { QuizQuestionScreen(0, {}, {}) }
}
