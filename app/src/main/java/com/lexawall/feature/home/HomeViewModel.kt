package com.lexawall.feature.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lexawall.data.local.DatabaseModule
import com.lexawall.data.local.PrefsManager
import com.lexawall.data.local.entities.WordEntity
import com.lexawall.domain.engine.StreakEngine
import com.lexawall.domain.model.Word
import com.lexawall.domain.model.UserStats
import com.lexawall.wallpaper.WallpaperWordManager
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val db = DatabaseModule.getInstance(appContext)
    private val userRepository = DatabaseModule.provideUserRepository(appContext)
    private val streakEngine = StreakEngine()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val _currentWallpaperWord = MutableStateFlow<WordEntity?>(null)
    val currentWallpaperWord: StateFlow<WordEntity?> = _currentWallpaperWord.asStateFlow()
    private val allStates = HomeScreenState.entries.toList()

    init {
        loadData()
        startWallpaperSyncLoop()
    }

    private fun loadData(isRefresh: Boolean = false) {
        // Only show the loading spinner on the very first load.
        // Refreshes (ON_RESUME) keep the current UI to avoid a visible blink.
        if (!isRefresh) {
            _uiState.update { it.copy(isLoading = true) }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val today = LocalDate.now().toString()
            val yesterday = LocalDate.now().minusDays(1).toString()
            if (PrefsManager.getCurrentWordId(appContext).isNullOrBlank()) {
                WallpaperWordManager.updateCurrentWord(appContext)
            }

            val greeting = userRepository.getGreeting()
            val (wallpaperWord, currentWallpaperWordEntity) = buildWallpaperWordSnapshot()

            val streak = db.streakDao().getStreak()
            val recentQuiz = db.quizSessionDao().getRecentSessions(1).firstOrNull()
            val level = runCatching { db.levelDao().getLevel() }.getOrNull()
            val masteredProgress = db.userProgressDao().getWordsByState("MASTERED")
            val masteredCount = masteredProgress.size
            val dailyProgress = getDailyProgress(appContext)

            val stats = UserStats(
                streakDays = streak?.currentStreak ?: 0,
                wordsMastered = masteredCount,
                quizScore = recentQuiz?.score ?: 0,
                quizTotal = recentQuiz?.total ?: 0
            )

            val prefs = appContext.getSharedPreferences("lexawall_prefs", Application.MODE_PRIVATE)
            val userLevel = PrefsManager.getUserLevel(appContext)
            val quizInProgress = PrefsManager.getQuizInProgress(appContext)
            val (currentWordText, _, _) = WallpaperWordManager.getCurrentWord(appContext)
            val hasIntentionalWordSet = !PrefsManager.getCurrentWordId(appContext).isNullOrBlank()

            var showLevelCompletion = false
            val levelCompletionKey = when {
                userLevel == "A2" && masteredCount >= 10 -> "level_completion_shown_for_B1"
                userLevel == "B1" && masteredCount >= 20 -> "level_completion_shown_for_B2"
                userLevel == "B2" && masteredCount >= 40 -> "level_completion_shown_for_C1"
                else -> null
            }
            if (levelCompletionKey != null && !prefs.getBoolean(levelCompletionKey, false)) {
                prefs.edit().putBoolean(levelCompletionKey, true).apply()
                showLevelCompletion = true
            }

            var showDay30Summary = false
            if ((streak?.currentStreak ?: 0) >= 30 && !prefs.getBoolean("day30_shown", false)) {
                prefs.edit().putBoolean("day30_shown", true).apply()
                showDay30Summary = true
            }

            val screenState = when {
                recentQuiz == null && (streak?.currentStreak ?: 0) == 0 && masteredCount == 0 -> HomeScreenState.DAY_1
                recentQuiz?.datestamp == today -> HomeScreenState.QUIZ_COMPLETED
                quizInProgress -> HomeScreenState.RESUME_QUIZ
                streak != null &&
                    streakEngine.isStreakBroken(streak, today) &&
                    streakEngine.canRedeem(streak, today) -> HomeScreenState.REDEMPTION_AVAILABLE
                currentWordText == "ephemeral" && !hasIntentionalWordSet -> HomeScreenState.WALLPAPER_NOT_SET
                streak != null &&
                    streak.lastCompletedDate != null &&
                    streak.lastCompletedDate != today &&
                    streak.lastCompletedDate != yesterday &&
                    !streakEngine.isStreakBroken(streak, today) -> HomeScreenState.WELCOME_BACK
                else -> HomeScreenState.DEFAULT
            }

            withContext(Dispatchers.Main) {
                _currentWallpaperWord.value = currentWallpaperWordEntity
                _uiState.update {
                    it.copy(
                        greeting = greeting,
                        wallpaperWord = wallpaperWord,
                        stats = stats,
                        currentTitle = level?.title ?: "The Curious",
                        dailyProgress = dailyProgress,
                        isLoading = false,
                        screenState = screenState,
                        showWallpaperBanner = screenState == HomeScreenState.WALLPAPER_NOT_SET,
                        showLevelCompletion = showLevelCompletion,
                        showDay30Summary = showDay30Summary
                    )
                }
            }
        }
    }

    fun cycleState() {
        val nextIndex = (allStates.indexOf(_uiState.value.screenState) + 1) % allStates.size
        val nextState = allStates[nextIndex]
        _uiState.update {
            it.copy(
                screenState = nextState,
                showWallpaperBanner = nextState == HomeScreenState.WALLPAPER_NOT_SET
            )
        }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(showWallpaperBanner = false) }
    }

    fun markQuizInProgress(inProgress: Boolean) {
        PrefsManager.saveQuizInProgress(appContext, inProgress)
    }

    fun refreshWallpaperWord() {
        viewModelScope.launch(Dispatchers.IO) {
            val (wallpaperWord, entity) = buildWallpaperWordSnapshot()
            withContext(Dispatchers.Main) {
                _currentWallpaperWord.value = entity
                _uiState.update { state -> state.copy(wallpaperWord = wallpaperWord) }
            }
        }
    }

    fun refreshData() {
        loadData(isRefresh = true)
    }

    private fun startWallpaperSyncLoop() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val (wallpaperWord, entity) = buildWallpaperWordSnapshot()
                withContext(Dispatchers.Main) {
                    _currentWallpaperWord.value = entity
                    _uiState.update { state ->
                        if (state.wallpaperWord.id == wallpaperWord.id &&
                            state.wallpaperWord.text == wallpaperWord.text &&
                            state.wallpaperWord.definition == wallpaperWord.definition &&
                            state.wallpaperWord.partOfSpeech == wallpaperWord.partOfSpeech
                        ) {
                            state
                        } else {
                            state.copy(wallpaperWord = wallpaperWord)
                        }
                    }
                }
                delay(60_000L)
            }
        }
    }

    private suspend fun buildWallpaperWordSnapshot(): Pair<Word, WordEntity?> {
        val (text, definition, pos) = WallpaperWordManager.getCurrentWord(appContext)
        val example = WallpaperWordManager.getCurrentWordExample(appContext)
        val currentId = PrefsManager.getCurrentWordId(appContext)
        val directEntity = if (!currentId.isNullOrBlank()) db.wordDao().getWordById(currentId) else null
        val entity = directEntity ?: db.wordDao().getAllWords().firstOrNull()

        // Recover from stale/invalid saved IDs (e.g., old dataset IDs).
        if (directEntity == null && entity != null) {
            PrefsManager.saveCurrentWordId(appContext, entity.id)
            WallpaperWordManager.updateCurrentWord(
                context = appContext,
                text = entity.word,
                definition = entity.definition,
                pos = entity.partOfSpeech,
                exampleSentence = entity.exampleSentence,
                wordId = entity.id
            )
        }

        val resolvedId = entity?.id ?: currentId ?: text
        val resolvedText = if (text.isNotBlank()) text else entity?.word.orEmpty()
        val resolvedDefinition = if (definition.isNotBlank()) definition else entity?.definition.orEmpty()
        val resolvedPos = if (pos.isNotBlank()) pos else entity?.partOfSpeech.orEmpty()
        val resolvedExample = if (example.isNotBlank()) example else entity?.exampleSentence.orEmpty()

        return Word(
            id = resolvedId,
            text = resolvedText,
            partOfSpeech = resolvedPos,
            level = entity?.cefrLevel.orEmpty(),
            definition = resolvedDefinition,
            exampleSentence = resolvedExample,
            pronunciation = entity?.pronunciationIpa.orEmpty(),
            etymology = entity?.etymology.orEmpty()
        ) to entity
    }

    private suspend fun getDailyProgress(context: Context): DailyProgressSnapshot {
        val database = DatabaseModule.getInstance(context)
        val today = LocalDate.now().toString()
        val wordsLearned = database.userProgressDao().getProgressByLastAnsweredDate(today).size
        val xpEarned = database.quizSessionDao().getSessionsForDate(today).sumOf { it.xpEarned }
        val streak = database.streakDao().getStreak()
        val level = database.levelDao().getLevel()

        return DailyProgressSnapshot(
            wordsLearnedToday = wordsLearned,
            xpEarnedToday = xpEarned,
            currentStreak = streak.currentStreak,
            currentLevel = level.currentLevel,
            currentTitle = level.title
        )
    }
}
