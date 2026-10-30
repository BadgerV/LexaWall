package com.lexawall.feature.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.ui.DmMono
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LexaWallTheme
import com.lexawall.core.ui.LocalLexaTheme
import com.lexawall.core.ui.PlayfairDisplay
import com.lexawall.domain.model.UserStats
import com.lexawall.domain.model.Word

@Composable
@Suppress("UNUSED_PARAMETER")
fun HomeScreen(
    onStartQuiz: () -> Unit,
    onOpenFlashcards: () -> Unit,
    onWordTap: (String) -> Unit,
    onRestoreStreak: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    HomeContent(
        uiState = uiState,
        wallpaperCardWord = uiState.wallpaperWord,
        onStartQuiz = onStartQuiz,
        onWordTap = onWordTap,
        onRestoreStreak = onRestoreStreak,
        onDismissBanner = viewModel::dismissBanner
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    wallpaperCardWord: Word,
    onStartQuiz: () -> Unit,
    onWordTap: (String) -> Unit,
    onRestoreStreak: () -> Unit,
    onDismissBanner: () -> Unit
) {
    if (uiState.isLoading) {
        val activeTheme = LocalLexaTheme.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .background(Color(activeTheme.backgroundTop)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(activeTheme.accentColor))
        }
        return
    }

    val activeTheme = LocalLexaTheme.current
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(activeTheme.backgroundTop),
            Color(activeTheme.backgroundBottom)
        )
    )
    val accent = Color(activeTheme.accentColor)
    val mutedAccent = Color(activeTheme.accentColorDim)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(22.dp))

        HeaderSection(
            greeting = uiState.greeting,
            title = uiState.currentTitle,
            streakDays = uiState.stats.streakDays,
            accentColor = accent,
            surfaceColor = Color(activeTheme.surfaceColor),
            mutedColor = Color(activeTheme.accentColorDim).copy(alpha = 0.75f)
        )

        Spacer(Modifier.height(18.dp))

        StatsRow(
            snapshot = uiState.dailyProgress,
            surfaceColor = Color(activeTheme.surfaceColor)
        )

        Spacer(Modifier.height(16.dp))

        if (uiState.showWallpaperBanner) {
            WallpaperBannerCard(
                accent = accent,
                surfaceColor = Color(activeTheme.surfaceColor),
                mutedColor = Color(activeTheme.accentColorDim).copy(alpha = 0.75f),
                onDismiss = onDismissBanner
            )
            Spacer(Modifier.height(16.dp))
        }

        when (uiState.screenState) {
            HomeScreenState.DEFAULT,
            HomeScreenState.DAY_1,
            HomeScreenState.WELCOME_BACK,
            HomeScreenState.WALLPAPER_NOT_SET -> {
                WordDropAvailableCard(
                    accentColor = accent,
                    dimAccent = mutedAccent,
                    backgroundTop = Color(activeTheme.backgroundTop),
                    backgroundBottom = Color(activeTheme.backgroundBottom),
                    onStart = onStartQuiz
                )
            }

            HomeScreenState.RESUME_QUIZ -> {
                ResumeWordDropCard(
                    accentColor = accent,
                    dimAccent = mutedAccent,
                    onContinue = onStartQuiz
                )
            }

            HomeScreenState.QUIZ_COMPLETED -> {
                WordDropCompletedCard(accentColor = accent)
            }

            HomeScreenState.REDEMPTION_AVAILABLE -> {
                RedemptionAvailableCard(
                    streakDays = uiState.stats.streakDays,
                    onRestoreStreak = onRestoreStreak,
                    accentColor = accent
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        WallpaperWordCard(
            word = wallpaperCardWord,
            accentColor = accent,
            dimAccent = mutedAccent,
            surfaceColor = Color(activeTheme.surfaceColor),
            backgroundTop = Color(activeTheme.backgroundTop),
            backgroundBottom = Color(activeTheme.backgroundBottom),
            showQuizAnchorTag = uiState.screenState == HomeScreenState.DEFAULT,
            onClick = { onWordTap(wallpaperCardWord.id) }
        )

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun HeaderSection(
    greeting: String,
    title: String,
    streakDays: Int,
    accentColor: Color,
    surfaceColor: Color,
    mutedColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = greeting.uppercase(),
                style = LexaType.overlineSmall.copy(
                    fontFamily = DmMono,
                    letterSpacing = 1.8.sp,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = mutedColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = PlayfairDisplay,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 30.sp,
                    lineHeight = 36.sp
                ),
                color = Color.White
            )
        }

        if (streakDays > 0) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(surfaceColor.copy(alpha = 0.95f))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "🔥",
                    style = LexaType.bodySmall.copy(fontSize = 16.sp)
                )
                Text(
                    streakDays.toString(),
                    style = LexaType.monoSmall.copy(fontFamily = DmMono, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    color = accentColor
                )
            }
        }
    }
}

