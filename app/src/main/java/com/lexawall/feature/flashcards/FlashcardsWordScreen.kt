package com.lexawall.feature.flashcards

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lexawall.core.components.GhostButton
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.ui.DmMono
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LocalLexaTheme
import com.lexawall.core.ui.PlayfairDisplay
import com.lexawall.core.ui.components.LexaMascotAnimated
import kotlinx.coroutines.delay

@Composable
fun FlashcardsWordScreen(
    cardIndex: Int,
    onReveal: () -> Unit,
    onStillLearning: (Int, Boolean) -> Unit,
    onKnowIt: (Int, Boolean) -> Unit,
    onClose: () -> Unit,
    viewModel: FlashcardsViewModel = viewModel()
) {
    val theme = LocalLexaTheme.current
    val words by viewModel.words.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val sessionsToday by viewModel.sessionsToday.collectAsState()
    val sessionLimit by viewModel.sessionLimit.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var currentIndex by remember { mutableIntStateOf(cardIndex.coerceAtLeast(0)) }
    var showSessionEnd by remember { mutableStateOf(false) }
    var endMessage by remember { mutableStateOf("That's a session.") }
    var sessionsRemainingInEndState by remember { mutableIntStateOf(0) }

    if (isLoading) {
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

    if (isLocked) {
        FlashcardsLockedState(
            sessionLimit = sessionLimit,
            onStartWordDrop = { onKnowIt(0, true) },
            onComeBackTomorrow = onClose
        )
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
                PrimaryButton("Do today's Word Drop →", onClick = { onKnowIt(0, true) })
            }
        }
        return
    }

    if (showSessionEnd) {
        FlashcardsSessionEndState(
            message = endMessage,
            sessionsRemaining = sessionsRemainingInEndState,
            onGoAgain = {
                showSessionEnd = false
                currentIndex = 0
            },
            onStartWordDrop = { onKnowIt(0, true) },
            onComeBackTomorrow = onClose
        )
        return
    }

    val safeIndex = currentIndex.coerceIn(0, words.lastIndex)
    val word = words[safeIndex]
    val isLast = safeIndex == words.lastIndex
    var secondsRemaining by remember(safeIndex, timerSeconds) { mutableIntStateOf(timerSeconds) }
    val progress by animateFloatAsState(
        targetValue = (secondsRemaining.toFloat() / timerSeconds.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900, easing = LinearEasing),
        label = "flashcardTimerProgress"
    )

    fun completeSession() {
        val updatedCount = sessionsToday + 1
        viewModel.completeSession()
        sessionsRemainingInEndState = (sessionLimit - updatedCount).coerceAtLeast(0)
        endMessage = listOf("That's a session.", "Words seen.", "You showed up.").random()
        showSessionEnd = true
    }

    fun advanceCard() {
        if (isLast) {
            completeSession()
        } else {
            currentIndex += 1
        }
    }

    LaunchedEffect(safeIndex, showSessionEnd, timerSeconds) {
        if (showSessionEnd) return@LaunchedEffect
        secondsRemaining = timerSeconds
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining -= 1
        }
        advanceCard()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color(theme.surfaceColor))
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
            color = Color(theme.accentColor),
            trackColor = Color(0xFF1E1E2A)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(24.dp)
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
                    color = Color(0xFFAAAAAA)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            PrimaryButton("Next →", onClick = { advanceCard() })
        }
    }
}

@Composable
private fun FlashcardsLockedState(
    sessionLimit: Int,
    onStartWordDrop: () -> Unit,
    onComeBackTomorrow: () -> Unit
) {
    val title = if (sessionLimit == 1) {
        "You've seen today's words."
    } else {
        "You've seen today's words twice."
    }
    val subtitle = if (sessionLimit == 1) {
        "That's your session for today."
    } else {
        "That's your two sessions for today."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(LexaColors.BackgroundDark)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LexaMascotAnimated(size = 48.dp)
        Spacer(Modifier.height(16.dp))
        Text(
            title,
            style = LexaType.wordMedium.copy(fontStyle = FontStyle.Italic, fontSize = 18.sp),
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            style = LexaType.bodySmall.copy(fontSize = 13.sp),
            color = Color(0xFF888899),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        PrimaryButton("Do today's Word Drop →", onStartWordDrop)
        Spacer(Modifier.height(8.dp))
        GhostButton("Come back tomorrow", onComeBackTomorrow)
    }
}

@Composable
private fun FlashcardsSessionEndState(
    message: String,
    sessionsRemaining: Int,
    onGoAgain: () -> Unit,
    onStartWordDrop: () -> Unit,
    onComeBackTomorrow: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(LexaColors.BackgroundDark)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LexaMascotAnimated(size = 48.dp)
        Spacer(Modifier.height(16.dp))
        Text(
            message,
            style = LexaType.wordMedium.copy(fontStyle = FontStyle.Italic, fontSize = 18.sp),
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (sessionsRemaining == 1) "You have one more session today." else "That's both sessions for today.",
            style = LexaType.bodySmall.copy(fontSize = 13.sp),
            color = Color(0xFF888899),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        if (sessionsRemaining > 0) {
            PrimaryButton("Go again →", onGoAgain)
            Spacer(Modifier.height(8.dp))
            GhostButton("Do today's Word Drop →", onStartWordDrop)
        } else {
            PrimaryButton("Do today's Word Drop →", onStartWordDrop)
            Spacer(Modifier.height(8.dp))
            GhostButton("Come back tomorrow", onComeBackTomorrow)
        }
    }
}
