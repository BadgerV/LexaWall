package com.lexawall.feature.flashcards

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lexawall.data.local.DatabaseModule
import com.lexawall.data.local.PrefsManager
import com.lexawall.data.local.entities.WordEntity
import com.lexawall.domain.engine.DailyWordPool
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FlashcardsViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val wordDao = DatabaseModule.getInstance(appContext).wordDao()

    private val _words = MutableStateFlow<List<WordEntity>>(emptyList())
    val words: StateFlow<List<WordEntity>> = _words.asStateFlow()

    private val _sessionsToday = MutableStateFlow(0)
    val sessionsToday: StateFlow<Int> = _sessionsToday.asStateFlow()

    private val _isCompetitiveMode = MutableStateFlow(false)
    val isCompetitiveMode: StateFlow<Boolean> = _isCompetitiveMode.asStateFlow()

    private val _sessionLimit = MutableStateFlow(2)
    val sessionLimit: StateFlow<Int> = _sessionLimit.asStateFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _timerSeconds = MutableStateFlow(12)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadState()
    }

    fun completeSession() {
        viewModelScope.launch(Dispatchers.IO) {
            val today = LocalDate.now().toString()
            val newCount = _sessionsToday.value + 1
            PrefsManager.saveFlashcardSessionsToday(appContext, newCount)
            PrefsManager.saveFlashcardSessionDate(appContext, today)
            _sessionsToday.value = newCount
            _isLocked.value = newCount >= _sessionLimit.value
        }
    }

    private fun loadState() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val today = LocalDate.now().toString()
            val storedDate = PrefsManager.getFlashcardSessionDate(appContext)
            if (storedDate != today) {
                PrefsManager.saveFlashcardSessionsToday(appContext, 0)
                PrefsManager.saveFlashcardSessionDate(appContext, today)
            }

            val sessions = PrefsManager.getFlashcardSessionsToday(appContext)
            val competitive = PrefsManager.isCompetitiveModeEnabled(appContext)
            val limit = if (competitive) 1 else 2
            val timer = if (competitive) 7 else 12
            val dailyWords = DailyWordPool.getDailyWords(appContext, wordDao)

            _sessionsToday.value = sessions
            _isCompetitiveMode.value = competitive
            _sessionLimit.value = limit
            _timerSeconds.value = timer
            _isLocked.value = sessions >= limit
            _words.value = dailyWords
            _isLoading.value = false
        }
    }
}
