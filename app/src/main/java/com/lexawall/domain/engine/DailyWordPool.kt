package com.lexawall.domain.engine

import android.content.Context
import com.lexawall.data.local.PrefsManager
import com.lexawall.data.local.dao.WordDao
import com.lexawall.data.local.entities.WordEntity
import java.time.LocalDate
import kotlin.random.Random

object DailyWordPool {
    suspend fun getWallpaperWords(context: Context, wordDao: WordDao): List<WordEntity> {
        val appContext = context.applicationContext
        val today = LocalDate.now().toString()

        if (PrefsManager.getDailyPoolDate(appContext) == today) {
            val savedIds = PrefsManager.getDailyPoolWordIds(appContext)
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            if (savedIds.isNotEmpty()) {
                val fetched = wordDao.getWordsByIds(savedIds)
                val byId = fetched.associateBy { it.id }
                val ordered = savedIds.mapNotNull { byId[it] }
                if (ordered.isNotEmpty()) {
                    return ordered
                }
            }
        }

        val finalWords = wordDao
            .getAllWords()
            .shuffled(Random.Default)
            .take(25)

        PrefsManager.saveDailyPoolDate(appContext, today)
        PrefsManager.saveDailyPoolWordIds(appContext, finalWords.joinToString(",") { it.id })
        return finalWords
    }

    suspend fun getFlashcardWords(context: Context, wordDao: WordDao): List<WordEntity> {
        return getWallpaperWords(context, wordDao).take(15)
    }

    suspend fun getWordDropWords(context: Context, wordDao: WordDao): List<WordEntity> {
        return getWallpaperWords(context, wordDao).take(10)
    }

    // Backward compatibility for existing callers; now resolves to flashcard pool (15).
    suspend fun getDailyWords(context: Context, wordDao: WordDao): List<WordEntity> {
        return getFlashcardWords(context, wordDao)
    }
}
