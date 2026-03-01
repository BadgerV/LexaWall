package com.lexawall.domain.engine

import android.content.Context
import com.lexawall.data.local.DatabaseModule
import com.lexawall.data.local.PrefsManager
import com.lexawall.data.local.dao.WordDao
import com.lexawall.data.local.entities.WordEntity
import java.time.LocalDate

object DailyWordPool {
    private val spacedRepetitionEngine = SpacedRepetitionEngine()

    suspend fun getDailyWords(context: Context, wordDao: WordDao): List<WordEntity> {
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

        val db = DatabaseModule.getInstance(appContext)
        val allProgress = db.userProgressDao().getAllProgress()
        val pool = spacedRepetitionEngine.buildQuizPool(allProgress)
        val poolIds = pool.map { it.wordId }.distinct()

        val generated = if (poolIds.isNotEmpty()) {
            val fetched = wordDao.getWordsByIds(poolIds)
            val byId = fetched.associateBy { it.id }
            poolIds.mapNotNull { byId[it] }
        } else {
            wordDao.getRandomWords(15)
        }

        val finalWords = if (generated.isEmpty()) {
            wordDao.getAllWords().take(15)
        } else {
            generated
        }

        PrefsManager.saveDailyPoolDate(appContext, today)
        PrefsManager.saveDailyPoolWordIds(appContext, finalWords.joinToString(",") { it.id })
        return finalWords
    }
}
