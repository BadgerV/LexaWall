package com.lexawall.domain.repository

import com.lexawall.domain.model.FlashcardSessionResult
import com.lexawall.domain.model.QuizQuestion
import com.lexawall.domain.model.QuizResult
import com.lexawall.domain.model.ThemeOption
import com.lexawall.domain.model.UserStats
import com.lexawall.domain.model.Word

interface WordRepository {
    fun getWallpaperWord(): Word
    fun getWordById(id: String): Word
    fun getFlashcardWords(): List<Word>
}

interface QuizRepository {
    fun getQuizQuestions(): List<QuizQuestion>
    fun getQuizResult(score: Int, total: Int): QuizResult
}

interface UserRepository {
    fun getUserStats(): UserStats
    fun getGreeting(): String
}

interface ThemeRepository {
    fun getThemes(): List<ThemeOption>
}

