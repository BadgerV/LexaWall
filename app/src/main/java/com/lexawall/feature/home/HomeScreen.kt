package com.lexawall.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lexawall.core.components.*
import com.lexawall.core.ui.*
import com.lexawall.domain.model.*

@Composable
fun HomeScreen(
    onStartQuiz: () -> Unit,
    onOpenFlashcards: () -> Unit,
    onWordTap: (String) -> Unit,
    onRestoreStreak: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentWallpaperWordEntity by viewModel.currentWallpaperWord.collectAsState()

    // Keep wallpaper word card in sync with actual wallpaper rotation
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshWallpaperWord()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val wallpaperCardWord = currentWallpaperWordEntity?.let { entity ->
        Word(
            id = entity.id,
            text = entity.word,
            partOfSpeech = entity.partOfSpeech,
            level = entity.cefrLevel,
            definition = entity.definition,
            exampleSentence = entity.exampleSentence
        )
    } ?: uiState.wallpaperWord
    HomeContent(
        uiState,
        wallpaperCardWord,
        onStartQuiz,
        onOpenFlashcards,
        onWordTap,
        onRestoreStreak,
        viewModel::dismissBanner
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    wallpaperCardWord: Word,
    onStartQuiz: () -> Unit,
    onFlashcards: () -> Unit,
    onWordTap: (String) -> Unit,
    onRestoreStreak: () -> Unit,
    onDismissBanner: () -> Unit
) {
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .background(LexaColors.BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = LexaColors.Primary)
        }
        return
    }

    val activeTheme = LocalLexaTheme.current
    val accentColor = Color(activeTheme.accentColor)
    val wallpaperCardTopColor = Color(activeTheme.backgroundTop)

    var wallpaperCardVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        wallpaperCardVisible = true
    }
    val wallpaperCardAlpha by animateFloatAsState(
        targetValue = if (wallpaperCardVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
        label = "wallpaperCardAlpha"
    )
    val wallpaperCardOffset by animateFloatAsState(
        targetValue = if (wallpaperCardVisible) 0f else 24f,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
        label = "wallpaperCardOffset"
    )

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(LexaColors.BackgroundDark)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                OverlineTag(uiState.greeting, color = accentColor)
                if (uiState.screenState != HomeScreenState.DAY_1) {
                    Spacer(Modifier.height(4.dp))
                    StreakBadge(uiState.stats.streakDays)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    uiState.currentTitle.uppercase(),
                    style = LexaType.monoSmall.copy(fontFamily = DmMono, fontSize = 11.sp),
                    color = Color(activeTheme.accentColorDim)
                )
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            if (uiState.showWallpaperBanner) { WallpaperBanner(onDismissBanner); Spacer(Modifier.height(16.dp)) }
            if (uiState.screenState == HomeScreenState.WELCOME_BACK) {
                Text("Welcome back.", style = LexaType.headlineLarge, color = LexaColors.TextPrimary); Spacer(Modifier.height(4.dp))
                Text("Your words are waiting.", style = LexaType.bodyLarge, color = LexaColors.TextSecondary); Spacer(Modifier.height(24.dp))
            }
            when (uiState.screenState) {
                HomeScreenState.DEFAULT, HomeScreenState.WALLPAPER_NOT_SET, HomeScreenState.WELCOME_BACK -> QuizAvailableCard(onStartQuiz)
                HomeScreenState.QUIZ_COMPLETED -> QuizCompletedCard()
                HomeScreenState.RESUME_QUIZ -> ResumeQuizCard(onStartQuiz)
                HomeScreenState.DAY_1 -> Day1QuizCard(onStartQuiz)
                HomeScreenState.REDEMPTION_AVAILABLE -> RedemptionAvailableCard(uiState.stats.streakDays, onRestoreStreak, accentColor)
            }
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier.graphicsLayer(
                    alpha = wallpaperCardAlpha,
                    translationY = with(androidx.compose.ui.platform.LocalDensity.current) { wallpaperCardOffset.dp.toPx() }
                )
            ) {
                WallpaperWordCard(
                    word = wallpaperCardWord,
                    accentColor = accentColor,
                    cardTopColor = wallpaperCardTopColor,
                    showQuizAnchorTag = uiState.screenState == HomeScreenState.DEFAULT
                ) {
                    onWordTap(wallpaperCardWord.id)
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable private fun RedemptionAvailableCard(streakDays: Int, onRestoreStreak: () -> Unit, accentColor: Color) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LexaColors.CardDark)
            .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        OverlineTag("🔥 YOUR STREAK IS AT RISK.", color = accentColor)
        Spacer(Modifier.height(10.dp))
        Text(
            "🔥 $streakDays",
            style = LexaType.wordDisplay.copy(fontFamily = PlayfairDisplay, fontSize = 40.sp),
            color = accentColor
        )
        Spacer(Modifier.height(8.dp))
        Text("Answer 5 questions to restore it.", style = LexaType.bodyMedium, color = LexaColors.TextMuted)
        Spacer(Modifier.height(18.dp))
        PrimaryButton("Restore streak →", onRestoreStreak)
    }
}