@Composable
private fun StatsRow(
    snapshot: DailyProgressSnapshot,
    surfaceColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatTile(
            modifier = Modifier.weight(1f),
            icon = { Text(text = "📖", fontSize = 20.sp) },
            value = "${snapshot.wordsLearnedToday} answered",
            subtitle = "today",
            surfaceColor = surfaceColor
        )

        StatTile(
            modifier = Modifier.weight(1f),
            icon = { Text(text = "⚡", fontSize = 20.sp) },
            value = "${snapshot.xpEarnedToday} XP",
            subtitle = "Earned",
            surfaceColor = surfaceColor
        )

        StatTile(
            modifier = Modifier.weight(1f),
            icon = { Text("🔥", style = LexaType.bodySmall) },
            value = snapshot.currentStreak.toString(),
            subtitle = "Streak",
            surfaceColor = surfaceColor
        )

        StatTile(
            modifier = Modifier.weight(1f),
            icon = { Text(text = "⭐", fontSize = 20.sp) },
            value = "Lv. ${snapshot.currentLevel}",
            subtitle = "Explore",
            surfaceColor = surfaceColor
        )
    }
}

@Composable
private fun StatTile(
    modifier: Modifier,
    icon: @Composable () -> Unit,
    value: String,
    subtitle: String,
    surfaceColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val bgColor by animateColorAsState(
        targetValue = if (pressed) surfaceColor.copy(alpha = 0.82f) else surfaceColor.copy(alpha = 0.94f),
        animationSpec = tween(durationMillis = 120),
        label = "homeStatPress"
    )

    Column(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = {})
            .padding(horizontal = 8.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()
        Text(
            text = value,
            style = LexaType.monoSmall.copy(fontFamily = DmMono, fontSize = 12.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            text = subtitle,
            style = LexaType.monoSmall.copy(fontFamily = DmMono, fontSize = 10.sp, fontWeight = FontWeight.Medium),
            color = LexaColors.TextMuted,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun WallpaperBannerCard(
    accent: Color,
    surfaceColor: Color,
    mutedColor: Color,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(999.dp))
            .background(surfaceColor.copy(alpha = 0.95f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(999.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(surfaceColor.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            Text("🖼", style = LexaType.monoSmall.copy(fontSize = 14.sp))
        }
        Text(
            text = "Set LexaWall as your wallpaper for passive learning",
            style = LexaType.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
            color = mutedColor,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "×",
            style = LexaType.bodySmall,
            color = mutedColor,
            modifier = Modifier.clickable(onClick = onDismiss)
        )
    }
}

@Composable
private fun WordDropAvailableCard(
    accentColor: Color,
    dimAccent: Color,
    backgroundTop: Color,
    backgroundBottom: Color,
    onStart: () -> Unit
) {
    val gradientStart = lerp(backgroundTop, dimAccent, 0.55f)
    val gradientEnd = lerp(backgroundBottom, accentColor, 0.35f)
    val cardBrush = Brush.linearGradient(
        listOf(
            gradientStart,
            gradientEnd
        )
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBrush)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopEnd)
                .offset(x = 48.dp, y = (-44).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(999.dp)
                )
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .fillMaxWidth()
                .height(140.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "TODAY'S WORD DROP",
                    style = LexaType.overlineSmall.copy(
                        fontFamily = DmMono,
                        fontSize = 10.sp,
                        letterSpacing = 1.5.sp
                    ),
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "12 Questions",
                    style = LexaType.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "~4 minutes",
                    style = LexaType.bodySmall.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = accentColor
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "Start Word Drop",
                        style = LexaType.labelSmall.copy(
                            fontFamily = DmMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(Modifier.size(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ResumeWordDropCard(
    accentColor: Color,
    dimAccent: Color,
    onContinue: () -> Unit
) {
    val cardBrush = Brush.verticalGradient(
        listOf(
            dimAccent.copy(alpha = 0.78f),
            dimAccent.copy(alpha = 0.45f)
        )
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBrush)
            .border(1.dp, accentColor.copy(alpha = 0.28f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Text(
            text = "WORD DROP IN PROGRESS",
            style = LexaType.overlineSmall.copy(fontFamily = DmMono),
            color = Color.White.copy(alpha = 0.72f)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Continue your Word Drop",
            style = LexaType.titleMedium.copy(fontFamily = PlayfairDisplay),
            color = Color.White
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Pick up where you stopped.",
            style = LexaType.bodySmall,
            color = Color.White.copy(alpha = 0.62f)
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier.align(Alignment.End),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF8F8FA),
                contentColor = Color(0xFF1A1A22)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = "Continue",
                style = LexaType.labelSmall.copy(fontFamily = DmMono)
            )
            Spacer(Modifier.size(6.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun WordDropCompletedCard(accentColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LexaColors.SurfaceDark.copy(alpha = 0.9f))
            .border(1.dp, accentColor.copy(alpha = 0.28f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Check, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Done for today.",
            style = LexaType.titleMedium.copy(fontFamily = PlayfairDisplay),
            color = LexaColors.TextPrimary
        )
        Text(
            text = "Come back tomorrow for a new Word Drop.",
            style = LexaType.bodySmall,
            color = LexaColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RedemptionAvailableCard(
    streakDays: Int,
    onRestoreStreak: () -> Unit,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LexaColors.SurfaceDark.copy(alpha = 0.9f))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Text(
            text = "🔥 YOUR STREAK IS AT RISK",
            style = LexaType.overlineSmall.copy(fontFamily = DmMono),
            color = accentColor
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "🔥 $streakDays",
            style = LexaType.wordDisplay.copy(fontFamily = PlayfairDisplay, fontSize = 36.sp),
            color = accentColor
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Answer 5 questions to restore it.",
            style = LexaType.bodySmall,
            color = LexaColors.TextSecondary
        )
        Spacer(Modifier.height(12.dp))
        PrimaryButton(
            text = "Restore streak →",
            onClick = onRestoreStreak,
            showArrow = false
        )
    }
}

@Composable
private fun WallpaperWordCard(
    word: Word,
    accentColor: Color,
    dimAccent: Color,
    surfaceColor: Color,
    backgroundTop: Color,
    backgroundBottom: Color,
    showQuizAnchorTag: Boolean,
    onClick: () -> Unit
) {
    val gradientStart = lerp(backgroundTop, dimAccent, 0.46f)
    val gradientEnd = lerp(backgroundBottom, surfaceColor, 0.62f)
    val cardBrush = Brush.verticalGradient(
        colors = listOf(
            gradientStart,
            gradientEnd
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBrush)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 22.dp)
            .height(300.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "[${word.partOfSpeech.lowercase().take(3)}.]",
                style = LexaType.monoSmall.copy(fontFamily = DmMono, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                color = accentColor
            )
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TinyChip(text = word.partOfSpeech.lowercase())
                TinyChip(text = word.level.lowercase())
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = word.text,
            style = LexaType.wordDisplay.copy(
                fontFamily = PlayfairDisplay,
                fontSize = 48.sp,
                lineHeight = 58.sp,
                fontWeight = FontWeight.Normal
            ),
            color = Color.White
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = word.definition,
            style = LexaType.bodyMedium.copy(fontSize = 18.sp, lineHeight = 29.sp, fontWeight = FontWeight.Medium),
            color = LexaColors.TextSecondary
        )

        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.10f))
        )
        Spacer(Modifier.height(20.dp))

        if (showQuizAnchorTag) {
            Text(
                text = "TODAY'S WORD DROP STARTS HERE  ↓",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = LexaType.overlineSmall.copy(fontFamily = DmMono, fontSize = 10.sp, letterSpacing = 1.5.sp),
                color = LexaColors.TextMuted
            )
        } else {
            Text(
                text = "IN YOUR WALLPAPER NOW",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = LexaType.overlineSmall.copy(fontFamily = DmMono),
                color = LexaColors.TextMuted
            )
        }
    }
}

@Composable
private fun TinyChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = LexaType.overlineSmall.copy(fontFamily = DmMono, fontSize = 10.sp),
            color = Color.White.copy(alpha = 0.70f)
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomePreview() {
    LexaWallTheme {
        HomeContent(
            uiState = HomeUiState(
                greeting = "GOOD MORNING",
                screenState = HomeScreenState.DEFAULT,
                wallpaperWord = Word(
                    id = "1",
                    text = "Ephemeral",
                    partOfSpeech = "Adjective",
                    level = "Intermediate",
                    definition = "lasting for only a very short time",
                    exampleSentence = "",
                    pronunciation = "",
                    etymology = ""
                ),
                stats = UserStats(streakDays = 14, wordsMastered = 47, quizScore = 9, quizTotal = 12),
                currentTitle = "The Curious",
                dailyProgress = DailyProgressSnapshot(
                    wordsLearnedToday = 3,
                    xpEarnedToday = 120,
                    currentStreak = 14,
                    currentLevel = 5,
                    currentTitle = "The Reader"
                ),
                isLoading = false,
                showWallpaperBanner = true
            ),
            wallpaperCardWord = Word(
                id = "1",
                text = "Ephemeral",
                partOfSpeech = "Adjective",
                level = "Intermediate",
                definition = "lasting for only a very short time",
                exampleSentence = "",
                pronunciation = "",
                etymology = ""
            ),
            onStartQuiz = {},
            onWordTap = {},
            onRestoreStreak = {},
            onDismissBanner = {}
        )
    }
}
