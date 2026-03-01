package com.lexawall.feature.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lexawall.core.ai.LexaTrigger
import com.lexawall.core.ai.LexaVoice
import com.lexawall.data.local.DatabaseModule
import com.lexawall.data.local.entities.BadgeEntity
import com.lexawall.data.local.entities.LevelEntity
import com.lexawall.data.local.entities.QuizSessionEntity
import com.lexawall.data.local.entities.WordEntity
import com.lexawall.domain.engine.XpEngine
import com.lexawall.utils.LexaWallLogger
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

sealed interface ProgressUiState {
    data object Loading : ProgressUiState

    data class Success(
        val levelEntity: LevelEntity = LevelEntity(),
        val xpToNextLevel: Int = 0,
        val progressFraction: Float = 0f,
        val currentStreak: Int = 0,
        val longestStreak: Int = 0,
        val totalMastered: Int = 0,
        val quizAccuracyPercent: Int = 0,
        val masteredWords: List<WordEntity> = emptyList(),
        val badges: List<BadgeEntity> = emptyList(),
        val lexaMessage: String = "",
        val weeklyXp: List<Int> = List(7) { 0 }
    ) : ProgressUiState

    data class Error(
        val message: String = "Unable to load progress right now."
    ) : ProgressUiState
}

class ProgressViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val db = DatabaseModule.getInstance(appContext)

    private val _uiState = MutableStateFlow<ProgressUiState>(ProgressUiState.Loading)
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val levelEntity = db.levelDao().getLevel()
                val xpToNextLevel = XpEngine.getXpToNextLevel(levelEntity.currentXp)
                val progressFraction = XpEngine.getProgressFraction(levelEntity.currentXp)

                val streak = db.streakDao().getStreak()
                val currentStreak = streak?.currentStreak ?: 0

                val sessions = db.quizSessionDao().getAllSessions()
                val totalCorrect = sessions.sumOf { it.score }
                val totalAnswers = sessions.sumOf { it.total }
                val quizAccuracyPercent = if (totalAnswers > 0) {
                    ((totalCorrect.toFloat() / totalAnswers.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
                } else {
                    0
                }

                val longestStreak = computeLongestStreak(sessions.map { it.datestamp })
                val masteredProgress = db.userProgressDao().getWordsByState("MASTERED")
                val masteredWords = masteredProgress
                    .sortedByDescending { it.lastAnsweredDate ?: "" }
                    .mapNotNull { progress -> db.wordDao().getWordById(progress.wordId) }

                val badges = db.badgeDao().getAllBadges()
                val weeklyXp = computeWeeklyXp(sessions)

                val masteredWordStrings = masteredWords.map { it.word }
                val lexaMessage = LexaVoice.generateMessage(
                    appContext,
                    LexaTrigger.OnProgressViewed(
                        title = levelEntity.title,
                        masteredWords = masteredWordStrings
                    ),
                    speakAloud = false
                ).text

                val successState = ProgressUiState.Success(
                    levelEntity = levelEntity,
                    xpToNextLevel = xpToNextLevel,
                    progressFraction = progressFraction,
                    currentStreak = currentStreak,
                    longestStreak = longestStreak,
                    totalMastered = masteredWords.size,
                    quizAccuracyPercent = quizAccuracyPercent,
                    masteredWords = masteredWords,
                    badges = badges,
                    lexaMessage = lexaMessage,
                    weeklyXp = weeklyXp
                )

                withContext(Dispatchers.Main) {
                    _uiState.value = successState
                }
            } catch (e: Exception) {
                LexaWallLogger.e("LexaWall", "Failed to load progress state", e)
                withContext(Dispatchers.Main) {
                    _uiState.value = ProgressUiState.Error()
                }
            }
        }
    }

    private fun computeWeeklyXp(sessions: List<QuizSessionEntity>): List<Int> {
        val today = LocalDate.now()
        val xpByDate = sessions.groupBy { it.datestamp }.mapValues { (_, entries) ->
            entries.sumOf { it.xpEarned }
        }
        return (6 downTo 0).map { offset ->
            val day = today.minusDays(offset.toLong()).toString()
            xpByDate[day] ?: 0
        }
    }

    private fun computeLongestStreak(datestamps: List<String>): Int {
        val dates = datestamps
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
            .distinct()
            .sorted()

        if (dates.isEmpty()) return 0

        var best = 1
        var current = 1
        for (i in 1 until dates.size) {
            val previous = dates[i - 1]
            val currentDate = dates[i]
            if (previous.plusDays(1) == currentDate) {
                current += 1
            } else {
                current = 1
            }
            if (current > best) {
                best = current
            }
        }
        return best
    }
}
