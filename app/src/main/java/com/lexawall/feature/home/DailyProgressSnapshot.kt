package com.lexawall.feature.home

data class DailyProgressSnapshot(
    val wordsLearnedToday: Int = 0,
    val xpEarnedToday: Int = 0,
    val currentStreak: Int = 0,
    val currentLevel: Int = 1,
    val currentTitle: String = "The Curious"
)
