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
        LexaWallWallpaperService.refresh()
        LexaWallLogger.d("Rotation", "Wallpaper refresh requested")
    }

    fun updateCurrentWord(context: Context) {
        val nextWord = runBlocking(Dispatchers.IO) {
            val wordDao = DatabaseModule.getInstance(context.applicationContext).wordDao()
            val dailyWords = DailyWordPool.getDailyWords(context.applicationContext, wordDao)
            val allWords = wordDao.getAllWords()
            if (allWords.isEmpty()) return@runBlocking null

            val dailyIds = dailyWords.map { it.id }.toSet()
            val extraWords = allWords
                .filterNot { it.id in dailyIds }
                .shuffled()
                .take(10)
            val rotationPool = (dailyWords + extraWords)
                .ifEmpty { allWords.shuffled().take(25) }
                .distinctBy { it.id }

            val currentWordId = PrefsManager.getCurrentWordId(context)
            val candidates = if (!currentWordId.isNullOrBlank() && rotationPool.size > 1) {
                rotationPool.filterNot { it.id == currentWordId }
            } else {
                rotationPool
            }
            candidates.randomOrNull()
        } ?: return
        LexaWallLogger.d("Rotation", "Selected next word: ${nextWord.word} (${nextWord.id})")

        updateCurrentWord(
            context = context,
            text = nextWord.word,
            definition = nextWord.definition,
            pos = nextWord.partOfSpeech,
            exampleSentence = nextWord.exampleSentence,
            wordId = nextWord.id
        )
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
