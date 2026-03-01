package com.lexawall.feature.home

import com.lexawall.domain.model.*

data class HomeUiState(
    val greeting: String = "GOOD MORNING",
    val screenState: HomeScreenState = HomeScreenState.DEFAULT,
    val wallpaperWord: Word = Word("", "", "", "", "", ""),
    val stats: UserStats = UserStats(0, 0, 0, 0),
    val currentTitle: String = "The Curious",
    val isLoading: Boolean = true,
    val showWallpaperBanner: Boolean = false,
    val showLevelCompletion: Boolean = false,
    val showDay30Summary: Boolean = false,
)

enum class HomeScreenState { DEFAULT, QUIZ_COMPLETED, WELCOME_BACK, RESUME_QUIZ, WALLPAPER_NOT_SET, DAY_1, REDEMPTION_AVAILABLE }
