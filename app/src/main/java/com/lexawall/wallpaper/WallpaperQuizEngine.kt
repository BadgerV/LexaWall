package com.lexawall.wallpaper

import android.content.Context
import com.lexawall.data.local.DatabaseModule
import com.lexawall.data.local.PrefsManager
import com.lexawall.utils.LexaWallLogger
import java.time.LocalDate
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class WallpaperQuizPrompt(
    val quotaDate: String,
    val wordId: String,
    val word: String,
    val options: List<String>,
    val correctIndex: Int
)

class WallpaperQuizEngine(
    context: Context,
    private val scope: CoroutineScope,
    private val onQuizReady: (WallpaperQuizPrompt) -> Unit
) {
    private val appContext = context.applicationContext
    private val random = Random(System.currentTimeMillis())
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Volatile
    private var isScreenOn = false

    @Volatile
    private var quizActive = false

    private var monitorJob: Job? = null
    private var isMonitoringRotations = false
    private var monitoringStartMillis = 0L
    private var monitoringQuotaDate: String = ""
    private var monitoringWordId: String? = null
    private var observedRotations = 0
    private var pendingPrompt: WallpaperQuizPrompt? = null

    fun onScreenOn() {
        isScreenOn = true
        startMonitoringIfEligible()
    }

    fun onScreenOff() {
        isScreenOn = false
        if (monitorJob != null || isMonitoringRotations) {
            cancelMonitoringAndReschedule()
        }
    }

    fun onWallpaperRotated() {
        if (!isMonitoringRotations || !isScreenOn || quizActive) return
        observedRotations += 1
        LexaWallLogger.d("WallpaperQuiz", "Observed rotation count=$observedRotations")
        if (observedRotations >= 2) {
            isMonitoringRotations = false
            monitorJob?.cancel()
            monitorJob = null
            scope.launch(Dispatchers.IO) {
                triggerQuiz()
            }
        }
    }

    fun onQuizAnswered(wordId: String, wasCorrect: Boolean) {
        val now = System.currentTimeMillis()
        val prompt = pendingPrompt
        val quotaDate = prompt?.quotaDate ?: today()
        val newCount = getCountForDate(quotaDate) + 1
        saveCountForDate(quotaDate, newCount)

        PrefsManager.saveLastWallpaperQuizTime(appContext, now)
        saveLastAnsweredWord(wordId, now)
        scheduleNextTriggerWindow(now)
        clearPendingPrompt()

        LexaWallLogger.d(
            "WallpaperQuiz",
            "Answer recorded word=$wordId correct=$wasCorrect quotaDate=$quotaDate count=$newCount"
        )
    }

    fun onQuizOverlayDismissed() {
        clearPendingPrompt()
    }

    private fun startMonitoringIfEligible() {
        if (quizActive || monitorJob != null) return
        if (!PrefsManager.isWallpaperQuizOptedIn(appContext)) return

        val now = System.currentTimeMillis()
        val today = today()
        normalizeDailyCounter(today)

        if (getCountForDate(today) >= MAX_QUIZZES_PER_DAY) {
            LexaWallLogger.d("WallpaperQuiz", "Daily cap reached, skipping")
            return
        }
        if (isWithinCooldown(now)) {
            LexaWallLogger.d("WallpaperQuiz", "Cooldown active, skipping")
            return
        }

        val nextTrigger = PrefsManager.getNextWallpaperQuizTriggerTime(appContext)
        if (nextTrigger > now) {
            LexaWallLogger.d("WallpaperQuiz", "Next trigger window not reached yet")
            return
        }

        val currentWordId = PrefsManager.getCurrentWordId(appContext)
        if (!currentWordId.isNullOrBlank() && wasSameWordAnsweredRecently(currentWordId, now)) {
            LexaWallLogger.d("WallpaperQuiz", "Same word answered recently, skipping retrigger")
            return
        }

        monitorJob = scope.launch(Dispatchers.IO) {
            val initialDelay = random.nextLong(INITIAL_DELAY_MIN_MS, INITIAL_DELAY_MAX_MS + 1)
            LexaWallLogger.d("WallpaperQuiz", "Initial random delay=$initialDelay ms")
            delay(initialDelay)

            if (!isScreenOn) {
                cancelMonitoringAndReschedule()
                return@launch
            }

            monitoringQuotaDate = today
            monitoringWordId = currentWordId
            monitoringStartMillis = System.currentTimeMillis()
            observedRotations = 0
            isMonitoringRotations = true
            LexaWallLogger.d("WallpaperQuiz", "Monitoring rotations started for date=$monitoringQuotaDate")

            while (isActive && isMonitoringRotations) {
                if (!isScreenOn) {
                    cancelMonitoringAndReschedule()
                    return@launch
                }

                val elapsed = System.currentTimeMillis() - monitoringStartMillis
                if (elapsed >= ROTATION_WAIT_TIMEOUT_MS) {
                    LexaWallLogger.d("WallpaperQuiz", "Timeout waiting for 2 rotations; rescheduling")
                    isMonitoringRotations = false
                    val timeoutQuotaDate = monitoringQuotaDate.ifBlank { today() }
                    saveCountForDate(timeoutQuotaDate, getCountForDate(timeoutQuotaDate) + 1)
                    scheduleNextTriggerWindow(System.currentTimeMillis())
                    monitorJob = null
                    return@launch
                }
                delay(250)
            }
        }
    }

    private suspend fun triggerQuiz() {
        if (!isScreenOn || quizActive) return
        val now = System.currentTimeMillis()
        if (isWithinCooldown(now)) return
        val wordId = monitoringWordId ?: PrefsManager.getCurrentWordId(appContext)
        if (!wordId.isNullOrBlank() && wasSameWordAnsweredRecently(wordId, now)) return

        val prompt = buildPrompt(wordId, monitoringQuotaDate.ifBlank { today() })
        if (prompt == null) {
            scheduleNextTriggerWindow(now)
            return
        }

        pendingPrompt = prompt
        quizActive = true
        LexaWallLogger.d("WallpaperQuiz", "Quiz triggered for word=${prompt.word} (${prompt.wordId})")
        onQuizReady(prompt)
    }

    private suspend fun buildPrompt(wordIdHint: String?, quotaDate: String): WallpaperQuizPrompt? {
        val wordDao = DatabaseModule.getInstance(appContext).wordDao()
        val allWords = wordDao.getAllWords()
        if (allWords.size < 4) return null

        val target = when {
            !wordIdHint.isNullOrBlank() -> wordDao.getWordById(wordIdHint)
            else -> null
        } ?: run {
            val currentText = WallpaperWordManager.getCurrentWord(appContext).first
            allWords.firstOrNull { it.word.equals(currentText, ignoreCase = true) } ?: allWords.randomOrNull()
        } ?: return null

        val correctDefinition = target.definition.trim()
        if (correctDefinition.isBlank()) return null

        val distractors = allWords
            .asSequence()
            .filter { it.id != target.id }
            .map { it.definition.trim() }
            .filter { it.isNotBlank() && !it.equals(correctDefinition, ignoreCase = true) }
            .distinct()
            .toList()
            .shuffled(random)
            .take(3)

        if (distractors.size < 3) return null

        val options = (distractors + correctDefinition).shuffled(random)
        val correctIndex = options.indexOf(correctDefinition)
        if (correctIndex < 0) return null

        return WallpaperQuizPrompt(
            quotaDate = quotaDate,
            wordId = target.id,
            word = target.word,
            options = options,
            correctIndex = correctIndex
        )
    }

    private fun cancelMonitoringAndReschedule() {
        isMonitoringRotations = false
        monitorJob?.cancel()
        monitorJob = null
        if (PrefsManager.isWallpaperQuizOptedIn(appContext)) {
            scheduleNextTriggerWindow(System.currentTimeMillis())
        }
    }

    private fun clearPendingPrompt() {
        pendingPrompt = null
        quizActive = false
    }

    private fun normalizeDailyCounter(today: String) {
        val storedDate = prefs.getString(KEY_WALLPAPER_QUIZ_COUNT_DATE, null)
        if (storedDate != today) {
            saveCountForDate(today, 0)
        }
    }

    private fun getCountForDate(date: String): Int {
        val storedDate = prefs.getString(KEY_WALLPAPER_QUIZ_COUNT_DATE, null)
        return if (storedDate == date) {
            PrefsManager.getWallpaperQuizCountToday(appContext)
        } else {
            0
        }
    }

    private fun saveCountForDate(date: String, count: Int) {
        prefs.edit().putString(KEY_WALLPAPER_QUIZ_COUNT_DATE, date).apply()
        PrefsManager.saveWallpaperQuizCountToday(appContext, count.coerceAtLeast(0))
    }

    private fun isWithinCooldown(now: Long): Boolean {
        val lastAnsweredAt = PrefsManager.getLastWallpaperQuizTime(appContext)
        return lastAnsweredAt > 0L && (now - lastAnsweredAt) < QUIZ_COOLDOWN_MS
    }

    private fun wasSameWordAnsweredRecently(wordId: String, now: Long): Boolean {
        val lastWordId = prefs.getString(KEY_LAST_ANSWERED_WORD_ID, null)
        val lastWordAnsweredAt = prefs.getLong(KEY_LAST_ANSWERED_WORD_TIME, 0L)
        return lastWordId == wordId &&
            lastWordAnsweredAt > 0L &&
            (now - lastWordAnsweredAt) < SAME_WORD_COOLDOWN_MS
    }

    private fun saveLastAnsweredWord(wordId: String, answeredAt: Long) {
        prefs.edit()
            .putString(KEY_LAST_ANSWERED_WORD_ID, wordId)
            .putLong(KEY_LAST_ANSWERED_WORD_TIME, answeredAt)
            .apply()
    }

    private fun scheduleNextTriggerWindow(fromTimeMs: Long) {
        val hours = random.nextInt(5, 9)
        val nextTime = fromTimeMs + (hours * ONE_HOUR_MS)
        PrefsManager.saveNextWallpaperQuizTriggerTime(appContext, nextTime)
        LexaWallLogger.d("WallpaperQuiz", "Next trigger scheduled in ${hours}h")
    }

    private fun today(): String = LocalDate.now().toString()

    companion object {
        private const val PREFS_NAME = "lexawall_prefs"
        private const val KEY_WALLPAPER_QUIZ_COUNT_DATE = "wallpaper_quiz_count_date"
        private const val KEY_LAST_ANSWERED_WORD_ID = "wallpaper_quiz_last_word_id"
        private const val KEY_LAST_ANSWERED_WORD_TIME = "wallpaper_quiz_last_word_time"

        private const val MAX_QUIZZES_PER_DAY = 2
        private const val INITIAL_DELAY_MIN_MS = 30_000L
        private const val INITIAL_DELAY_MAX_MS = 90_000L
        private const val ROTATION_WAIT_TIMEOUT_MS = 5 * 60 * 1000L
        private const val QUIZ_COOLDOWN_MS = 5 * 60 * 60 * 1000L
        private const val SAME_WORD_COOLDOWN_MS = 5 * 60 * 1000L
        private const val ONE_HOUR_MS = 60 * 60 * 1000L

        internal fun isWithinQuizCooldown(now: Long, lastAnsweredAt: Long): Boolean {
            return lastAnsweredAt > 0L && (now - lastAnsweredAt) < QUIZ_COOLDOWN_MS
        }

        internal fun isSameWordWithinCooldown(
            lastWordId: String?,
            lastAnsweredAt: Long,
            currentWordId: String,
            now: Long
        ): Boolean {
            return lastWordId == currentWordId &&
                lastAnsweredAt > 0L &&
                (now - lastAnsweredAt) < SAME_WORD_COOLDOWN_MS
        }

        internal fun resolveTodayCount(
            storedDate: String?,
            today: String,
            storedCount: Int
        ): Int {
            return if (storedDate == today) storedCount.coerceAtLeast(0) else 0
        }

        internal fun canTriggerWithDailyLimit(todayCount: Int): Boolean {
            return todayCount < MAX_QUIZZES_PER_DAY
        }
    }
}