@Composable private fun QuizAvailableCard(onStart: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LexaColors.CardDark).border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(16.dp)).padding(24.dp)) {
        OverlineTag("TODAY'S WORD DROP"); Spacer(Modifier.height(8.dp))
        Text("Daily Vocabulary", style = LexaType.headlineLarge.copy(fontFamily = PlayfairDisplay), color = LexaColors.TextPrimary); Spacer(Modifier.height(8.dp))
        Text("12 questions · ~4 minutes", style = LexaType.bodyMedium, color = LexaColors.TextMuted); Spacer(Modifier.height(20.dp))
        PrimaryButton("Start Word Drop", onStart, showArrow = true)
    }
}

@Composable private fun QuizCompletedCard() {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LexaColors.CardDark).border(1.dp, LexaColors.Success.copy(alpha = 0.3f), RoundedCornerShape(16.dp)).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(LexaColors.Success.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, null, tint = LexaColors.Success, modifier = Modifier.size(24.dp)) }
        Spacer(Modifier.height(16.dp)); Text("Done for today.", style = LexaType.headlineSmall, color = LexaColors.Success, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp)); Text("See you tomorrow.", style = LexaType.bodyMedium, color = LexaColors.TextMuted, textAlign = TextAlign.Center)
    }
}

@Composable private fun ResumeQuizCard(onContinue: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LexaColors.CardDark).border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(16.dp)).padding(24.dp)) {
        Text("Continue your Word Drop?", style = LexaType.titleMedium, color = LexaColors.TextPrimary); Spacer(Modifier.height(4.dp))
        Text("Word Drop 6 of 12", style = LexaType.bodyMedium, color = LexaColors.TextMuted); Spacer(Modifier.height(20.dp))
        PrimaryButton("Continue", onContinue)
    }
}

@Composable private fun Day1QuizCard(onStart: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LexaColors.CardDark).border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(16.dp)).padding(24.dp)) {
        OverlineTag("TODAY'S WORD DROP"); Spacer(Modifier.height(8.dp))
        Text("Your first Word Drop", style = LexaType.headlineLarge.copy(fontFamily = PlayfairDisplay), color = LexaColors.TextPrimary); Spacer(Modifier.height(8.dp))
        Text("12 questions · ~4 minutes", style = LexaType.bodyMedium, color = LexaColors.TextMuted); Spacer(Modifier.height(20.dp))
        PrimaryButton("Start Word Drop", onStart, showArrow = true)
    }
}

@Composable
private fun WallpaperWordCard(
    word: Word,
    accentColor: Color,
    cardTopColor: Color,
    showQuizAnchorTag: Boolean,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(cardTopColor.copy(alpha = 0.32f), LexaColors.CardDark)
                )
            )
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(24.dp)
    ) {
        OverlineTag("ON YOUR WALLPAPER NOW", color = LexaColors.TextMuted); Spacer(Modifier.height(12.dp))
        Text(word.text, style = LexaType.wordDisplay.copy(fontSize = 40.sp), color = LexaColors.TextPrimary); Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("[${word.partOfSpeech.lowercase().take(3)}.]", style = LexaType.monoSmall, color = accentColor)
            Text(word.definition.lowercase(), style = LexaType.bodyMedium, color = LexaColors.TextMuted)
        }; Spacer(Modifier.height(12.dp))
        if (showQuizAnchorTag) {
            Text(
                "TODAY'S WORD DROP STARTS HERE",
                style = LexaType.overlineSmall.copy(fontFamily = DmMono),
                color = accentColor
            )
            Spacer(Modifier.height(10.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { SmallTag(word.partOfSpeech); SmallTag(word.level) }
    }
}

@Composable private fun WallpaperBanner(onDismiss: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(LexaColors.Primary.copy(alpha = 0.15f)).border(1.dp, LexaColors.Primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp)).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Set LexaWall as your wallpaper to learn passively. Set now →", style = LexaType.bodySmall, color = LexaColors.Primary, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, "Dismiss", tint = LexaColors.Primary, modifier = Modifier.size(16.dp)) }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { HomeContent(HomeUiState(greeting = "GOOD MORNING", screenState = HomeScreenState.DEFAULT, wallpaperWord = Word("ephemeral", "ephemeral", "Adjective", "B2", "Lasting for a very short time.", "Fashions are ephemeral, changing with every season."), stats = UserStats(12, 42, 11, 12)), Word("ephemeral", "ephemeral", "Adjective", "B2", "Lasting for a very short time.", "Fashions are ephemeral, changing with every season."), {}, {}, {}, {}, {}) } }
