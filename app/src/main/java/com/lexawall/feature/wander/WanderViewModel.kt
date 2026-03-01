package com.lexawall.feature.wander

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lexawall.data.local.DatabaseModule
import com.lexawall.data.local.PrefsManager
import com.lexawall.data.local.entities.WordEntity
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WanderViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val db = DatabaseModule.getInstance(appContext)

    private val _wanderWords = MutableStateFlow<List<WordEntity>>(emptyList())
    val wanderWords: StateFlow<List<WordEntity>> = _wanderWords.asStateFlow()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadTodayWords()
    }

    private fun loadTodayWords() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val today = LocalDate.now().toString()
            val savedDate = PrefsManager.getWanderDate(appContext)

            val words = if (savedDate != today) {
                val dailyPoolIds = PrefsManager.getDailyPoolWordIds(appContext)
                    .split(',')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                val seenWanderIds = PrefsManager.getWanderSeenAllIds(appContext)
                    .split(',')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                val combinedExclusions = (dailyPoolIds + seenWanderIds).distinct()
                var generated = getWanderCandidates(combinedExclusions)

                if (generated.size < 50) {
                    PrefsManager.saveWanderSeenAllIds(appContext, "")
                    generated = getWanderCandidates(dailyPoolIds)
                }

                val todayIds = generated.map { it.id }
                val updatedSeen = (seenWanderIds + todayIds).distinct().joinToString(",")
                PrefsManager.saveWanderSeenAllIds(appContext, updatedSeen)
                PrefsManager.saveWanderWordIds(appContext, todayIds.joinToString(","))
                PrefsManager.saveWanderDate(appContext, today)
                generated
            } else {
                val savedIds = PrefsManager.getWanderWordIds(appContext)
                    .split(',')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                if (savedIds.isEmpty()) {
                    val generated = db.wordDao().getRandomWords(50)
                    val ids = generated.joinToString(",") { it.id }
                    PrefsManager.saveWanderWordIds(appContext, ids)
                    generated
                } else {
                    val fetched = db.wordDao().getWordsByIds(savedIds)
                    val byId = fetched.associateBy { it.id }
                    savedIds.mapNotNull { byId[it] }
                }
            }

            _wanderWords.value = words
            _isLoading.value = false
        }
    }

    private suspend fun getWanderCandidates(excludeIds: List<String>): List<WordEntity> {
        return if (excludeIds.isEmpty()) {
            db.wordDao().getRandomWords(50)
        } else {
            db.wordDao().getWanderWords(excludeIds, 50)
        }
    }
}
