package com.lexawall.core.navigation

sealed class Screen(val route: String) {
    // Onboarding
    data object Splash : Screen("splash")
    data object OnboardingWallpaper : Screen("onboarding_wallpaper")
    data object OnboardingSetup : Screen("onboarding_setup")
    data object OnboardingNotifications : Screen("onboarding_notifications")

    // Home
    data object Home : Screen("home")

    // Quiz
    data object QuizQuestion : Screen("quiz_question/{questionIndex}") {
        fun createRoute(questionIndex: Int) = "quiz_question/$questionIndex"
    }
    data object QuizCorrect : Screen("quiz_correct/{questionIndex}") {
        fun createRoute(questionIndex: Int) = "quiz_correct/$questionIndex"
    }
    data object QuizWrong : Screen("quiz_wrong/{questionIndex}") {
        fun createRoute(questionIndex: Int) = "quiz_wrong/$questionIndex"
    }
    data object QuizEnd : Screen("quiz_end/{score}/{total}") {
        fun createRoute(score: Int, total: Int) = "quiz_end/$score/$total"
    }

    // Flashcards
    data object FlashcardsWord : Screen("flashcards_word/{cardIndex}") {
        fun createRoute(cardIndex: Int) = "flashcards_word/$cardIndex"
    }
    data object FlashcardsRevealed : Screen("flashcards_revealed/{cardIndex}") {
        fun createRoute(cardIndex: Int) = "flashcards_revealed/$cardIndex"
    }
    data object FlashcardsSummary : Screen("flashcards_summary")

    // Word Detail
    data object WordDetail : Screen("word_detail/{wordId}") {
        fun createRoute(wordId: String) = "word_detail/$wordId"
    }
    data object MiniQuizQuestion : Screen("mini_quiz_question/{wordId}") {
        fun createRoute(wordId: String) = "mini_quiz_question/$wordId"
    }
    data object MiniQuizFeedback : Screen("mini_quiz_feedback/{wordId}/{isCorrect}") {
        fun createRoute(wordId: String, isCorrect: Boolean) = "mini_quiz_feedback/$wordId/$isCorrect"
    }

    // Settings
    data object Settings : Screen("settings")
    data object ThemePicker : Screen("theme_picker")

    // Milestones
    data object Graduation : Screen("graduation")
    data object LevelCompletion : Screen("level_completion")
    data object Day30Summary : Screen("day_30_summary")
}

