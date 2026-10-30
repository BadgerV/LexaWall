package com.lexawall.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.lexawall.MainActivity
import com.lexawall.core.ui.LocalLexaTheme
import com.lexawall.data.local.DatabaseModule
import com.lexawall.data.local.PrefsManager
import com.lexawall.data.local.entities.StreakEntity
import com.lexawall.domain.engine.StreakEngine
import com.lexawall.feature.flashcards.FlashcardsSessionSummaryScreen
import com.lexawall.feature.flashcards.FlashcardsWordScreen
import com.lexawall.feature.home.HomeScreen
import com.lexawall.feature.home.HomeViewModel
import com.lexawall.feature.levelup.LevelUpScreen
import com.lexawall.feature.milestones.Day30SummaryScreen
import com.lexawall.feature.milestones.GraduationScreen
import com.lexawall.feature.milestones.LevelCompletionScreen
import com.lexawall.feature.onboarding.CalibrationQuizScreen
import com.lexawall.feature.onboarding.OnboardingNotificationsScreen
import com.lexawall.feature.onboarding.OnboardingSetupScreen
import com.lexawall.feature.onboarding.OnboardingWallpaperScreen
import com.lexawall.feature.onboarding.SplashScreen
import com.lexawall.feature.profile.ProfileScreen
import com.lexawall.feature.progress.ProgressScreen
import com.lexawall.feature.quiz.QuizCorrectScreen
import com.lexawall.feature.quiz.QuizEndScreen
import com.lexawall.feature.quiz.QuizQuestionScreen
import com.lexawall.feature.quiz.QuizViewModel
import com.lexawall.feature.quiz.QuizWrongScreen
import com.lexawall.feature.quiz.RedemptionQuizScreen
import com.lexawall.feature.quiz.BonusChallengeScreen
import com.lexawall.feature.settings.SettingsScreen
import com.lexawall.feature.settings.ThemePickerScreen
import com.lexawall.feature.worddetail.MiniQuizFeedbackScreen
import com.lexawall.feature.worddetail.MiniQuizQuestionScreen
import com.lexawall.feature.worddetail.WordDetailScreen
import com.lexawall.feature.voice.VoicePracticeScreen
import com.lexawall.feature.wander.WanderScreen
import com.lexawall.feature.wander.WanderWordDetailScreen
import com.lexawall.feature.wordhunt.WordHuntScreen
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun NavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel = viewModel()
) {
    val quizViewModel: QuizViewModel = viewModel()
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route.orEmpty()
    var showFlashcardsLeaveWarning by remember { mutableStateOf(false) }
    var pendingBottomNavAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val startDestination = remember(context) {
        if (PrefsManager.isOnboardingComplete(context)) Screen.Home.route else Screen.Splash.route
    }

    LaunchedEffect(MainActivity.pendingOpenWordDrop.value, currentRoute) {
        if (MainActivity.pendingOpenWordDrop.value && PrefsManager.isOnboardingComplete(context)) {
            homeViewModel.markQuizInProgress(true)
            val targetRoute = Screen.WordDropQuestion.createRoute(0)
            if (currentRoute != targetRoute) {
                navController.navigate(targetRoute) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            MainActivity.pendingOpenWordDrop.value = false
        }
    }

    LaunchedEffect(MainActivity.pendingBonusChallengeType.value, currentRoute) {
        val challengeType = MainActivity.pendingBonusChallengeType.value
        if (!challengeType.isNullOrBlank() && PrefsManager.isOnboardingComplete(context)) {
            val targetRoute = Screen.BonusChallenge.createRoute(challengeType)
            if (currentRoute != targetRoute) {
                navController.navigate(targetRoute) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            MainActivity.pendingBonusChallengeType.value = null
        }
    }

    fun countFlashcardsExitSession() {
        val today = LocalDate.now().toString()
        val savedDate = PrefsManager.getFlashcardSessionDate(context)
        if (savedDate != today) {
            PrefsManager.saveFlashcardSessionCount(context, 0)
            PrefsManager.saveFlashcardSessionDate(context, today)
        }
        val currentCount = PrefsManager.getFlashcardSessionCount(context)
        PrefsManager.saveFlashcardSessionCount(context, currentCount + 1)
        PrefsManager.saveFlashcardSessionDate(context, today)
    }

    fun runWithFlashcardsExitGuard(action: () -> Unit) {
        if (currentRoute.startsWith("flashcards_word")) {
            val today = LocalDate.now().toString()
            val savedDate = PrefsManager.getFlashcardSessionDate(context)
            if (savedDate != today) {
                action()
                return
            }
            val currentCount = PrefsManager.getFlashcardSessionCount(context)
            if (currentCount >= 2) {
                action()
                return
            }
            pendingBottomNavAction = action
            showFlashcardsLeaveWarning = true
        } else {
            action()
        }
    }

    Scaffold(
        containerColor = Color(LocalLexaTheme.current.backgroundTop),
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                LexaBottomNavigationBar(
                    currentRoute = currentRoute,
                    onHome = {
                        runWithFlashcardsExitGuard {
                            homeViewModel.markQuizInProgress(false)
                            navigateToBottomBarRoute(navController, Screen.Home.route)
                        }
                    },
                    onFlashcards = {
                        homeViewModel.markQuizInProgress(false)
                        navigateToBottomBarRoute(navController, Screen.FlashcardsWord.createRoute(0))
                    },
                    onWander = {
                        runWithFlashcardsExitGuard {
                            homeViewModel.markQuizInProgress(false)
                            navigateToBottomBarRoute(navController, Screen.Wander.route)
                        }
                    },
                    onProgress = {
                        runWithFlashcardsExitGuard {
                            homeViewModel.markQuizInProgress(false)
                            navigateToBottomBarRoute(navController, Screen.Progress.route)
                        }
                    },
                    onSettings = {
                        runWithFlashcardsExitGuard {
                            homeViewModel.markQuizInProgress(false)
                            navigateToBottomBarRoute(navController, Screen.Settings.route)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onTimeout = {
                        navController.navigate(Screen.OnboardingWallpaper.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onOnboardingComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.OnboardingWallpaper.route) {
                val localContext = LocalContext.current
                OnboardingWallpaperScreen(
                    onSetWallpaper = {
                        navController.navigate(Screen.OnboardingSetup.route)
                    },
                    onSkip = {
                        PrefsManager.saveWallpaperOptedOut(localContext, true)
                        navController.navigate(Screen.OnboardingNotifications.route) {
                            popUpTo(Screen.OnboardingWallpaper.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.OnboardingSetup.route) {
                OnboardingSetupScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() },
                    onNotNow = { navController.navigate(Screen.OnboardingNotifications.route) }
                )
            }
            composable(Screen.OnboardingNotifications.route) {
                OnboardingNotificationsScreen(
                    onAllow = { navController.navigate(Screen.CalibrationWordDrop.route) },
                    onNoThanks = { navController.navigate(Screen.CalibrationWordDrop.route) }
                )
            }
            composable(Screen.CalibrationWordDrop.route) {
                CalibrationQuizScreen(
                    onComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.CalibrationWordDrop.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                val homeUiState by homeViewModel.uiState.collectAsState()
                val localContext = LocalContext.current

                LaunchedEffect(Unit) {
                    val pendingGraduationWordId = PrefsManager.getGraduationWordId(localContext)
                    if (!pendingGraduationWordId.isNullOrBlank()) {
                        navController.navigate(Screen.Graduation.createRoute(pendingGraduationWordId))
                        PrefsManager.saveGraduationWordId(localContext, null)
                    }
                }
                LaunchedEffect(homeUiState.showLevelCompletion) {
                    if (homeUiState.showLevelCompletion) {
                        navController.navigate(Screen.LevelCompletion.route)
                    }
                }
                LaunchedEffect(homeUiState.showDay30Summary) {
                    if (homeUiState.showDay30Summary) {
                        navController.navigate(Screen.Day30Summary.route)
                    }
                }

                HomeScreen(
                    onStartQuiz = {
                        homeViewModel.markQuizInProgress(true)
                        navController.navigate(Screen.WordDropQuestion.createRoute(0))
                    },
                    onOpenFlashcards = {
                        homeViewModel.markQuizInProgress(false)
                        navController.navigate(Screen.FlashcardsWord.createRoute(0))
                    },
                    onWordTap = { wordId ->
                        homeViewModel.markQuizInProgress(false)
                        navController.navigate(Screen.WordDetail.createRoute(wordId))
                    },
                    onRestoreStreak = {
                        homeViewModel.markQuizInProgress(false)
                        navController.navigate(Screen.RedemptionWordDrop.route)
                    }
                )
            }
            composable(Screen.Progress.route) {
                ProgressScreen(
                    onWordTap = { wordId -> navController.navigate(Screen.WordDetail.createRoute(wordId)) }
                )
            }
            composable(Screen.Wander.route) {
                WanderScreen(
                    onWordTap = { wordIndex -> navController.navigate(Screen.WanderWordDetail.createRoute(wordIndex)) }
                )
            }
            composable(
                Screen.WanderWordDetail.route,
                arguments = listOf(navArgument("wordIndex") { type = NavType.IntType })
            ) { entry ->
                val wordIndex = entry.arguments?.getInt("wordIndex") ?: 0
                WanderWordDetailScreen(
                    initialIndex = wordIndex,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.RedemptionWordDrop.route) {
                val localContext = LocalContext.current
                val db = remember { DatabaseModule.getInstance(localContext) }
                val streakEngine = remember { StreakEngine() }
                val scope = rememberCoroutineScope()
                var currentStreak by remember { mutableIntStateOf(0) }

                LaunchedEffect(Unit) {
                    currentStreak = withContext(Dispatchers.IO) {
                        db.streakDao().getStreak()?.currentStreak ?: 0
                    }
                }

                RedemptionQuizScreen(
                    currentStreak = currentStreak,
                    onPass = {
                        scope.launch(Dispatchers.IO) {
                            val today = LocalDate.now().toString()
                            val streak = db.streakDao().getStreak() ?: StreakEntity(
                                id = 1,
                                currentStreak = 0,
                                lastCompletedDate = null,
                                freezesUsedThisWeek = 0,
                                weekStartDate = null
                            )
                            val updated = streakEngine.recordCompletion(streak, today)
                            db.streakDao().insertOrReplace(updated.streak)

                            launch(Dispatchers.Main) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.RedemptionWordDrop.route) { inclusive = true }
                                }
                            }
                        }
                    },
                    onFail = {
                        scope.launch(Dispatchers.IO) {
                            val streak = db.streakDao().getStreak() ?: StreakEntity(
                                id = 1,
                                currentStreak = 0,
                                lastCompletedDate = null,
                                freezesUsedThisWeek = 0,
                                weekStartDate = null
                            )
                            db.streakDao().insertOrReplace(streak.copy(currentStreak = 0))

                            launch(Dispatchers.Main) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.RedemptionWordDrop.route) { inclusive = true }
                                }
                            }
                        }
                    }
                )
            }
            composable(
                Screen.WordDropQuestion.route,
                arguments = listOf(navArgument("questionIndex") { type = NavType.IntType })
            ) { entry ->
                val idx = entry.arguments?.getInt("questionIndex") ?: 0
                val localContext = LocalContext.current
                val scope = rememberCoroutineScope()
                QuizQuestionScreen(
                    questionIndex = idx,
                    onCorrect = {
                        val isLast = quizViewModel.isLastQuestion(idx)
                        if (isLast) {
                            scope.launch {
                                val score = quizViewModel.getScore()
                                val total = quizViewModel.getTotalQuestions()
                                val predictedStreak = quizViewModel.predictPostCompletionStreak()
                                val predictedXp = quizViewModel.predictSessionXp(score, total)
                                val graduationWordId = PrefsManager.getGraduationWordId(localContext)
                                if (!graduationWordId.isNullOrBlank()) {
                                    PrefsManager.savePendingQuizEnd(localContext, score, total, predictedStreak, predictedXp)
                                    navController.navigate(Screen.Graduation.createRoute(graduationWordId)) {
                                        popUpTo(Screen.Home.route) { inclusive = false }
                                    }
                                } else {
                                    navController.navigate(Screen.WordDropEnd.createRoute(score, total, predictedStreak, predictedXp)) {
                                        popUpTo(Screen.WordDropQuestion.route) { inclusive = true }
                                    }
                                }
                            }
                        } else {
                            navController.navigate(Screen.WordDropQuestion.createRoute(idx + 1)) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        }
                    },
                    onWrong = {
                        val isLast = quizViewModel.isLastQuestion(idx)
                        if (isLast) {
                            scope.launch {
                                val score = quizViewModel.getScore()
                                val total = quizViewModel.getTotalQuestions()
                                val predictedStreak = quizViewModel.predictPostCompletionStreak()
                                val predictedXp = quizViewModel.predictSessionXp(score, total)
                                val graduationWordId = PrefsManager.getGraduationWordId(localContext)
                                if (!graduationWordId.isNullOrBlank()) {
                                    PrefsManager.savePendingQuizEnd(localContext, score, total, predictedStreak, predictedXp)
                                    navController.navigate(Screen.Graduation.createRoute(graduationWordId)) {
                                        popUpTo(Screen.Home.route) { inclusive = false }
                                    }
                                } else {
                                    navController.navigate(Screen.WordDropEnd.createRoute(score, total, predictedStreak, predictedXp)) {
                                        popUpTo(Screen.WordDropQuestion.route) { inclusive = true }
                                    }
                                }
                            }
                        } else {
                            navController.navigate(Screen.WordDropQuestion.createRoute(idx + 1)) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        }
                    },
                    onBackToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.WordDropQuestion.route) { inclusive = true }
                        }
                    },
                    quizViewModel = quizViewModel
                )
            }
            composable(Screen.QuizCorrect.route, arguments = listOf(navArgument("questionIndex") { type = NavType.IntType })) { entry ->
                val idx = entry.arguments?.getInt("questionIndex") ?: 0
                val localContext = LocalContext.current
                val scope = rememberCoroutineScope()
                QuizCorrectScreen(questionIndex = idx, onContinue = { next, isLast ->
                    if (isLast) {
                        scope.launch {
                            val score = quizViewModel.getScore()
                            val total = quizViewModel.getTotalQuestions()
                            val predictedStreak = quizViewModel.predictPostCompletionStreak()
                            val predictedXp = quizViewModel.predictSessionXp(score, total)
                            val graduationWordId = PrefsManager.getGraduationWordId(localContext)
                            if (!graduationWordId.isNullOrBlank()) {
                                PrefsManager.savePendingQuizEnd(localContext, score, total, predictedStreak, predictedXp)
                                navController.navigate(Screen.Graduation.createRoute(graduationWordId)) {
                                    popUpTo(Screen.Home.route) { inclusive = false }
                                }
                            } else {
                                navController.navigate(Screen.WordDropEnd.createRoute(score, total, predictedStreak, predictedXp)) {
                                    popUpTo(Screen.WordDropQuestion.route) { inclusive = true }
                                }
                            }
                        }
                    } else {
                        navController.navigate(Screen.WordDropQuestion.createRoute(next)) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                }, quizViewModel = quizViewModel)
            }
            composable(Screen.QuizWrong.route, arguments = listOf(navArgument("questionIndex") { type = NavType.IntType })) { entry ->
                val idx = entry.arguments?.getInt("questionIndex") ?: 0
                val localContext = LocalContext.current
                val scope = rememberCoroutineScope()
                QuizWrongScreen(questionIndex = idx, onGotIt = { next, isLast ->
                    if (isLast) {
                        scope.launch {
                            val score = quizViewModel.getScore()
                            val total = quizViewModel.getTotalQuestions()
                            val predictedStreak = quizViewModel.predictPostCompletionStreak()
                            val predictedXp = quizViewModel.predictSessionXp(score, total)
                            val graduationWordId = PrefsManager.getGraduationWordId(localContext)
                            if (!graduationWordId.isNullOrBlank()) {
                                PrefsManager.savePendingQuizEnd(localContext, score, total, predictedStreak, predictedXp)
                                navController.navigate(Screen.Graduation.createRoute(graduationWordId)) {
                                    popUpTo(Screen.Home.route) { inclusive = false }
                                }
                            } else {
                                navController.navigate(Screen.WordDropEnd.createRoute(score, total, predictedStreak, predictedXp)) {
                                    popUpTo(Screen.WordDropQuestion.route) { inclusive = true }
                                }
                            }
                        }
                    } else {
                        navController.navigate(Screen.WordDropQuestion.createRoute(next)) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                }, quizViewModel = quizViewModel)
            }
            composable(
                Screen.WordDropEnd.route,
                arguments = listOf(
                    navArgument("score") { type = NavType.IntType },
                    navArgument("total") { type = NavType.IntType },
                    navArgument("streak") { type = NavType.IntType },
                    navArgument("xp") { type = NavType.IntType }
                )
            ) { entry ->
                QuizEndScreen(
                    score = entry.arguments?.getInt("score") ?: 0,
                    total = entry.arguments?.getInt("total") ?: 0,
                    streak = entry.arguments?.getInt("streak") ?: 0,
                    xpEarned = entry.arguments?.getInt("xp") ?: 0,
                    onNavigateLevelUp = { newTitle, unlockedTheme, unlockedWordTier ->
                        navController.navigate(
                            Screen.LevelUp.createRoute(newTitle, unlockedTheme, unlockedWordTier)
                        )
                    },
                    onBackToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.WordDropQuestion.route) { inclusive = true }
                        }
                        homeViewModel.markQuizInProgress(false)
                    },
                    onBack = { navController.popBackStack() },
                    quizViewModel = quizViewModel
                )
            }
            composable(
                Screen.BonusChallenge.route,
                arguments = listOf(navArgument("challengeType") { type = NavType.StringType })
            ) { entry ->
                val challengeType = entry.arguments?.getString("challengeType").orEmpty()
                BonusChallengeScreen(
                    challengeTypeRaw = challengeType,
                    onClose = { navController.popBackStack() },
                    onCompleted = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.BonusChallenge.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.FlashcardsWord.route, arguments = listOf(navArgument("cardIndex") { type = NavType.IntType })) { entry ->
                val idx = entry.arguments?.getInt("cardIndex") ?: 0
                FlashcardsWordScreen(
                    cardIndex = idx,
                    onReveal = {},
                    onStillLearning = { n, l ->
                        if (l) navController.navigate(Screen.FlashcardsSummary.route) { popUpTo(Screen.Home.route) }
                        else navController.navigate(Screen.FlashcardsWord.createRoute(n)) { popUpTo(Screen.Home.route) }
                    },
                    onKnowIt = { _, _ ->
                        homeViewModel.markQuizInProgress(true)
                        navController.navigate(Screen.WordDropQuestion.createRoute(0)) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    },
                    onClose = {
                        navController.navigate(Screen.Home.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                )
            }
            composable(Screen.FlashcardsSummary.route) {
                FlashcardsSessionSummaryScreen(
                    onStartQuiz = {
                        navController.navigate(Screen.WordDropQuestion.createRoute(0)) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onBackToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                )
            }
            composable(Screen.WordDetail.route, arguments = listOf(navArgument("wordId") { type = NavType.StringType })) { entry ->
                val wordId = entry.arguments?.getString("wordId") ?: "ephemeral"
                WordDetailScreen(
                    wordId = wordId,
                    onBack = { navController.popBackStack() },
                    onQuizMe = { navController.navigate(Screen.MiniWordDropQuestion.createRoute(it)) },
                    onPracticePronunciation = { navController.navigate(Screen.VoicePractice.createRoute(it)) }
                )
            }
            composable(
                Screen.VoicePractice.route,
                arguments = listOf(navArgument("wordId") { type = NavType.StringType })
            ) { entry ->
                val voiceWordId = entry.arguments?.getString("wordId") ?: "1"
                VoicePracticeScreen(
                    wordId = voiceWordId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.MiniWordDropQuestion.route, arguments = listOf(navArgument("wordId") { type = NavType.StringType })) { entry ->
                val wordId = entry.arguments?.getString("wordId") ?: "ephemeral"
                MiniQuizQuestionScreen(wordId = wordId, onAnswer = { isCorrect ->
                    navController.navigate(Screen.MiniWordDropFeedback.createRoute(wordId, isCorrect)) {
                        popUpTo(Screen.MiniWordDropQuestion.createRoute(wordId)) { inclusive = true }
                    }
                })
            }
            composable(Screen.MiniWordDropFeedback.route, arguments = listOf(navArgument("wordId") { type = NavType.StringType }, navArgument("isCorrect") { type = NavType.BoolType })) { entry ->
                val wordId = entry.arguments?.getString("wordId") ?: "ephemeral"
                val isCorrect = entry.arguments?.getBoolean("isCorrect") ?: true
                MiniQuizFeedbackScreen(wordId = wordId, isCorrect = isCorrect, onBackToWord = {
                    navController.popBackStack()
                })
            }
            composable(Screen.Settings.route) {
                val localContext = LocalContext.current
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onThemeTap = { navController.navigate(Screen.ThemePicker.route) },
                    onLevelChanged = { homeViewModel.refreshData() },
                    onVoicePracticeTap = {
                        val targetWordId = PrefsManager.getCurrentWordId(localContext)
                            ?.takeIf { it.isNotBlank() }
                            ?: "1"
                        navController.navigate(Screen.VoicePractice.createRoute(targetWordId))
                    },
                    onWordHuntTap = { navController.navigate(Screen.WordHunt.route) },
                    onProfileTap = { navController.navigate(Screen.Profile.route) }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.WordHunt.route) {
                WordHuntScreen(
                    onClose = { navController.popBackStack() },
                    onSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                )
            }
            composable(Screen.ThemePicker.route) {
                ThemePickerScreen(onBack = { navController.popBackStack() })
            }
            composable(
                Screen.Graduation.route,
                arguments = listOf(navArgument("wordId") { type = NavType.StringType })
            ) { entry ->
                val localContext = LocalContext.current
                val wordId = entry.arguments?.getString("wordId") ?: "1"
                GraduationScreen(onKeepGoing = {
                    val pendingQuizEnd = PrefsManager.getPendingQuizEnd(localContext)
                    if (pendingQuizEnd != null) {
                        PrefsManager.clearPendingQuizEnd(localContext)
                        navController.navigate(
                            Screen.WordDropEnd.createRoute(
                                pendingQuizEnd.first,
                                pendingQuizEnd.second,
                                pendingQuizEnd.third,
                                pendingQuizEnd.fourth
                            )
                        ) {
                            popUpTo(Screen.WordDropQuestion.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.WordDropQuestion.route) { inclusive = true }
                        }
                    }
                }, wordId = wordId, onBack = { navController.popBackStack() })
            }
            composable(
                Screen.LevelUp.route,
                arguments = listOf(
                    navArgument("newTitle") { type = NavType.StringType },
                    navArgument("unlockedTheme") { type = NavType.StringType },
                    navArgument("unlockedWordTier") { type = NavType.StringType }
                )
            ) { entry ->
                val newTitle = entry.arguments?.getString("newTitle").orEmpty()
                val unlockedTheme = entry.arguments?.getString("unlockedTheme").orEmpty()
                val unlockedWordTier = entry.arguments?.getString("unlockedWordTier").orEmpty()
                LevelUpScreen(
                    newTitle = newTitle,
                    unlockedTheme = unlockedTheme,
                    unlockedWordTier = unlockedWordTier,
                    onContinue = {
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.LevelCompletion.route) {
                LevelCompletionScreen(onContinue = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                })
            }
            composable(Screen.Day30Summary.route) {
                Day30SummaryScreen(onBackToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                })
            }
            }

            if (showFlashcardsLeaveWarning) {
                AlertDialog(
                    onDismissRequest = { showFlashcardsLeaveWarning = false },
                    title = {
                        Text(
                            text = "Leave session?",
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                        )
                    },
                    text = {
                        Text(
                            text = "Leaving now counts as one flashcard session."
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            countFlashcardsExitSession()
                            showFlashcardsLeaveWarning = false
                            pendingBottomNavAction?.invoke()
                            pendingBottomNavAction = null
                        }) {
                            Text("Leave")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showFlashcardsLeaveWarning = false
                            pendingBottomNavAction = null
                        }) {
                            Text("Keep practicing")
                        }
                    }
                )
            }
        }
    }

    LaunchedEffect(MainActivity.pendingDeepLinkWordId.value) {
        val deepLinkWordId = MainActivity.pendingDeepLinkWordId.value
        if (!deepLinkWordId.isNullOrBlank()) {
            navController.navigate(Screen.WordDetail.createRoute(deepLinkWordId))
            MainActivity.pendingDeepLinkWordId.value = null
        }
    }
}

private fun shouldShowBottomBar(currentRoute: String): Boolean {
    return currentRoute == Screen.Home.route ||
        currentRoute == Screen.Wander.route ||
        currentRoute == Screen.Progress.route ||
        currentRoute == Screen.Settings.route ||
        currentRoute.startsWith("flashcards_word") ||
        currentRoute.startsWith("flashcards_summary")
}

@Composable
private fun LexaBottomNavigationBar(
    currentRoute: String,
    onHome: () -> Unit,
    onFlashcards: () -> Unit,
    onWander: () -> Unit,
    onProgress: () -> Unit,
    onSettings: () -> Unit
) {
    val activeTheme = LocalLexaTheme.current
    val selectedColor = Color(activeTheme.accentColor)
    val unselectedColor = Color(0xFF666680)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(activeTheme.backgroundTop).copy(alpha = 0.94f),
                        Color(activeTheme.backgroundTop)
                    )
                )
            )
            .systemBarsPadding()
            .padding(top = 10.dp, bottom = 6.dp, start = 22.dp, end = 22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem(
                selected = currentRoute == Screen.Home.route,
                icon = Icons.Default.MenuBook,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                onClick = onHome
            )
            BottomItem(
                selected = currentRoute.startsWith("flashcards_word") || currentRoute.startsWith("flashcards_summary"),
                icon = Icons.Default.History,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                onClick = onFlashcards
            )
            BottomItem(
                selected = currentRoute == Screen.Wander.route,
                icon = Icons.Default.Explore,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                onClick = onWander
            )
            BottomItem(
                selected = currentRoute == Screen.Progress.route,
                icon = Icons.Default.BarChart,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                onClick = onProgress
            )
            BottomItem(
                selected = currentRoute == Screen.Settings.route,
                icon = Icons.Default.Person,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                onClick = onSettings
            )
        }
    }
}

@Composable
private fun BottomItem(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selectedColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = Modifier.wrapContentHeight()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) selectedColor else unselectedColor,
                modifier = Modifier
                    .size(22.dp)
                    .alpha(if (selected) 1f else 0.55f)
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(if (selected) 4.dp else 0.dp)
                    .background(if (selected) selectedColor else Color.Transparent, CircleShape)
            )
        }
    }
}

private fun navigateToBottomBarRoute(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
