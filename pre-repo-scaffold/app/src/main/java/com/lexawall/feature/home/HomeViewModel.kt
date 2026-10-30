package com.lexawall.feature.home

import androidx.lifecycle.ViewModel
import com.lexawall.data.fake.FakeUserRepository
import com.lexawall.data.fake.FakeWordRepository
import com.lexawall.domain.repository.UserRepository
import com.lexawall.domain.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel(
    private val wordRepository: WordRepository = FakeWordRepository(),
    private val userRepository: UserRepository = FakeUserRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val allStates = HomeScreenState.entries.toList()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.update {
            it.copy(
                greeting = userRepository.getGreeting(),
                wallpaperWord = wordRepository.getWallpaperWord(),
                stats = userRepository.getUserStats(),
                screenState = HomeScreenState.DEFAULT,
            )
        }
    }

    fun cycleState() {
        val currentIndex = allStates.indexOf(_uiState.value.screenState)
        val nextIndex = (currentIndex + 1) % allStates.size
        val nextState = allStates[nextIndex]
        _uiState.update {
            it.copy(
                screenState = nextState,
                showWallpaperBanner = nextState == HomeScreenState.WALLPAPER_NOT_SET
            )
        }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(showWallpaperBanner = false) }
    }
}

