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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.GlowCircle
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.components.ProgressDotsRow
import com.lexawall.core.components.QuizOptionRow
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LocalLexaTheme
import com.lexawall.core.ui.PlayfairDisplay
import com.lexawall.data.local.DatabaseModule
import com.lexawall.domain.model.QuizOptionState
import com.lexawall.domain.model.QuizQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RedemptionQuizScreen(
    currentStreak: Int,
    onPass: () -> Unit,
    onFail: () -> Unit
) {
    val activeTheme = LocalLexaTheme.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { DatabaseModule.getInstance(context) }
    val quizRepository = remember { DatabaseModule.provideQuizRepository(context) }

    var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedOption by remember(index) { mutableIntStateOf(-1) }
    var finished by remember { mutableStateOf(false) }
    var passed by remember { mutableStateOf(false) }
    var passHeadlineVisible by remember { mutableStateOf(false) }
    val passHeadlineScale by animateFloatAsState(
        targetValue = if (passHeadlineVisible) 1f else 0.7f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "passHeadlineScale"
    )

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val eligibleIds = (db.userProgressDao().getWordsByState("SEEN") +
                db.userProgressDao().getWordsByState("TESTED"))
                .map { it.wordId }
                .toSet()

            val pool = quizRepository.getQuizQuestions()
            val filtered = pool.filter { it.word.id in eligibleIds }
            questions = (if (filtered.size >= 5) filtered else pool).take(5)
        }
    }
    LaunchedEffect(finished, passed) {
        if (finished && passed) {
            passHeadlineVisible = true
        }
    }

    if (questions.isEmpty() && !finished) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .background(LexaColors.BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading...", style = LexaType.bodyMedium, color = LexaColors.TextMuted)
        }
        return
    }

    if (finished) {
        if (passed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .background(LexaColors.BackgroundDark),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    GlowCircle(
                        icon = { Icon(Icons.Default.Check, null, tint = LexaColors.BackgroundDark) },
                        glowColor = LexaColors.Success
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Streak saved.",
                        style = LexaType.headlineLarge.copy(fontFamily = PlayfairDisplay),
                        color = LexaColors.Primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer(scaleX = passHeadlineScale, scaleY = passHeadlineScale)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "🔥 $currentStreak",
                        style = LexaType.wordDisplay.copy(fontFamily = PlayfairDisplay, fontSize = 48.sp),
                        color = LexaColors.Primary
                    )
                    Spacer(Modifier.height(24.dp))
                    PrimaryButton("Keep going →", onPass)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .background(LexaColors.BackgroundDark),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    GlowCircle(
                        icon = { Icon(Icons.Default.Close, null, tint = Color(activeTheme.accentColorDim)) },
                        glowColor = Color(activeTheme.accentColorDim)
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Start again today.",
                        style = LexaType.headlineLarge.copy(fontFamily = PlayfairDisplay),
                        color = LexaColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Everyone pauses. The words are still here.",
                        style = LexaType.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 13.sp),
                        color = Color(activeTheme.accentColorDim),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "🔥 0",
                        style = LexaType.wordDisplay.copy(fontFamily = PlayfairDisplay, fontSize = 48.sp),
                        color = LexaColors.TextPrimary
                    )
                    Spacer(Modifier.height(24.dp))
                    PrimaryButton("Start fresh →", onFail)
                }
            }
        }
        return
    }

    val q = questions[index]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(LexaColors.BackgroundDark)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            "RESTORE YOUR STREAK",
            style = LexaType.mono,
            color = LexaColors.Primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        ProgressDotsRow(total = 5, current = index, answered = index)
        Spacer(Modifier.height(32.dp))
        Text(
            "🔥 $currentStreak",
            style = LexaType.wordDisplay.copy(fontFamily = PlayfairDisplay, fontSize = 48.sp),
            color = LexaColors.Primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Answer 4 of 5 correctly to restore it.",
            style = LexaType.bodyMedium,
            color = LexaColors.TextMuted,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        Text(
            q.word.text,
            style = LexaType.displaySmall.copy(fontFamily = PlayfairDisplay, fontSize = 44.sp),
            color = LexaColors.TextPrimary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))

        q.options.forEachIndexed { optionIndex, option ->
            val state = when {
                selectedOption == -1 -> QuizOptionState.Default
                optionIndex == selectedOption && optionIndex == q.correctIndex -> QuizOptionState.Selected
                optionIndex == selectedOption -> QuizOptionState.Wrong
                else -> QuizOptionState.Default
            }

            QuizOptionRow(
                text = option,
                state = state,
                onClick = {
                    if (selectedOption != -1) return@QuizOptionRow

                    selectedOption = optionIndex
                    if (optionIndex == q.correctIndex) score += 1

                    scope.launch {
                        delay(400)
                        if (index == questions.lastIndex) {
                            passed = score >= 4
                            finished = true
                        } else {
                            index += 1
                            selectedOption = -1
                        }
                    }
                },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}
