package com.lexawall.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lexawall.feature.flashcards.FlashcardsRevealedScreen
import com.lexawall.feature.flashcards.FlashcardsSessionSummaryScreen
import com.lexawall.feature.flashcards.FlashcardsWordScreen
import com.lexawall.feature.home.HomeScreen
import com.lexawall.feature.milestones.Day30SummaryScreen
import com.lexawall.feature.milestones.GraduationScreen
import com.lexawall.feature.milestones.LevelCompletionScreen
import com.lexawall.feature.onboarding.OnboardingNotificationsScreen
import com.lexawall.feature.onboarding.OnboardingSetupScreen
import com.lexawall.feature.onboarding.OnboardingWallpaperScreen
import com.lexawall.feature.onboarding.SplashScreen
import com.lexawall.feature.quiz.QuizCorrectScreen
import com.lexawall.feature.quiz.QuizEndScreen
import com.lexawall.feature.quiz.QuizQuestionScreen
import com.lexawall.feature.quiz.QuizWrongScreen
import com.lexawall.feature.settings.SettingsScreen
import com.lexawall.feature.settings.ThemePickerScreen
import com.lexawall.feature.worddetail.MiniQuizFeedbackScreen
import com.lexawall.feature.worddetail.MiniQuizQuestionScreen
import com.lexawall.feature.worddetail.WordDetailScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // ─── Onboarding ───
        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Screen.OnboardingWallpaper.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.OnboardingWallpaper.route) {
            OnboardingWallpaperScreen(
                onSetWallpaper = {
                    navController.navigate(Screen.OnboardingSetup.route)
                }
            )
        }
        composable(Screen.OnboardingSetup.route) {
            OnboardingSetupScreen(
                onBack = { navController.popBackStack() },
                onOpenPicker = {
                    navController.navigate(Screen.OnboardingNotifications.route)
                },
                onNotNow = {
                    navController.navigate(Screen.OnboardingNotifications.route)
                }
            )
        }
        composable(Screen.OnboardingNotifications.route) {
            OnboardingNotificationsScreen(
                onAllow = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNoThanks = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ─── Home ───
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onStartQuiz = {
                    navController.navigate(Screen.QuizQuestion.createRoute(0))
                },
                onOpenFlashcards = {
                    navController.navigate(Screen.FlashcardsWord.createRoute(0))
                },
                onWordTap = { wordId ->
                    navController.navigate(Screen.WordDetail.createRoute(wordId))
                }
            )
        }

        // ─── Quiz ───
        composable(
            route = Screen.QuizQuestion.route,
            arguments = listOf(navArgument("questionIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("questionIndex") ?: 0
            QuizQuestionScreen(
                questionIndex = index,
                onCorrect = {
                    navController.navigate(Screen.QuizCorrect.createRoute(index)) {
                        popUpTo(Screen.QuizQuestion.createRoute(index)) { inclusive = true }
                    }
                },
                onWrong = {
                    navController.navigate(Screen.QuizWrong.createRoute(index)) {
                        popUpTo(Screen.QuizQuestion.createRoute(index)) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.QuizCorrect.route,
            arguments = listOf(navArgument("questionIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("questionIndex") ?: 0
            QuizCorrectScreen(
                questionIndex = index,
                onContinue = { nextIndex, isLast ->
                    if (isLast) {
                        navController.navigate(Screen.QuizEnd.createRoute(score = 11, total = 13)) {
                            popUpTo(Screen.Home.route)
                        }
                    } else {
                        navController.navigate(Screen.QuizQuestion.createRoute(nextIndex)) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                }
            )
        }
        composable(
            route = Screen.QuizWrong.route,
            arguments = listOf(navArgument("questionIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("questionIndex") ?: 0
            QuizWrongScreen(
                questionIndex = index,
                onGotIt = { nextIndex, isLast ->
                    if (isLast) {
                        navController.navigate(Screen.QuizEnd.createRoute(score = 11, total = 13)) {
                            popUpTo(Screen.Home.route)
                        }
                    } else {
                        navController.navigate(Screen.QuizQuestion.createRoute(nextIndex)) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                }
            )
        }
        composable(
            route = Screen.QuizEnd.route,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt("score") ?: 11
            val total = backStackEntry.arguments?.getInt("total") ?: 13
            QuizEndScreen(
                score = score,
                total = total,
                onBackToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // ─── Flashcards ───
        composable(
            route = Screen.FlashcardsWord.route,
            arguments = listOf(navArgument("cardIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("cardIndex") ?: 0
            FlashcardsWordScreen(
                cardIndex = index,
                onReveal = {
                    navController.navigate(Screen.FlashcardsRevealed.createRoute(index)) {
                        popUpTo(Screen.FlashcardsWord.createRoute(index)) { inclusive = true }
                    }
                },
                onStillLearning = { nextIndex, isLast ->
                    if (isLast) {
                        navController.navigate(Screen.FlashcardsSummary.route) {
                            popUpTo(Screen.Home.route)
                        }
                    } else {
                        navController.navigate(Screen.FlashcardsWord.createRoute(nextIndex)) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                },
                onKnowIt = { nextIndex, isLast ->
                    if (isLast) {
                        navController.navigate(Screen.FlashcardsSummary.route) {
                            popUpTo(Screen.Home.route)
                        }
                    } else {
                        navController.navigate(Screen.FlashcardsWord.createRoute(nextIndex)) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                },
                onClose = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.FlashcardsRevealed.route,
            arguments = listOf(navArgument("cardIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("cardIndex") ?: 0
            FlashcardsRevealedScreen(
                cardIndex = index,
                onFlipBack = { navController.popBackStack() },
                onStillLearning = { nextIndex, isLast ->
                    if (isLast) {
                        navController.navigate(Screen.FlashcardsSummary.route) {
                            popUpTo(Screen.Home.route)
                        }
                    } else {
                        navController.navigate(Screen.FlashcardsWord.createRoute(nextIndex)) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                },
                onKnowIt = { nextIndex, isLast ->
                    if (isLast) {
                        navController.navigate(Screen.FlashcardsSummary.route) {
                            popUpTo(Screen.Home.route)
                        }
                    } else {
                        navController.navigate(Screen.FlashcardsWord.createRoute(nextIndex)) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                },
                onClose = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.FlashcardsSummary.route) {
            FlashcardsSessionSummaryScreen(
                onStartQuiz = {
                    navController.navigate(Screen.QuizQuestion.createRoute(0)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBackToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // ─── Word Detail ───
        composable(
            route = Screen.WordDetail.route,
            arguments = listOf(navArgument("wordId") { type = NavType.StringType })
        ) { backStackEntry ->
            val wordId = backStackEntry.arguments?.getString("wordId") ?: "ephemeral"
            WordDetailScreen(
                wordId = wordId,
                onBack = { navController.popBackStack() },
                onQuizMe = { wId ->
                    navController.navigate(Screen.MiniQuizQuestion.createRoute(wId))
                }
            )
        }
        composable(
            route = Screen.MiniQuizQuestion.route,
            arguments = listOf(navArgument("wordId") { type = NavType.StringType })
        ) { backStackEntry ->
            val wordId = backStackEntry.arguments?.getString("wordId") ?: "ephemeral"
            MiniQuizQuestionScreen(
                wordId = wordId,
                onAnswer = { isCorrect ->
                    navController.navigate(Screen.MiniQuizFeedback.createRoute(wordId, isCorrect)) {
                        popUpTo(Screen.MiniQuizQuestion.createRoute(wordId)) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.MiniQuizFeedback.route,
            arguments = listOf(
                navArgument("wordId") { type = NavType.StringType },
                navArgument("isCorrect") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val wordId = backStackEntry.arguments?.getString("wordId") ?: "ephemeral"
            val isCorrect = backStackEntry.arguments?.getBoolean("isCorrect") ?: true
            MiniQuizFeedbackScreen(
                wordId = wordId,
                isCorrect = isCorrect,
                onBackToWord = {
                    navController.navigate(Screen.WordDetail.createRoute(wordId)) {
                        popUpTo(Screen.WordDetail.createRoute(wordId)) { inclusive = true }
                    }
                }
            )
        }

        // ─── Settings ───
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onThemeTap = { navController.navigate(Screen.ThemePicker.route) },
                onResetConfirmed = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.ThemePicker.route) {
            ThemePickerScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Milestones ───
        composable(Screen.Graduation.route) {
            GraduationScreen(
                onKeepGoing = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.LevelCompletion.route) {
            LevelCompletionScreen(
                onContinue = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Day30Summary.route) {
            Day30SummaryScreen(
                onBackToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

