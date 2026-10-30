package com.lexawall.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lexawall.core.components.GoldDivider
import com.lexawall.core.components.OverlineTag
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.components.SmallTag
import com.lexawall.core.components.StreakBadge
import com.lexawall.core.ui.DmMono
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LexaWallTheme
import com.lexawall.core.ui.PlayfairDisplay
import com.lexawall.domain.model.UserStats
import com.lexawall.domain.model.Word

@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onStartQuiz: () -> Unit,
    onOpenFlashcards: () -> Unit,
    onWordTap: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreenContent(
        uiState = uiState,
        onNavigateToSettings = onNavigateToSettings,
        onStartQuiz = onStartQuiz,
        onOpenFlashcards = onOpenFlashcards,
        onWordTap = onWordTap,
        onCycleState = viewModel::cycleState,
        onDismissBanner = viewModel::dismissBanner,
    )
}

@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onNavigateToSettings: () -> Unit,
    onStartQuiz: () -> Unit,
    onOpenFlashcards: () -> Unit,
    onWordTap: (String) -> Unit,
    onCycleState: () -> Unit,
    onDismissBanner: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexaColors.BackgroundDark)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                OverlineTag(
                    text = uiState.greeting,
                    color = LexaColors.Primary
                )
                if (uiState.screenState != HomeScreenState.DAY_1) {
                    Spacer(Modifier.height(4.dp))
                    StreakBadge(days = uiState.stats.streakDays)
                }
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = LexaColors.TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Wallpaper not set banner
            if (uiState.showWallpaperBanner) {
                WallpaperBanner(onDismiss = onDismissBanner)
                Spacer(Modifier.height(16.dp))
            }

            // Welcome back headline
            if (uiState.screenState == HomeScreenState.WELCOME_BACK) {
                Text(
                    text = "Welcome back.",
                    style = LexaType.headlineLarge,
                    color = LexaColors.TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Your words are waiting.",
                    style = LexaType.bodyLarge,
                    color = LexaColors.TextSecondary
                )
                Spacer(Modifier.height(24.dp))
            }

            // Quiz / Status card based on state
            when (uiState.screenState) {
                HomeScreenState.DEFAULT, HomeScreenState.WALLPAPER_NOT_SET, HomeScreenState.WELCOME_BACK ->
                    QuizAvailableCard(onStartQuiz = onStartQuiz)
                HomeScreenState.QUIZ_COMPLETED ->
                    QuizCompletedCard()
                HomeScreenState.RESUME_QUIZ ->
                    ResumeQuizCard(onContinue = onStartQuiz)
                HomeScreenState.DAY_1 ->
                    Day1QuizCard(onStartQuiz = onStartQuiz)
            }

            Spacer(Modifier.height(20.dp))

            // Wallpaper word card
            WallpaperWordCard(
                word = uiState.wallpaperWord,
                onClick = { onWordTap(uiState.wallpaperWord.id) }
            )

            Spacer(Modifier.height(20.dp))

            // Flashcards shortcut (hidden on DAY_1)
            if (uiState.screenState != HomeScreenState.DAY_1) {
                TextButton(onClick = onOpenFlashcards) {
                    Text(
                        text = "Practice flashcards →",
                        style = LexaType.labelMedium,
                        color = LexaColors.Primary
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // Demo cycle state button
            TextButton(
                onClick = onCycleState,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Demo: cycle state → (${uiState.screenState.name})",
                    style = LexaType.monoSmall,
                    color = LexaColors.TextMuted
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── Quiz Available Card ───

@Composable
private fun QuizAvailableCard(onStartQuiz: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LexaColors.CardDark)
            .border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        OverlineTag(text = "TODAY'S QUIZ")
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Daily Vocabulary",
            style = LexaType.headlineLarge.copy(fontFamily = PlayfairDisplay),
            color = LexaColors.TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "12 questions · ~4 minutes",
            style = LexaType.bodyMedium,
            color = LexaColors.TextMuted
        )
        Spacer(Modifier.height(20.dp))
        PrimaryButton(
            text = "Start quiz",
            onClick = onStartQuiz,
            showArrow = true
        )
    }
}

// ─── Quiz Completed Card ───

@Composable
private fun QuizCompletedCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LexaColors.CardDark)
            .border(1.dp, LexaColors.Success.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(LexaColors.Success.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = LexaColors.Success,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Done for today.",
            style = LexaType.headlineSmall,
            color = LexaColors.Success,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "See you tomorrow.",
            style = LexaType.bodyMedium,
            color = LexaColors.TextMuted,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Resume Quiz Card ───

@Composable
private fun ResumeQuizCard(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LexaColors.CardDark)
            .border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Text(
            text = "Continue your quiz?",
            style = LexaType.titleMedium,
            color = LexaColors.TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Question 6 of 12",
            style = LexaType.bodyMedium,
            color = LexaColors.TextMuted
        )
        Spacer(Modifier.height(20.dp))
        PrimaryButton(
            text = "Continue",
            onClick = onContinue
        )
    }
}

// ─── Day 1 Quiz Card ───

@Composable
private fun Day1QuizCard(onStartQuiz: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LexaColors.CardDark)
            .border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        OverlineTag(text = "TODAY'S QUIZ")
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Your first quiz",
            style = LexaType.headlineLarge.copy(fontFamily = PlayfairDisplay),
            color = LexaColors.TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "12 questions · ~4 minutes",
            style = LexaType.bodyMedium,
            color = LexaColors.TextMuted
        )
        Spacer(Modifier.height(20.dp))
        PrimaryButton(
            text = "Start quiz",
            onClick = onStartQuiz,
            showArrow = true
        )
    }
}

// ─── Wallpaper Word Card ───

@Composable
private fun WallpaperWordCard(
    word: Word,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LexaColors.CardDark)
            .border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OverlineTag(
                text = "ON YOUR WALLPAPER NOW",
                color = LexaColors.TextMuted
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = word.text,
            style = LexaType.wordDisplay.copy(fontSize = 40.sp),
            color = LexaColors.TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "[${word.partOfSpeech.lowercase().take(3)}.]",
                style = LexaType.monoSmall,
                color = LexaColors.Primary
            )
            Text(
                text = word.definition.lowercase(),
                style = LexaType.bodyMedium,
                color = LexaColors.TextMuted
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallTag(text = word.partOfSpeech)
            SmallTag(text = word.level)
        }
    }
}

// ─── Wallpaper Not Set Banner ───

@Composable
private fun WallpaperBanner(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(LexaColors.Primary.copy(alpha = 0.15f))
            .border(1.dp, LexaColors.Primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Set LexaWall as your wallpaper to learn passively. Set now →",
            style = LexaType.bodySmall,
            color = LexaColors.Primary,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = LexaColors.Primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenPreview() {
    LexaWallTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                greeting = "GOOD MORNING",
                screenState = HomeScreenState.DEFAULT,
                wallpaperWord = Word(
                    id = "ephemeral", text = "ephemeral", partOfSpeech = "Adjective",
                    level = "B2", definition = "Lasting for a very short time.",
                    exampleSentence = "Fashions are ephemeral, changing with every season."
                ),
                stats = UserStats(streakDays = 12, wordsMastered = 42, quizScore = 11, quizTotal = 12)
            ),
            onNavigateToSettings = {},
            onStartQuiz = {},
            onOpenFlashcards = {},
            onWordTap = {},
            onCycleState = {},
            onDismissBanner = {},
        )
    }
}

