package com.lexawall.data.local.seeder

import android.content.Context
import com.lexawall.data.local.LexaWallDatabase
import com.lexawall.data.local.PrefsManager
import com.lexawall.data.local.entities.StreakEntity
import com.lexawall.data.local.entities.UserProgressEntity
import com.lexawall.data.local.entities.WordEntity
import com.lexawall.utils.LexaWallLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseSeeder(
    private val context: Context,
    private val database: LexaWallDatabase
) {
    suspend fun seedIfNeeded() {
        val existingWordCount = withContext(Dispatchers.IO) { database.wordDao().getWordCount() }
        if (PrefsManager.isDbSeeded(context) && existingWordCount >= MINIMUM_WORD_COUNT) {
            LexaWallLogger.d(TAG, "Database already seeded. Skipping.")
            return
        }

        if (existingWordCount in 1 until MINIMUM_WORD_COUNT) {
            LexaWallLogger.w(
                TAG,
                "Word count too low ($existingWordCount). Forcing reseed."
            )
            PrefsManager.saveDbSeeded(context, false)
        }

        seedInternal()
        val initialCount = withContext(Dispatchers.IO) { database.wordDao().getWordCount() }
        if (initialCount == 0) {
            LexaWallLogger.w("LexaWall", "Seeding failed, retrying")
            PrefsManager.saveDbSeeded(context, false)
            seedInternal()
        }
    }

    private suspend fun seedInternal() {
        withContext(Dispatchers.IO) {
            var totalRows = 0
            var skippedRows = 0

            // Keep streak/level/badges; replace word dataset and dependent progress.
            database.wordDao().clearAll()
            database.userProgressDao().clearAll()
            PrefsManager.saveDailyPoolDate(context, "")
            PrefsManager.saveDailyPoolWordIds(context, "")
            PrefsManager.saveWanderDate(context, "")
            PrefsManager.saveWanderWordIds(context, "")
            PrefsManager.saveWanderSeenAllIds(context, "")

            val words = mutableListOf<WordEntity>()

            context.assets.open(ASSET_FILE_NAME).bufferedReader().use { reader ->
                val allRows = parseCsvRows(reader.readText())
                if (allRows.size <= 1) {
                    LexaWallLogger.w(TAG, "CSV contains no data rows.")
                    return@withContext
                }

                for (row in allRows.drop(1)) { // skip header
                    totalRows++
                    val word = row.toWordEntityOrNull()
                    if (word == null) skippedRows++ else words.add(word)
                }
            }

            if (words.isEmpty()) {
                LexaWallLogger.w(
                    TAG,
                    "No valid words parsed from CSV. totalRows=$totalRows, skippedRows=$skippedRows"
                )
                return@withContext
            }

            database.wordDao().insertAll(words)

            for (word in words) {
                database.userProgressDao().insertOrReplace(
                    UserProgressEntity(
                        wordId = word.id,
                        state = "NEW",
                        intervalDays = 1,
                        correctStreak = 0,
                        lastAnsweredDate = null
                    )
                )
            }

            database.streakDao().insertOrReplace(
                StreakEntity(
                    id = 1,
                    currentStreak = 0,
                    lastCompletedDate = null,
                    freezesUsedThisWeek = 0,
                    weekStartDate = null
                )
            )

            PrefsManager.saveDbSeeded(context, true)
            LexaWallLogger.d(
                TAG,
                "Database seed complete. totalRows=$totalRows, insertedWords=${words.size}, skippedRows=$skippedRows"
            )
        }
    }

    private fun List<String>.toWordEntityOrNull(): WordEntity? {
        if (size < REQUIRED_COLUMN_COUNT) return null

        val id = this[0].trim()
        val word = this[1].trim()
        val partOfSpeech = this[2].trim()
        val cefrLevel = this[3].trim()
        val definition = this[4].trim()
        val exampleSentence = this[5].trim()
        val notificationLine1 = this[6].trim()
        val notificationLine2 = this[7].trim()
        // Column 14 (index 14) = lexa_says — a literary passage using the word
        val lexaSays = this.getOrNull(14)?.trim()?.ifBlank { null }

        if (
            id.isEmpty() ||
            word.isEmpty() ||
            partOfSpeech.isEmpty() ||
            cefrLevel.isEmpty() ||
            definition.isEmpty() ||
            exampleSentence.isEmpty() ||
            notificationLine1.isEmpty() ||
            notificationLine2.isEmpty()
        ) {
            return null
        }

        return WordEntity(
            id = id,
            word = word,
            partOfSpeech = partOfSpeech,
            cefrLevel = cefrLevel,
            definition = definition,
            exampleSentence = exampleSentence,
            notificationLine1 = notificationLine1,
            notificationLine2 = notificationLine2,
            pronunciationIpa = null,
            etymology = null,
            lexaSays = lexaSays
        )
    }

    private fun parseCsvRows(text: String): List<List<String>> {
        if (text.isBlank()) return emptyList()
        val rows = mutableListOf<List<String>>()
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < text.length) {
            val ch = text[i]
            when {
                ch == '"' -> {
                    if (inQuotes && i + 1 < text.length && text[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ch == ',' && !inQuotes -> {
                    fields.add(current.toString())
                    current.setLength(0)
                }
                (ch == '\n' || ch == '\r') && !inQuotes -> {
                    if (ch == '\r' && i + 1 < text.length && text[i + 1] == '\n') i++
                    fields.add(current.toString())
                    current.setLength(0)
                    if (fields.any { it.isNotBlank() }) {
                        rows.add(fields.toList())
                    }
                    fields.clear()
                }
                else -> current.append(ch)
            }
            i++
        }
        if (current.isNotEmpty() || fields.isNotEmpty()) {
            fields.add(current.toString())
            if (fields.any { it.isNotBlank() }) rows.add(fields.toList())
        }
        return rows
    }

    companion object {
        private const val TAG = "DatabaseSeeder"
        private const val ASSET_FILE_NAME = "lexawall_words_v2.csv"
        private const val REQUIRED_COLUMN_COUNT = 8
        private const val MINIMUM_WORD_COUNT = 50
    }
}
