package com.lexawall.feature.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.GhostButton
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.PlayfairDisplay
import com.lexawall.data.local.DatabaseModule
import com.lexawall.data.local.PrefsManager
import com.lexawall.data.local.entities.BonusChallengeSession
import com.lexawall.data.local.entities.LevelEntity
import com.lexawall.data.local.entities.WordEntity
import com.lexawall.domain.engine.BonusChallengeEngine
import com.lexawall.domain.engine.DailyWordPool
import com.lexawall.domain.engine.XpEngine
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class BonusChallengeQuestion(
    val word: WordEntity,
    val options: List<String>,
    val correctIndex: Int
)

@Composable
fun BonusChallengeScreen(
    challengeTypeRaw: String,
    onClose: () -> Unit,
    onCompleted: () -> Unit = onClose
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { DatabaseModule.getInstance(context) }
    val challengeType = remember(challengeTypeRaw) {
        BonusChallengeEngine.ChallengeType.fromName(challengeTypeRaw)
            ?: BonusChallengeEngine.ChallengeType.WEEKEND_DOUBLE_XP
    }

    var question by remember { mutableStateOf<BonusChallengeQuestion?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var resultCorrect by remember { mutableStateOf<Boolean?>(null) }
    var xpEarned by remember { mutableIntStateOf(0) }
    var speedRunSeconds by remember { mutableIntStateOf(5) }

    LaunchedEffect(challengeType) {
        isLoading = true
        errorText = null
        question = withContext(Dispatchers.IO) {
            buildChallengeQuestion(db, context, challengeType)
        }
        if (question == null) {
            errorText = "No challenge question is available right now."
        }
        isLoading = false
    }

    LaunchedEffect(challengeType, question?.word?.id, selectedIndex) {
        if (challengeType != BonusChallengeEngine.ChallengeType.SPEED_RUN) return@LaunchedEffect
        if (question == null || selectedIndex != null || resultCorrect != null) return@LaunchedEffect

        speedRunSeconds = 5
        while (speedRunSeconds > 0 && selectedIndex == null && resultCorrect == null) {
            delay(1000)
            speedRunSeconds -= 1
        }

        if (speedRunSeconds == 0 && selectedIndex == null && resultCorrect == null) {
            scope.launch {
                val awarded = completeChallenge(
                    db = db,
                    context = context,
                    challengeType = challengeType,
                    wasCorrect = false
                )
                xpEarned = awarded
                selectedIndex = -1
                resultCorrect = false
            }
        }
    }

    fun onSelect(index: Int) {
        val q = question ?: return
        if (selectedIndex != null) return
        selectedIndex = index
        val wasCorrect = index == q.correctIndex
        scope.launch {
            val awarded = completeChallenge(
                db = db,
                context = context,
                challengeType = challengeType,
                wasCorrect = wasCorrect
            )
            xpEarned = awarded
            resultCorrect = wasCorrect
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(LexaColors.BackgroundDark)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = LexaColors.TextSecondary
                )
            }
            Text(
                text = "Challenge",
                style = LexaType.titleMedium,
                color = LexaColors.TextSecondary
            )
            Spacer(Modifier.size(48.dp))
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF2A1E08), Color(0xFF140E06))
                    )
                )
                .padding(18.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = challengeType.title,
                    style = LexaType.headlineLarge.copy(
                        fontFamily = PlayfairDisplay,
                        fontSize = 24.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = challengeType.subtitle,
                    style = LexaType.bodyLarge,
                    color = Color(0xFFFFD700),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LexaColors.Primary)
            }
        } else if (!errorText.isNullOrBlank()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = errorText.orEmpty(),
                    style = LexaType.bodyLarge,
                    color = LexaColors.TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val currentQuestion = question
            if (currentQuestion != null) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = currentQuestion.word.word,
                        style = LexaType.wordDisplay.copy(
                            fontFamily = PlayfairDisplay,
                            fontStyle = FontStyle.Italic,
                            fontSize = 42.sp
                        ),
                        color = LexaColors.TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (challengeType == BonusChallengeEngine.ChallengeType.SPEED_RUN && resultCorrect == null) {
                        Text(
                            text = speedRunSeconds.toString(),
                            style = LexaType.scoreDisplay.copy(fontSize = 48.sp),
                            color = if (speedRunSeconds <= 2) Color(0xFFFF6B35) else LexaColors.Primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    currentQuestion.options.forEachIndexed { index, option ->
                        val answered = resultCorrect != null
                        val isCorrectOption = index == currentQuestion.correctIndex
                        val selected = selectedIndex == index
                        val rowColor = when {
                            answered && isCorrectOption -> Color(0xFFFFD700)
                            answered && selected && !isCorrectOption -> Color(0xFFFF9800)
                            else -> LexaColors.SurfaceDark
                        }
                        val textColor = if (answered && (isCorrectOption || selected)) Color.Black else LexaColors.TextPrimary

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(rowColor)
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                                .clickable(enabled = !answered) { onSelect(index) }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = option,
                                style = LexaType.bodyMedium,
                                color = textColor
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = resultCorrect != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = if (resultCorrect == true) {
                                    when (challengeType) {
                                        BonusChallengeEngine.ChallengeType.STORM -> "⚡ Electrifying! +${xpEarned} XP!"
                                        else -> "Challenge complete! +${xpEarned} XP"
                                    }
                                } else {
                                    "⚡ Keep learning! +${xpEarned} XP"
                                },
                                style = LexaType.titleMedium,
                                color = if (resultCorrect == true) LexaColors.Primary else LexaColors.TextMuted,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "1/1 Challenge Completed Today",
                                style = LexaType.bodySmall,
                                color = LexaColors.TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (resultCorrect != null) {
            PrimaryButton("Continue", onCompleted)
        } else {
            GhostButton("Close", onClose)
        }
    }
}

private suspend fun buildChallengeQuestion(
    db: com.lexawall.data.local.LexaWallDatabase,
    context: android.content.Context,
    challengeType: BonusChallengeEngine.ChallengeType
): BonusChallengeQuestion? {
    val allWords = db.wordDao().getAllWords()
    if (allWords.size < 4) return null

    val targetWord = when (challengeType) {
        BonusChallengeEngine.ChallengeType.STORM -> {
            allWords.firstOrNull { it.cefrLevel.equals("C1", ignoreCase = true) }
                ?: allWords.randomOrNull()
        }
        else -> {
            val daily = DailyWordPool.getWordDropWords(context, db.wordDao())
            daily.randomOrNull() ?: allWords.randomOrNull()
        }
    } ?: return null

    val correctDefinition = targetWord.definition.trim()
    if (correctDefinition.isBlank()) return null

    val distractors = allWords
        .asSequence()
        .filter { it.id != targetWord.id }
        .map { it.definition.trim() }
        .filter { it.isNotBlank() && !it.equals(correctDefinition, ignoreCase = true) }
        .distinct()
        .toList()
        .shuffled(Random.Default)
        .take(3)

    if (distractors.size < 3) return null

    val options = (distractors + correctDefinition).shuffled(Random.Default)
    val correctIndex = options.indexOf(correctDefinition)
    if (correctIndex < 0) return null

    return BonusChallengeQuestion(
        word = targetWord,
        options = options,
        correctIndex = correctIndex
    )
}

private suspend fun completeChallenge(
    db: com.lexawall.data.local.LexaWallDatabase,
    context: android.content.Context,
    challengeType: BonusChallengeEngine.ChallengeType,
    wasCorrect: Boolean
): Int {
    val xp = if (wasCorrect) challengeType.rewardXp else 1
    withContext(Dispatchers.IO) {
        db.bonusChallengeDao().insertSession(
            BonusChallengeSession(
                timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                challengeType = challengeType.name,
                completed = true,
                xpEarned = xp
            )
        )

        val levelDao = db.levelDao()
        val level = runCatching { levelDao.getLevel() }
            .getOrElse {
                val fallback = LevelEntity()
                levelDao.insertOrReplace(fallback)
                fallback
            }
        val newCurrentXp = level.currentXp + xp
        val newTotalXp = level.totalXpEarned + xp
        val newDef = XpEngine.getLevelForXp(newCurrentXp)
        levelDao.updateLevel(
            level.copy(
                currentXp = newCurrentXp,
                totalXpEarned = newTotalXp,
                currentLevel = newDef.level,
                title = newDef.title
            )
        )

        PrefsManager.saveLastBonusChallengeCompletedDate(context, LocalDate.now().toString())
    }
    return xp
}

private val TIMESTAMP_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
