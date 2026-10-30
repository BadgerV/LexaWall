package com.lexawall.feature.quiz

import com.lexawall.domain.model.QuizQuestion
import com.lexawall.domain.model.QuizOptionState

data class QuizUiState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOption: Int? = null,
    val optionStates: List<QuizOptionState> = emptyList(),
    val score: Int = 0,
)

