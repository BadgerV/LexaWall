package com.lexawall.feature.quiz

import android.app.Application
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lexawall.core.ai.LexaMessage
import com.lexawall.core.ai.LexaTrigger
import com.lexawall.core.ai.LexaVoice
import com.lexawall.data.local.DatabaseModule
import com.lexawall.data.local.entities.BadgeEntity
import com.lexawall.data.local.entities.LevelEntity
import com.lexawall.data.local.entities.QuizSessionEntity
import com.lexawall.data.local.entities.StreakEntity
import com.lexawall.data.local.entities.UserProgressEntity
import com.lexawall.data.local.entities.WallpaperQuizSession
import com.lexawall.data.local.entities.WordEntity
import com.lexawall.domain.engine.SpacedRepetitionEngine
import com.lexawall.domain.engine.StreakEngine
import com.lexawall.domain.engine.BonusChallengeEngine
import com.lexawall.domain.engine.XpBadges
import com.lexawall.domain.engine.XpEngine
import com.lexawall.domain.model.QuizOptionState
import com.lexawall.domain.repository.QuizRepository
import com.lexawall.notifications.NotificationScheduler
import com.lexawall.utils.LexaWallLogger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Collections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LevelUpEvent(
    val newDefinition: XpEngine.LevelDefinition
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val database = DatabaseModule.getInstance(appContext)
    private val quizRepository: QuizRepository = DatabaseModule.provideQuizRepository(appContext)
    private val spacedRepetitionEngine = SpacedRepetitionEngine()
    private val streakEngine = StreakEngine()
    private val bonusChallengeEngine = BonusChallengeEngine(appContext)
    private val prefs by lazy {
        appContext.getSharedPreferences("lexawall_prefs", Context.MODE_PRIVATE)
    }
    private val wrongAnswerCounts = Collections.synchronizedMap(mutableMapOf<String, Int>())
    private val graduatedThisSession = Collections.synchronizedList(mutableListOf<WordEntity>())

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _lexaMessage = MutableStateFlow<LexaMessage?>(null)
    val lexaMessage: StateFlow<LexaMessage?> = _lexaMessage.asStateFlow()

    private val _levelUpEvent = MutableStateFlow<LevelUpEvent?>(null)
    val levelUpEvent: StateFlow<LevelUpEvent?> = _levelUpEvent.asStateFlow()

    private val _correctAnswers = MutableStateFlow(0)
    val correctAnswers: StateFlow<Int> = _correctAnswers.asStateFlow()

    private val _lastSessionXpEarned = MutableStateFlow(0)
    val lastSessionXpEarned: StateFlow<Int> = _lastSessionXpEarned.asStateFlow()
    private val _lastSessionStreak = MutableStateFlow(0)
    val lastSessionStreak: StateFlow<Int> = _lastSessionStreak.asStateFlow()

    init {
        viewModelScope.launch {
            refreshQuestions(force = true)
        }
    }

    suspend fun refreshQuestions(force: Boolean = false) {
        withContext(Dispatchers.IO) {
            if (!force && _uiState.value.questions.isNotEmpty()) return@withContext

            val questions = quizRepository.getQuizQuestions()
            _uiState.update {
                it.copy(
                    questions = questions,
                    currentIndex = 0,
                    selectedOption = null,
                    optionStates = List(questions.firstOrNull()?.options?.size ?: 3) {
                        QuizOptionState.Default
                    }
                )
            }
        }
    }

    fun getQuestion(index: Int) = _uiState.value.questions.getOrNull(
        index.coerceIn(0, _uiState.value.questions.lastIndex.coerceAtLeast(0))
    )

    fun getTotalQuestions() = _uiState.value.questions.size

    fun isLastQuestion(index: Int) = index >= _uiState.value.questions.lastIndex

    fun resetQuizSession() {
        wrongAnswerCounts.clear()
        graduatedThisSession.clear()
        _correctAnswers.value = 0
        _uiState.update { it.copy(score = 0) }
    }

    fun getScore() = _uiState.value.score

    fun clearLevelUpEvent() {
        _levelUpEvent.value = null
    }

    fun recordAnswer(wordId: String, wasCorrect: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            LexaWallLogger.d("Score", "recordAnswer called, correct=$wasCorrect, total=${_correctAnswers.value}")
            val existing = database.userProgressDao().getProgressByWordId(wordId)
                ?: UserProgressEntity(
                    wordId = wordId,
                    state = "NEW",
                    intervalDays = 1,
                    correctStreak = 0,
                    lastAnsweredDate = null
                )

            val wordEntity = database.wordDao().getWordById(wordId)
            val seenProgress = spacedRepetitionEngine.advanceToSeen(existing)

            val progressed = if (wasCorrect) {
                spacedRepetitionEngine.advanceWord(
                    progress = seenProgress,
                    cefrLevel = wordEntity?.cefrLevel
                )
            } else {
                spacedRepetitionEngine.resetWord(seenProgress)
            }

            val updated = progressed.copy(lastAnsweredDate = LocalDate.now().toString())
            database.userProgressDao().insertOrReplace(updated)

            if (wasCorrect) {
                _correctAnswers.update { it + 1 }
                _uiState.update { it.copy(score = it.score + 1) }
            }

            if (!wasCorrect) {
                val current = wrongAnswerCounts[wordId] ?: 0
                wrongAnswerCounts[wordId] = current + 1
            }

            if (wasCorrect && spacedRepetitionEngine.isGraduated(updated)) {
                prefs.edit().putString("pending_graduation_word_id", wordId).apply()
                if (wordEntity != null && graduatedThisSession.none { it.id == wordEntity.id }) {
                    graduatedThisSession.add(wordEntity)
                }
            }
        }
    }

    fun recordWallpaperQuizAnswer(wordId: String, wasCorrect: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val xpEarned = if (wasCorrect) 15 else 1

            val currentLevel = runCatching { database.levelDao().getLevel() }
                .getOrElse {
                    val fallback = LevelEntity()
                    database.levelDao().insertOrReplace(fallback)
                    fallback
                }

            val oldLevel = XpEngine.getLevelForXp(currentLevel.currentXp)
            val newCurrentXp = currentLevel.currentXp + xpEarned
            val newLevel = XpEngine.getLevelForXp(newCurrentXp)

            database.levelDao().updateLevel(
                currentLevel.copy(
                    currentXp = newCurrentXp,
                    totalXpEarned = currentLevel.totalXpEarned + xpEarned,
                    currentLevel = newLevel.level,
                    title = newLevel.title
                )
            )

            database.wallpaperQuizDao().insertSession(
                WallpaperQuizSession(
                    timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                    wordId = wordId,
                    correct = wasCorrect,
                    xpEarned = xpEarned
                )
            )

            if (newLevel.level > oldLevel.level) {
                _levelUpEvent.value = LevelUpEvent(newLevel)
            }
        }
    }

    suspend fun completeQuiz(score: Int, total: Int): Int = withContext(Dispatchers.IO) {
        LexaWallLogger.d("Streak", "completeQuiz called, score=$score")
        val today = LocalDate.now().toString()

        val currentStreak = database.streakDao().getStreak()

        LexaWallLogger.d("Streak", "Before: streak=${currentStreak.currentStreak}")
        val streakResult = streakEngine.recordCompletion(currentStreak, today)
        database.streakDao().insertOrReplace(streakResult.streak)
        LexaWallLogger.d("Streak", "After: streak=${streakResult.streak.currentStreak}")

        val quizXp = XpEngine.calculateQuizXp(score, total)
        val masteryXp = graduatedThisSession.size * XpEngine.calculateMasteryXp()
        val streakMilestoneXp = if (streakResult.streakMilestoneReached) {
            XpEngine.calculateStreakMilestoneXp()
        } else {
            0
        }
        val totalSessionXp = quizXp + masteryXp + streakMilestoneXp

        database.quizSessionDao().insertSession(
            QuizSessionEntity(
                datestamp = today,
                score = score,
                total = total,
                xpEarned = totalSessionXp
            )
        )

        val currentLevelEntity = runCatching { database.levelDao().getLevel() }
            .getOrElse {
                val fallback = LevelEntity()
                database.levelDao().insertOrReplace(fallback)
                fallback
            }

        val previousLevelDefinition = XpEngine.getLevelForXp(currentLevelEntity.currentXp)
        val newCurrentXp = currentLevelEntity.currentXp + totalSessionXp
        val newTotalXp = currentLevelEntity.totalXpEarned + totalSessionXp
        val newLevelDefinition = XpEngine.getLevelForXp(newCurrentXp)

        database.levelDao().updateLevel(
            currentLevelEntity.copy(
                currentXp = newCurrentXp,
                totalXpEarned = newTotalXp,
                currentLevel = newLevelDefinition.level,
                title = newLevelDefinition.title
            )
        )

        if (newLevelDefinition.level > previousLevelDefinition.level) {
            _levelUpEvent.value = LevelUpEvent(newLevelDefinition)
        }

        val totalMastered = database.userProgressDao().getWordsByState("MASTERED").size
        if (totalMastered >= 1) awardBadgeIfMissing(XpBadges.FIRST_WORD)
        if (totalMastered >= 10) awardBadgeIfMissing(XpBadges.WORDS_10)
        if (totalMastered >= 50) awardBadgeIfMissing(XpBadges.WORDS_50)
        if (totalMastered >= 100) awardBadgeIfMissing(XpBadges.WORDS_100)

        if (total > 0 && score == total) {
            awardBadgeIfMissing(XpBadges.PERFECT_QUIZ)
        }

        if (streakResult.streak.currentStreak == 7) {
            awardBadgeIfMissing(XpBadges.STREAK_7)
        }
        if (streakResult.streak.currentStreak == 30) {
            awardBadgeIfMissing(XpBadges.STREAK_30)
        }

        if (previousLevelDefinition.level < 5 && newLevelDefinition.level >= 5) {
            awardBadgeIfMissing(XpBadges.LEVEL_5)
        }
        if (previousLevelDefinition.level < 10 && newLevelDefinition.level >= 10) {
            awardBadgeIfMissing(XpBadges.LEVEL_10)
        }

        val hardestWordId = wrongAnswerCounts.maxByOrNull { it.value }?.key
        var hardestWordText = _uiState.value.questions.firstOrNull()?.word?.text.orEmpty()
        if (hardestWordId != null) {
            val hardestWord = database.wordDao().getWordById(hardestWordId)
            if (hardestWord != null) {
                hardestWordText = hardestWord.word
                if (!NotificationManagerCompat.from(appContext).areNotificationsEnabled()) {
                    LexaWallLogger.w("LexaWall", "Notifications disabled; skipping post-word-drop schedule")
                } else {
                    LexaWallLogger.d("LexaWall", "Scheduling notification for word: ${hardestWord.word}")
                    NotificationScheduler.schedulePostQuizNotification(
                        context = appContext,
                        word = hardestWord.word,
                        notificationLine1 = hardestWord.notificationLine1,
                        notificationLine2 = hardestWord.notificationLine2,
                        quizCompletedHour = LocalTime.now().hour
                    )
                    LexaWallLogger.d("LexaWall", "Notification scheduled")
                }
            }
        }

        val bonusChallenge = bonusChallengeEngine.resolveEligibleChallenge()
        if (bonusChallenge != null && NotificationManagerCompat.from(appContext).areNotificationsEnabled()) {
            NotificationScheduler.scheduleBonusChallengeNotification(appContext, bonusChallenge)
        }

        val lexa = LexaVoice.generateMessage(
            appContext,
            LexaTrigger.OnQuizComplete(
                score = score,
                streakCount = streakResult.streak.currentStreak,
                hardestWord = hardestWordText
            )
        )
        withContext(Dispatchers.Main) {
            _lexaMessage.value = lexa
            _lastSessionXpEarned.value = totalSessionXp
            _lastSessionStreak.value = streakResult.streak.currentStreak
        }

        wrongAnswerCounts.clear()
        graduatedThisSession.clear()

        totalSessionXp
    }

    suspend fun predictPostCompletionStreak(): Int = withContext(Dispatchers.IO) {
        val today = LocalDate.now().toString()
        val currentStreak = database.streakDao().getStreak()
        val predicted = streakEngine.recordCompletion(currentStreak, today)
        predicted.streak.currentStreak
    }

    suspend fun predictSessionXp(score: Int, totalQuestions: Int): Int = withContext(Dispatchers.IO) {
        val today = LocalDate.now().toString()
        val currentStreak = database.streakDao().getStreak()

        val streakMilestoneXp = if (streakEngine.recordCompletion(currentStreak, today).streakMilestoneReached) {
            XpEngine.calculateStreakMilestoneXp()
        } else {
            0
        }
        val masteryXp = graduatedThisSession.size * XpEngine.calculateMasteryXp()
        XpEngine.calculateQuizXp(score, totalQuestions) + masteryXp + streakMilestoneXp
    }

    private suspend fun awardBadgeIfMissing(key: String) {
        if (!database.badgeDao().hasBadge(key)) {
            database.badgeDao().insertBadge(
                BadgeEntity(
                    badgeKey = key,
                    earnedAt = System.currentTimeMillis()
                )
            )
        }
    }

    companion object {
        private val TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
}
