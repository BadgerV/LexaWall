package com.lexawall.data.local.repository

import android.content.Context
import com.lexawall.data.local.PrefsManager
import com.lexawall.data.local.dao.UserProgressDao
import com.lexawall.data.local.dao.WordDao
import com.lexawall.data.local.entities.WordEntity
import com.lexawall.domain.model.Word
import com.lexawall.domain.repository.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.random.Random

class RoomWordRepository(
    private val context: Context,
    private val wordDao: WordDao,
    private val userProgressDao: UserProgressDao
) : WordRepository {

    override fun getWallpaperWord(): Word = runBlocking {
        withContext(Dispatchers.IO) {
            val currentWordId = PrefsManager.getCurrentWordId(context)

            val word = if (!currentWordId.isNullOrBlank()) {
                wordDao.getWordById(currentWordId)
            } else {
                null
            }
                ?: wordDao.getAllWords().firstOrNull()

            word?.toDomainWord() ?: Word(
                id = "ephemeral",
                text = "ephemeral",
                partOfSpeech = "",
                level = "",
                definition = "",
                exampleSentence = "",
                pronunciation = "",
                etymology = "",
                lexaSays = null
            )
        }
    }

    override fun getWordById(id: String): Word = runBlocking {
        withContext(Dispatchers.IO) {
            val word = wordDao.getWordById(id) ?: wordDao.getAllWords().firstOrNull()
            word?.toDomainWord() ?: Word(
                id = id,
                text = id,
                partOfSpeech = "",
                level = "",
                definition = "",
                exampleSentence = "",
                pronunciation = "",
                etymology = "",
                lexaSays = null
            )
        }
    }

    override fun getFlashcardWords(): List<Word> = runBlocking {
        withContext(Dispatchers.IO) {
            val seen = userProgressDao.getWordsByState("SEEN")
            val tested = userProgressDao.getWordsByState("TESTED")

            val seenAndTestedWordIds = (seen + tested)
                .map { it.wordId }
                .distinct()
            val seenAndTestedWords = seenAndTestedWordIds
                .mapNotNull { id -> wordDao.getWordById(id)?.toDomainWord() }

            if (seenAndTestedWords.size >= 5) {
                return@withContext seenAndTestedWords.take(10)
            }

            val existingIds = seenAndTestedWords.map { it.id }.toSet()
            val remainingSlots = (10 - seenAndTestedWords.size).coerceAtLeast(0)
            val newWordIds = userProgressDao.getWordsByState("NEW")
                .map { it.wordId }
                .filterNot { it in existingIds }
                .distinct()
                .shuffled(Random.Default)
                .take(remainingSlots)

            val newWords = newWordIds.mapNotNull { id ->
                wordDao.getWordById(id)?.toDomainWord()
            }

            (seenAndTestedWords + newWords).take(10)
        }
    }

    private fun WordEntity.toDomainWord(): Word {
        return Word(
            id = id,
            text = word,
            partOfSpeech = partOfSpeech,
            level = cefrLevel,
            definition = definition,
            exampleSentence = exampleSentence,
            pronunciation = pronunciationIpa.orEmpty(),
            etymology = etymology.orEmpty(),
            lexaSays = lexaSays
        )
    }
}
