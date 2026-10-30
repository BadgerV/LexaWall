package com.lexawall.feature.quiz

import androidx.lifecycle.ViewModel
import com.lexawall.data.fake.FakeQuizRepository
import com.lexawall.domain.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.lexawall.domain.model.QuizOptionState

class QuizViewModel(
    private val quizRepository: QuizRepository = FakeQuizRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        val questions = quizRepository.getQuizQuestions()
        _uiState.update {
            it.copy(
                questions = questions,
                optionStates = List(questions.firstOrNull()?.options?.size ?: 3) { QuizOptionState.Default }
            )
        }
    }

    fun getQuestion(index: Int) = _uiState.value.questions.getOrNull(
        index.coerceIn(0, _uiState.value.questions.lastIndex.coerceAtLeast(0))
    )

    fun getTotalQuestions() = _uiState.value.questions.size

    fun isLastQuestion(index: Int) = index >= _uiState.value.questions.lastIndex
}

