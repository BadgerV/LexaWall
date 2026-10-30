package com.lexawall.domain.model

data class Word(
    val id: String,
    val text: String,
    val partOfSpeech: String,
    val level: String,
    val definition: String,
    val exampleSentence: String,
    val pronunciation: String = "",
    val etymology: String = "",
)

data class QuizQuestion(
    val word: Word,
    val options: List<String>,
    val correctIndex: Int,
)

data class QuizResult(
    val score: Int,
    val total: Int,
    val headline: String,
)

data class FlashcardItem(
    val word: Word,
    val isRevealed: Boolean = false,
)

data class FlashcardSessionResult(
    val knewIt: Int,
    val stillLearning: Int,
)

data class UserStats(
    val streakDays: Int,
    val wordsMastered: Int,
    val quizScore: Int,
    val quizTotal: Int,
)

data class ThemeOption(
    val id: String,
    val name: String,
    val isActive: Boolean = false,
)

sealed class QuizOptionState {
    data object Default : QuizOptionState()
    data object Selected : QuizOptionState()
    data object Correct : QuizOptionState()
    data object Wrong : QuizOptionState()
}

