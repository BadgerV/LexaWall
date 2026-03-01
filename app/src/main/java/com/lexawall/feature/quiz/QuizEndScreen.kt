package com.lexawall.feature.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.Spring
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.ui.*
import com.lexawall.core.utils.DevicePerformance
import com.lexawall.data.local.PrefsManager
import com.lexawall.utils.LexaWallLogger

@Composable
fun QuizEndScreen(
    score: Int,
    total: Int,
    streak: Int,
    xpEarned: Int,
    onNavigateLevelUp: (String, String, String) -> Unit,
    onBackToHome: () -> Unit,
    onBack: () -> Unit = onBackToHome,
    quizViewModel: QuizViewModel = viewModel()
) {
    val context = LocalContext.current
    val activeTheme = LocalLexaTheme.current
    val isLowEnd = remember(context) { DevicePerformance.isLowEndDevice(context) }
    val lexaMessage by quizViewModel.lexaMessage.collectAsState()
    val quizUiState by quizViewModel.uiState.collectAsState()
    val correctAnswers by quizViewModel.correctAnswers.collectAsState()
    val displayedTotal = quizUiState.questions.size.takeIf { it > 0 } ?: total
    val displayedScore = correctAnswers
    val hasGraduationToday = remember { !PrefsManager.getGraduationWordId(context).isNullOrBlank() }
    var animatedStreak by remember { mutableIntStateOf(0) }
    var showFlame by remember { mutableStateOf(false) }
    var flameScaleTarget by remember { mutableStateOf(0f) }
    var flameAlphaTarget by remember { mutableStateOf(0f) }
    var showHeadline by remember { mutableStateOf(false) }
    var showXpPill by remember { mutableStateOf(false) }
    var resolvedXpEarned by remember { mutableIntStateOf(xpEarned) }
    var resolvedStreak by remember { mutableIntStateOf(streak) }
    val flameScale by animateFloatAsState(
        targetValue = flameScaleTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "flameScale"
    )
    val flameAlpha by animateFloatAsState(
        targetValue = flameAlphaTarget,
        animationSpec = tween(durationMillis = 300),
        label = "flameAlpha"
    )
    val headlineAlpha by animateFloatAsState(
        targetValue = if (showHeadline) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "headlineAlpha"
    )
    val streakHeadline = remember(resolvedStreak) {
        when {
            resolvedStreak > 7 -> "🔥 You're on fire."
            resolvedStreak > 3 -> "Building momentum."
            resolvedStreak == 1 -> "Day one. Let's go."
            else -> "Keep going."
        }
    }
    val rotatingPraise = remember(displayedScore, displayedTotal) {
        when {
            displayedScore == displayedTotal -> listOf("Brilliant finish.", "You nailed every one.", "Perfect run.")
            displayedScore >= (displayedTotal * 0.8) -> listOf("Sharp work today.", "You are getting faster.", "Great momentum.")
            displayedScore >= (displayedTotal * 0.5) -> listOf("Nice progress.", "You are building range.", "Good consistency.")
            else -> listOf("Keep showing up.", "Momentum beats perfection.", "Tomorrow is another shot.")
        }.random()
    }

    LaunchedEffect(Unit) {
        val finalScore = displayedScore
        val finalTotal = displayedTotal
        resolvedXpEarned = quizViewModel.completeQuiz(finalScore, finalTotal)
        resolvedStreak = quizViewModel.lastSessionStreak.value.takeIf { it > 0 } ?: streak
        quizViewModel.levelUpEvent.value?.let { event ->
            onNavigateLevelUp(
                event.newDefinition.title,
                event.newDefinition.unlocksTheme.orEmpty(),
                event.newDefinition.unlocksWordTier.orEmpty()
            )
            quizViewModel.clearLevelUpEvent()
        }
        if (hasGraduationToday) {
            PrefsManager.saveGraduationWordId(context, null)
        }
        LexaWallLogger.d("FireAnim", "streakCount=$resolvedStreak")
        flameAlphaTarget = 0f
        flameScaleTarget = 0f
        kotlinx.coroutines.delay(600)
        showFlame = true
        flameAlphaTarget = 1f
        flameScaleTarget = 1.2f
        kotlinx.coroutines.delay(200)
        flameScaleTarget = 1f
        kotlinx.coroutines.delay(200)
        animatedStreak = 0
        while (animatedStreak < resolvedStreak) {
            kotlinx.coroutines.delay(40)
            animatedStreak += 1
        }
        kotlinx.coroutines.delay(200)
        showXpPill = true
        showHeadline = true
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(LexaColors.BackgroundDark),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                streakHeadline,
                style = LexaType.headlineLarge.copy(
                    fontFamily = PlayfairDisplay,
                    fontStyle = FontStyle.Italic
                ),
                color = LexaColors.Primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(headlineAlpha)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "$displayedScore/$displayedTotal",
                style = LexaType.scoreDisplay,
                color = LexaColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                when {
                    displayedTotal > 0 && displayedScore == displayedTotal -> "Flawless."
                    displayedScore >= (displayedTotal * 0.8) -> "Honestly impressive."
                    displayedScore >= (displayedTotal * 0.5) -> "Solid effort."
                    else -> "Keep going."
                },
                style = LexaType.headlineLarge,
                color = LexaColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                rotatingPraise,
                style = LexaType.bodyLarge,
                color = LexaColors.TextMuted,
                textAlign = TextAlign.Center
            )
            if (!lexaMessage?.text.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    lexaMessage?.text.orEmpty(),
                    style = LexaType.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = LexaColors.TextMuted,
                    textAlign = TextAlign.Center
                )
            }
            if (hasGraduationToday) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "You made a word yours today.",
                    style = LexaType.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = LexaColors.TextMuted,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showFlame) {
                    StreakGlowPulse(
                        isLowEnd = isLowEnd,
                        accentColor = Color(activeTheme.accentColor),
                        modifier = Modifier.size(110.dp)
                    )
                    Text(
                        "🔥",
                        fontSize = 48.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = (-4).dp)
                            .graphicsLayer(
                                scaleX = flameScale,
                                scaleY = flameScale,
                                alpha = flameAlpha
                            )
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (showFlame) {
                Text(
                    "$animatedStreak",
                    style = LexaType.titleLarge.copy(fontSize = 52.sp, fontWeight = FontWeight.Bold),
                    color = LexaColors.Primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    "DAY STREAK",
                    style = LexaType.monoSmall.copy(fontFamily = DmMono),
                    color = LexaColors.Primary,
                    textAlign = TextAlign.Center
                )
                if (showXpPill) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
                            .background(Color(activeTheme.surfaceColor))
                            .border(1.dp, LexaColors.Primary.copy(alpha = 0.7f), androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "+$resolvedXpEarned XP",
                            style = LexaType.monoSmall.copy(fontFamily = DmMono, fontSize = 11.sp),
                            color = LexaColors.Primary
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "",
                style = LexaType.bodySmall,
                color = Color.Transparent,
                textAlign = TextAlign.Center
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            PrimaryButton("Back to home", onBackToHome)
        }
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 16.dp, start = 8.dp)
                .statusBarsPadding()
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(activeTheme.accentColorDim),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun StreakGlowPulse(
    isLowEnd: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val alpha = if (isLowEnd) {
        0.6f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "streakGlowTransition")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowPulseAlpha"
        ).value
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = alpha),
                        Color.Transparent
                    )
                )
            )
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun Preview() {
    LexaWallTheme {
        QuizEndScreen(
            score = 11,
            total = 13,
            streak = 6,
            xpEarned = 275,
            onNavigateLevelUp = { _, _, _ -> },
            onBackToHome = {}
        )
    }
}
