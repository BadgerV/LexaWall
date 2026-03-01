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
        if (PrefsManager.isDbSeeded(context) && existingWordCount > 0) {
            LexaWallLogger.d(TAG, "Database already seeded. Skipping.")
            return
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
            var totalLines = 0
            var skippedRows = 0

            val words = mutableListOf<WordEntity>()

            context.assets.open(ASSET_FILE_NAME).bufferedReader().use { reader ->
                reader.readLine() // skip header

                var line = reader.readLine()
                while (line != null) {
                    totalLines++

                    val parsed = parseCsvLine(line)
                    val word = parsed?.toWordEntityOrNull()
                    if (word == null) {
                        skippedRows++
                    } else {
                        words.add(word)
                    }

                    line = reader.readLine()
                }
            }

            if (words.isEmpty()) {
                LexaWallLogger.w(
                    TAG,
                    "No valid words parsed from CSV. totalLines=$totalLines, skippedRows=$skippedRows"
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
                "Database seed complete. totalLines=$totalLines, insertedWords=${words.size}, skippedRows=$skippedRows"
            )
        }
    }

    private fun List<String>.toWordEntityOrNull(): WordEntity? {
        if (size != EXPECTED_COLUMN_COUNT) return null

        val id = this[0].trim()
        val word = this[1].trim()
        val partOfSpeech = this[2].trim()
        val cefrLevel = this[3].trim()
        val definition = this[4].trim()
        val exampleSentence = this[5].trim()
        val notificationLine1 = this[6].trim()
        val notificationLine2 = this[7].trim()

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
            etymology = null
        )
    }

    private fun parseCsvLine(line: String): List<String>? {
        if (line.isBlank()) return null

        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
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
                else -> current.append(ch)
            }
            i++
        }

        if (inQuotes) return null

        fields.add(current.toString())
        return fields
    }

    companion object {
        private const val TAG = "DatabaseSeeder"
        private const val ASSET_FILE_NAME = "lexawall_words.csv"
        private const val EXPECTED_COLUMN_COUNT = 8
    }
}
