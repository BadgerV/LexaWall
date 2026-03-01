package com.lexawall.core.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object OnboardingWallpaper : Screen("onboarding_wallpaper")
    data object OnboardingSetup : Screen("onboarding_setup")
    data object OnboardingNotifications : Screen("onboarding_notifications")
    data object CalibrationWordDrop : Screen("calibration_word_drop")
    data object Home : Screen("home")
    data object Wander : Screen("wander")
    data object Progress : Screen("progress")
    data object WordDropQuestion : Screen("word_drop_question/{questionIndex}") {
        fun createRoute(questionIndex: Int) = "word_drop_question/$questionIndex"
    }
    data object QuizCorrect : Screen("quiz_correct/{questionIndex}") {
        fun createRoute(questionIndex: Int) = "quiz_correct/$questionIndex"
    }
    data object QuizWrong : Screen("quiz_wrong/{questionIndex}") {
        fun createRoute(questionIndex: Int) = "quiz_wrong/$questionIndex"
    }
    data object WordDropEnd : Screen("word_drop_end/{score}/{total}/{streak}/{xp}") {
        fun createRoute(score: Int, total: Int, streak: Int, xp: Int) = "word_drop_end/$score/$total/$streak/$xp"
    }
    data object LevelUp : Screen("level_up/{newTitle}/{unlockedTheme}/{unlockedWordTier}") {
        fun createRoute(newTitle: String, unlockedTheme: String, unlockedWordTier: String): String {
            return "level_up/${android.net.Uri.encode(newTitle)}/${android.net.Uri.encode(unlockedTheme)}/${android.net.Uri.encode(unlockedWordTier)}"
        }
    }
    data object RedemptionWordDrop : Screen("redemption_word_drop")
    data object FlashcardsWord : Screen("flashcards_word/{cardIndex}") {
        fun createRoute(cardIndex: Int) = "flashcards_word/$cardIndex"
    }
    data object FlashcardsSummary : Screen("flashcards_summary")
    data object WordDetail : Screen("word_detail/{wordId}") {
        fun createRoute(wordId: String) = "word_detail/$wordId"
    }
    data object WanderWordDetail : Screen("wander_word_detail/{wordIndex}") {
        fun createRoute(wordIndex: Int) = "wander_word_detail/$wordIndex"
    }
    data object MiniWordDropQuestion : Screen("mini_word_drop_question/{wordId}") {
        fun createRoute(wordId: String) = "mini_word_drop_question/$wordId"
    }
    data object MiniWordDropFeedback : Screen("mini_word_drop_feedback/{wordId}/{isCorrect}") {
        fun createRoute(wordId: String, isCorrect: Boolean) = "mini_word_drop_feedback/$wordId/$isCorrect"
    }
    data object Settings : Screen("settings")
    data object ThemePicker : Screen("theme_picker")
    data object Graduation : Screen("graduation/{wordId}") {
        fun createRoute(wordId: String) = "graduation/$wordId"
    }
    data object LevelCompletion : Screen("level_completion")
    data object Day30Summary : Screen("day_30_summary")
}
