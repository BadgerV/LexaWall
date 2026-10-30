package com.lexawall.feature.flashcards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler
import com.lexawall.core.components.GhostButton
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.ui.DmMono
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LocalLexaTheme
import com.lexawall.core.ui.PlayfairDisplay
import com.lexawall.data.local.PrefsManager
import kotlinx.coroutines.delay
import java.time.LocalDate

@Composable
fun FlashcardsWordScreen(
    cardIndex: Int,
    onReveal: () -> Unit,
    onStillLearning: (Int, Boolean) -> Unit,
    onKnowIt: (Int, Boolean) -> Unit,
    onClose: () -> Unit,
    viewModel: FlashcardsViewModel = viewModel()
) {
    val context = LocalContext.current
    val theme = LocalLexaTheme.current
    val words by viewModel.words.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val isCompetitiveMode by viewModel.isCompetitiveMode.collectAsState()
    val sessionLimit by viewModel.sessionLimit.collectAsState()

    var gateReady by remember { mutableStateOf(false) }
    var isPracticeLocked by remember { mutableStateOf(false) }
    var showLeaveWarning by remember { mutableStateOf(false) }
    var showDailyLimitInfo by remember { mutableStateOf(false) }

    fun countExitAsSession() {
        val today = LocalDate.now().toString()
        val savedDate = PrefsManager.getFlashcardSessionDate(context)
        if (savedDate != today) {
            PrefsManager.saveFlashcardSessionCount(context, 0)
            PrefsManager.saveFlashcardSessionDate(context, today)
        }
        val currentCount = PrefsManager.getFlashcardSessionCount(context)
        PrefsManager.saveFlashcardSessionCount(context, currentCount + 1)
        PrefsManager.saveFlashcardSessionDate(context, today)
    }

    LaunchedEffect(Unit) {
        val today = LocalDate.now().toString()
        val savedDate = PrefsManager.getFlashcardSessionDate(context)
        if (savedDate != today) {
            PrefsManager.saveFlashcardSessionCount(context, 0)
            PrefsManager.saveFlashcardSessionDate(context, today)
        }
        val todayCount = PrefsManager.getFlashcardSessionCount(context)
        val competitive = PrefsManager.isCompetitiveModeEnabled(context)
        isPracticeLocked = todayCount >= (if (competitive) 1 else 2)
        val infoDate = PrefsManager.getFlashcardLimitNoticeDate(context)
        showDailyLimitInfo = !isPracticeLocked && infoDate != today
        gateReady = true
    }

    if (!gateReady || isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(LexaColors.BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading...", style = LexaType.bodyMedium, color = LexaColors.TextMuted)
        }
        return
    }

    if (isPracticeLocked) {
        BackHandler(enabled = true) {
            onClose()
        }
        FlashcardsPracticeGateScreen(onBackToHome = onClose)
        return
    }

    if (words.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(LexaColors.BackgroundDark)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No words ready yet.", style = LexaType.bodyLarge, color = LexaColors.TextPrimary)
                Spacer(Modifier.height(16.dp))
                PrimaryButton("Back to home", onClick = onClose)
            }
        }
        return
    }

    if (showDailyLimitInfo) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(Color(theme.surfaceColor))
        )
        AlertDialog(
            onDismissRequest = {
                val today = LocalDate.now().toString()
                PrefsManager.saveFlashcardLimitNoticeDate(context, today)
                showDailyLimitInfo = false
            },
            title = {
                Text(
                    text = "Flashcard daily limit",
                    style = LexaType.titleMedium,
                    color = LexaColors.TextPrimary
                )
            },
            text = {
                Text(
                    text = if (isCompetitiveMode) {
                        "Competitive mode: 1 fast session per day (${timerSeconds}s per card). Leaving after starting counts as your session."
                    } else {
                        "You can run $sessionLimit flashcard sessions per day. If you leave after starting, that counts as one session."
                    },
                    style = LexaType.bodyMedium,
                    color = LexaColors.TextMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val today = LocalDate.now().toString()
                        PrefsManager.saveFlashcardLimitNoticeDate(context, today)
                        showDailyLimitInfo = false
                    }
                ) {
                    Text("Got it", color = LexaColors.Primary)
                }
            },
            containerColor = LexaColors.SurfaceDark
        )
        return
    }

    BackHandler(enabled = true) {
        showLeaveWarning = true
    }

    val safeIndex = cardIndex.coerceIn(0, words.lastIndex)
    val word = words[safeIndex]
    val isLastCard = safeIndex == words.lastIndex

    fun advanceToNextCard() {
        if (isLastCard) {
            onStillLearning(safeIndex, true)
        } else {
            onStillLearning(safeIndex + 1, false)
        }
    }

    var timerProgress by remember(safeIndex) { mutableFloatStateOf(1f) }
    var revealTriggered by remember(safeIndex) { mutableStateOf(false) }
    var timerCancelled by remember(safeIndex) { mutableStateOf(false) }

    val totalSteps = remember(timerSeconds) { timerSeconds * 10 }
    val decrementPerStep = remember(totalSteps) { 1f / totalSteps.toFloat() }

    LaunchedEffect(safeIndex, timerSeconds) {
        timerProgress = 1f
        revealTriggered = false
        timerCancelled = false

        repeat(totalSteps) {
            delay(100)
            if (timerCancelled || revealTriggered) return@LaunchedEffect
            timerProgress = (timerProgress - decrementPerStep).coerceAtLeast(0f)
        }

        if (!timerCancelled && !revealTriggered) {
            timerProgress = 0f
            revealTriggered = true
            advanceToNextCard()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color(theme.surfaceColor))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Canvas(modifier = Modifier.size(42.dp)) {
                drawArc(
                    color = Color(theme.accentColor).copy(alpha = 0.2f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color(theme.accentColor),
                    startAngle = -90f,
                    sweepAngle = 360f * timerProgress,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        Text(
            text = if (isCompetitiveMode) "Competitive: $timerSeconds sec/card · $sessionLimit session/day" else "Daily limit: $sessionLimit sessions · ${timerSeconds}s per card",
            style = LexaType.bodySmall.copy(fontFamily = DmMono, fontSize = 11.sp),
            color = LexaColors.TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
                .clickable {
                    if (!revealTriggered) {
                        timerCancelled = true
                        timerProgress = 0f
                        revealTriggered = true
                        onReveal()
                    }
                }
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = word.word,
                style = LexaType.wordDisplay.copy(
                    fontFamily = PlayfairDisplay,
                    fontStyle = FontStyle.Italic,
                    fontSize = 38.sp
                ),
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = word.partOfSpeech.uppercase(),
                style = LexaType.bodySmall.copy(fontFamily = DmMono, fontSize = 11.sp),
                color = Color(theme.accentColor)
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(1.dp)
                    .background(Color(theme.accentColor))
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = word.definition,
                style = LexaType.bodyLarge.copy(fontSize = 16.sp, lineHeight = 25.sp),
                color = Color(0xFFE8E8E8)
            )
            Spacer(Modifier.height(12.dp))
            Row {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(52.dp)
                        .background(Color(theme.accentColor))
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = word.exampleSentence,
                    style = LexaType.bodyMedium.copy(fontStyle = FontStyle.Italic, fontSize = 14.sp),
                    color = Color(0xFFCCCCCC)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            PrimaryButton("Next →", onClick = {
                timerCancelled = true
                advanceToNextCard()
            })
        }
    }

    if (showLeaveWarning) {
        AlertDialog(
            onDismissRequest = { showLeaveWarning = false },
            title = {
                Text(
                    text = "Leave session?",
                    style = LexaType.titleMedium,
                    color = LexaColors.TextPrimary
                )
            },
            text = {
                Text(
                    text = if (isCompetitiveMode) "Leaving now uses your only session for today." else "Leaving now counts as one flashcard session. You only have $sessionLimit per day.",
                    style = LexaType.bodyMedium,
                    color = LexaColors.TextMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        countExitAsSession()
                        showLeaveWarning = false
                        onClose()
                    }
                ) {
                    Text("Leave", color = LexaColors.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveWarning = false }) {
                    Text("Keep practicing", color = LexaColors.TextSecondary)
                }
            },
            containerColor = LexaColors.SurfaceDark
        )
    }

}

@Composable
private fun FlashcardsPracticeGateScreen(onBackToHome: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(LexaColors.BackgroundDark)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = LexaColors.Primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "You've practiced twice today.",
            style = LexaType.headlineLarge.copy(
                fontFamily = PlayfairDisplay,
                fontSize = 24.sp,
                fontStyle = FontStyle.Italic
            ),
            color = LexaColors.Primary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Come back after your quiz for more practice.",
            style = LexaType.bodyMedium,
            color = LexaColors.TextMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        GhostButton("Back to home", onClick = onBackToHome)
    }
}
