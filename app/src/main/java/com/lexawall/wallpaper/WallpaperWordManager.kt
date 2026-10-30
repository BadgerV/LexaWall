package com.lexawall.wallpaper

import android.content.Context
import com.lexawall.data.local.DatabaseModule
import com.lexawall.data.local.PrefsManager
import com.lexawall.domain.engine.DailyWordPool
import com.lexawall.utils.LexaWallLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

object WallpaperWordManager {
    fun updateCurrentWord(
        context: Context,
        text: String,
        definition: String,
        pos: String,
        exampleSentence: String = "",
        wordId: String = text
    ) {
        if (text.isBlank()) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_WORD_TEXT, text)
            .putString(KEY_WORD_DEFINITION, definition)
            .putString(KEY_WORD_POS, pos)
            .putString(KEY_WORD_EXAMPLE, exampleSentence)
            .apply()
        LexaWallLogger.d("Rotation", "Word payload saved for '$text'")

        PrefsManager.saveCurrentWordId(context, wordId)
        LexaWallLogger.d("Rotation", "Current word id saved: $wordId")
    }

    fun updateCurrentWord(context: Context): Boolean {
        val nextWord = runBlocking(Dispatchers.IO) {
            val wordDao = DatabaseModule.getInstance(context.applicationContext).wordDao()
            val rotationPool = DailyWordPool.getWallpaperWords(context.applicationContext, wordDao)
            if (rotationPool.isEmpty()) return@runBlocking null

            val currentWordId = PrefsManager.getCurrentWordId(context)
            if (rotationPool.size == 1) {
                return@runBlocking rotationPool.first()
            }

            val currentIndex = rotationPool.indexOfFirst { it.id == currentWordId }
            if (currentIndex >= 0) {
                rotationPool[(currentIndex + 1) % rotationPool.size]
            } else {
                // Random start, then deterministic cycling for even distribution.
                rotationPool.random()
            }
        } ?: return false
        LexaWallLogger.d("Rotation", "Selected next word: ${nextWord.word} (${nextWord.id})")

        updateCurrentWord(
            context = context,
            text = nextWord.word,
            definition = nextWord.definition,
            pos = nextWord.partOfSpeech,
            exampleSentence = nextWord.exampleSentence,
            wordId = nextWord.id
        )
        PrefsManager.saveLastWallpaperRotationAt(context, System.currentTimeMillis())
        LexaWallWallpaperService.refresh()
        LexaWallLogger.d("Rotation", "Wallpaper refresh requested")
        return true
    }

    fun rotateIfDue(context: Context, force: Boolean = false): Boolean {
        val now = System.currentTimeMillis()
        val last = PrefsManager.getLastWallpaperRotationAt(context)
        val interval = resolveRotationIntervalMs(PrefsManager.getRotationSpeed(context))
        val due = force || last == 0L || (now - last) >= interval
        if (!due) return false

        return updateCurrentWord(context)
    }

    fun getCurrentWord(context: Context): Triple<String, String, String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val text = prefs.getString(KEY_WORD_TEXT, DEFAULT_WORD_TEXT) ?: DEFAULT_WORD_TEXT
        val definition = prefs.getString(KEY_WORD_DEFINITION, DEFAULT_WORD_DEFINITION) ?: DEFAULT_WORD_DEFINITION
        val pos = prefs.getString(KEY_WORD_POS, DEFAULT_WORD_POS) ?: DEFAULT_WORD_POS
        return Triple(text, definition, pos)
    }

    fun getCurrentWordExample(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_WORD_EXAMPLE, DEFAULT_WORD_EXAMPLE) ?: DEFAULT_WORD_EXAMPLE
    }

    fun saveActiveTheme(context: Context, themeName: String) {
        PrefsManager.saveActiveTheme(context, themeName.lowercase())
    }

    fun getActiveTheme(context: Context): LexaTheme {
        return LexaThemes.fromKey(PrefsManager.getActiveTheme(context))
    }

    fun getRotationSpeed(context: Context): String {
        return PrefsManager.getRotationSpeed(context)
    }

    private fun resolveRotationIntervalMs(raw: String?): Long {
        return when (raw?.trim()?.lowercase()) {
            "1min", "1 min", "1minute", "1 minute", "1 minutes" -> 60_000L
            "5min", "5 min", "5minute", "5 minute", "5 minutes" -> 300_000L
            "10min", "10 min", "10minutes", "10 minute", "10 minutes", null, "" -> 600_000L
            else -> 600_000L
        }
    }

    private const val PREFS_NAME = "lexawall_prefs"
    private const val KEY_WORD_TEXT = "current_word_text"
    private const val KEY_WORD_DEFINITION = "current_word_definition"
    private const val KEY_WORD_POS = "current_word_pos"
    private const val KEY_WORD_EXAMPLE = "current_word_example"

    private const val DEFAULT_WORD_TEXT = "ephemeral"
    private const val DEFAULT_WORD_DEFINITION = "lasting only a short time"
    private const val DEFAULT_WORD_POS = "adjective"
    private const val DEFAULT_WORD_EXAMPLE = "Fashions are ephemeral, changing with every season."
}
