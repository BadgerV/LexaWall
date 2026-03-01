package com.lexawall.feature.flashcards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.*
import com.lexawall.core.ui.*
import com.lexawall.data.local.DatabaseModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

@Composable
fun FlashcardsSessionSummaryScreen(onStartQuiz: () -> Unit, onBackToHome: () -> Unit) {
    val context = LocalContext.current
    val todayQuizDone by produceState(initialValue = false) {
        value = withContext(Dispatchers.IO) {
            val today = LocalDate.now().toString()
            val db = DatabaseModule.getInstance(context)
            val recentQuiz = db.quizSessionDao().getRecentSessions(1).firstOrNull()
            recentQuiz?.datestamp == today
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(LexaColors.BackgroundDark),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            Modifier.weight(1f).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(LexaColors.Success.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    null,
                    tint = LexaColors.Success,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "Nice review.",
                style = LexaType.headlineLarge.copy(
                    fontFamily = PlayfairDisplay,
                    fontStyle = FontStyle.Italic
                ),
                color = LexaColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (todayQuizDone)
                    "You've already completed today's Word Drop. Come back tomorrow for more."
                else
                    "Your flashcards are settling in. Ready for today's Word Drop?",
                style = LexaType.bodyMedium.copy(fontSize = 15.sp),
                color = LexaColors.TextMuted,
                textAlign = TextAlign.Center
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!todayQuizDone) {
                PrimaryButton("Start today's Word Drop", onStartQuiz, showArrow = true)
            }
            GhostButton("Back to home", onBackToHome)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { FlashcardsSessionSummaryScreen({}, {}) } }
