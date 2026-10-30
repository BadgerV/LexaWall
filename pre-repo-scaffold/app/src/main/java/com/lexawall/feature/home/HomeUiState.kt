package com.lexawall.feature.home

import com.lexawall.domain.model.UserStats
import com.lexawall.domain.model.Word

data class HomeUiState(
    val greeting: String = "GOOD MORNING",
    val screenState: HomeScreenState = HomeScreenState.DEFAULT,
    val wallpaperWord: Word = Word(
        id = "", text = "", partOfSpeech = "", level = "",
        definition = "", exampleSentence = ""
    ),
    val stats: UserStats = UserStats(0, 0, 0, 0),
    val showWallpaperBanner: Boolean = false,
)

enum class HomeScreenState {
    DEFAULT,
    QUIZ_COMPLETED,
    WELCOME_BACK,
    RESUME_QUIZ,
    WALLPAPER_NOT_SET,
    DAY_1,
}

