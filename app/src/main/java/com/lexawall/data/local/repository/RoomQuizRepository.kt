package com.lexawall.data.local.repository

import android.content.Context
import com.lexawall.data.local.dao.UserProgressDao
import com.lexawall.data.local.dao.WordDao
import com.lexawall.data.local.entities.WordEntity
import com.lexawall.domain.engine.DailyWordPool
import com.lexawall.domain.engine.SpacedRepetitionEngine
import com.lexawall.domain.model.QuizQuestion
import com.lexawall.domain.model.QuizResult
import com.lexawall.domain.model.Word
import com.lexawall.domain.repository.QuizRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.random.Random

class RoomQuizRepository(
    private val context: Context,
    private val wordDao: WordDao,
    private val userProgressDao: UserProgressDao,
    private val spacedRepetitionEngine: SpacedRepetitionEngine = SpacedRepetitionEngine()
) : QuizRepository {

    override fun getQuizQuestions(): List<QuizQuestion> = runBlocking {
        withContext(Dispatchers.IO) {
            val allWords = wordDao.getAllWords()
            if (allWords.size < 3) {
                return@withContext emptyList()
            }
            val dailyWords = DailyWordPool.getWordDropWords(context, wordDao)
            if (dailyWords.isEmpty()) {
                return@withContext emptyList()
            }

            dailyWords
                .distinctBy { it.id }
                .mapNotNull { wordEntity ->
                    buildQuizQuestion(wordEntity, allWords)
                }
                .take(10)
        }
    }

    override fun getQuizResult(score: Int, total: Int): QuizResult {
        return QuizResult(
            score = score,
            total = total,
            headline = when {
                score == total -> "Flawless."
                score >= total * 0.8 -> "Honestly impressive."
                score >= total * 0.5 -> "Solid effort."
                else -> "Keep going."
            }
        )
    }

    private fun buildQuizQuestion(correctWord: WordEntity, allWords: List<WordEntity>): QuizQuestion {
        val incorrectDefinitions = allWords
            .asSequence()
            .filter { it.id != correctWord.id }
            .map { it.definition }
            .distinct()
            .shuffled(Random.Default)
            .take(2)
            .toMutableList()

        while (incorrectDefinitions.size < 2) {
            incorrectDefinitions.add(correctWord.definition)
        }

        val options = mutableListOf(correctWord.definition).apply {
            addAll(incorrectDefinitions)
        }.shuffled(Random.Default)
        val correctIndex = options.indexOf(correctWord.definition)

        return QuizQuestion(
            word = correctWord.toDomainWord(),
            options = options,
            correctIndex = correctIndex
        )
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
            etymology = etymology.orEmpty()
        )
    }
}
