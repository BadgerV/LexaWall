#!/bin/bash
# ============================================================================
# setup_lexawall.sh — Generates the entire LexaWall Android project
# Usage: chmod +x setup_lexawall.sh && ./setup_lexawall.sh
# ============================================================================
set -e

ROOT="LexaWall"
SRC="$ROOT/app/src/main/java/com/lexawall"
RES="$ROOT/app/src/main/res"

echo "🏗️  Creating LexaWall project structure..."

# ── Directory structure ──
mkdir -p "$SRC/core/ui"
mkdir -p "$SRC/core/components"
mkdir -p "$SRC/core/navigation"
mkdir -p "$SRC/domain/model"
mkdir -p "$SRC/domain/repository"
mkdir -p "$SRC/data/fake"
mkdir -p "$SRC/feature/onboarding"
mkdir -p "$SRC/feature/home"
mkdir -p "$SRC/feature/quiz"
mkdir -p "$SRC/feature/flashcards"
mkdir -p "$SRC/feature/worddetail"
mkdir -p "$SRC/feature/settings"
mkdir -p "$SRC/feature/milestones"
mkdir -p "$RES/values"
mkdir -p "$RES/drawable"
mkdir -p "$ROOT/gradle/wrapper"

# ============================================================================
# BUILD FILES
# ============================================================================

cat > "$ROOT/settings.gradle.kts" << 'EOF'
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "LexaWall"
include(":app")
EOF

cat > "$ROOT/build.gradle.kts" << 'EOF'
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false
}
EOF

cat > "$ROOT/gradle.properties" << 'EOF'
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
EOF

cat > "$ROOT/gradle/wrapper/gradle-wrapper.properties" << 'EOF'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.2-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF

cat > "$ROOT/app/build.gradle.kts" << 'EOF'
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.lexawall"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lexawall"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "2.4.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    implementation("androidx.navigation:navigation-compose:2.7.6")

    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
EOF

cat > "$ROOT/app/proguard-rules.pro" << 'EOF'
-keepattributes *Annotation*
EOF

# ============================================================================
# ANDROID RESOURCES
# ============================================================================

cat > "$ROOT/app/src/main/AndroidManifest.xml" << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:allowBackup="true"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.LexaWall">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.LexaWall">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
EOF

cat > "$RES/values/strings.xml" << 'EOF'
<resources>
    <string name="app_name">LexaWall</string>
</resources>
EOF

cat > "$RES/values/themes.xml" << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.LexaWall" parent="android:Theme.Material.NoActionBar">
        <item name="android:windowBackground">#FF1F1C13</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
    </style>
</resources>
EOF

# ============================================================================
# MAIN ACTIVITY
# ============================================================================

cat > "$SRC/MainActivity.kt" << 'EOF'
package com.lexawall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.lexawall.core.navigation.NavGraph
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaWallTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            LexaWallTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LexaColors.BackgroundDark
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}
EOF

# ============================================================================
# CORE / UI
# ============================================================================

cat > "$SRC/core/ui/Color.kt" << 'EOF'
package com.lexawall.core.ui

import androidx.compose.ui.graphics.Color

object LexaColors {
    val Primary = Color(0xFFC9A74A)
    val BackgroundDark = Color(0xFF1F1C13)
    val SplashBg = Color(0xFF080810)
    val SurfaceDark = Color(0xFF2A261A)
    val SurfaceHighlight = Color(0xFF393221)
    val CardDark = Color(0xFF121220)
    val Success = Color(0xFF64DC82)
    val ErrorRed = Color(0xFFD32F2F)
    val TextPrimary = Color(0xFFF2F0E9)
    val TextSecondary = Color(0xFFB8B2A3)
    val TextMuted = Color(0xFF666680)
    val BrandGold = Color(0xFFF0D080)
    val WhiteAlpha10 = Color(0x1AFFFFFF)

    val SuccessDark = Color(0xFF1B3A25)
    val ErrorDark = Color(0xFF3A1B1B)
    val PrimaryDark = Color(0xFF2A2310)
    val Transparent = Color.Transparent
    val White = Color.White
    val Black = Color.Black

    val MidnightGradientTop = Color(0xFF0D0D1A)
    val MidnightGradientBottom = Color(0xFF1A1A2E)
    val SavannaGradientTop = Color(0xFF2A1F0A)
    val SavannaGradientBottom = Color(0xFF3D2E14)
    val LagoonGradientTop = Color(0xFF0A1F2A)
    val LagoonGradientBottom = Color(0xFF143D3D)
    val HarmattanGradientTop = Color(0xFF1A1510)
    val HarmattanGradientBottom = Color(0xFF2E2A1A)
}
EOF

cat > "$SRC/core/ui/Type.kt" << 'EOF'
package com.lexawall.core.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val PlayfairDisplay = FontFamily.Serif
val DmSans = FontFamily.SansSerif
val DmMono = FontFamily.Monospace

object LexaType {

    val displayLarge = TextStyle(
        fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic, fontSize = 48.sp,
        lineHeight = 56.sp, letterSpacing = (-0.5).sp
    )
    val displayMedium = TextStyle(
        fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic, fontSize = 40.sp,
        lineHeight = 48.sp, letterSpacing = (-0.25).sp
    )
    val displaySmall = TextStyle(
        fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold,
        fontSize = 36.sp, lineHeight = 44.sp,
    )
    val headlineLarge = TextStyle(
        fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 40.sp,
    )
    val headlineMedium = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 36.sp,
    )
    val headlineSmall = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 32.sp,
    )
    val titleLarge = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp,
    )
    val titleMedium = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 24.sp,
    )
    val titleSmall = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp,
    )
    val bodyLarge = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp,
    )
    val bodyMedium = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp,
    )
    val bodySmall = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp,
    )
    val labelLarge = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.Bold,
        fontSize = 16.sp, lineHeight = 24.sp,
    )
    val labelMedium = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp,
    )
    val labelSmall = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp,
    )
    val overline = TextStyle(
        fontFamily = DmMono, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 2.sp,
    )
    val overlineSmall = TextStyle(
        fontFamily = DmMono, fontWeight = FontWeight.Normal,
        fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 1.5.sp,
    )
    val mono = TextStyle(
        fontFamily = DmMono, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp,
    )
    val monoSmall = TextStyle(
        fontFamily = DmMono, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp,
    )
    val wordDisplay = TextStyle(
        fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic, fontSize = 44.sp,
        lineHeight = 52.sp, letterSpacing = (-0.5).sp,
    )
    val wordMedium = TextStyle(
        fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic, fontSize = 36.sp, lineHeight = 44.sp,
    )
    val scoreDisplay = TextStyle(
        fontFamily = DmSans, fontWeight = FontWeight.ExtraBold,
        fontSize = 72.sp, lineHeight = 80.sp, letterSpacing = (-2).sp,
    )
}

val LexaTypography = Typography(
    displayLarge = LexaType.displayLarge,
    displayMedium = LexaType.displayMedium,
    displaySmall = LexaType.displaySmall,
    headlineLarge = LexaType.headlineLarge,
    headlineMedium = LexaType.headlineMedium,
    headlineSmall = LexaType.headlineSmall,
    titleLarge = LexaType.titleLarge,
    titleMedium = LexaType.titleMedium,
    titleSmall = LexaType.titleSmall,
    bodyLarge = LexaType.bodyLarge,
    bodyMedium = LexaType.bodyMedium,
    bodySmall = LexaType.bodySmall,
    labelLarge = LexaType.labelLarge,
    labelMedium = LexaType.labelMedium,
    labelSmall = LexaType.labelSmall,
)
EOF

cat > "$SRC/core/ui/Theme.kt" << 'EOF'
package com.lexawall.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val DarkColorScheme = darkColorScheme(
    primary = LexaColors.Primary,
    onPrimary = LexaColors.BackgroundDark,
    secondary = LexaColors.BrandGold,
    onSecondary = LexaColors.BackgroundDark,
    background = LexaColors.BackgroundDark,
    onBackground = LexaColors.TextPrimary,
    surface = LexaColors.SurfaceDark,
    onSurface = LexaColors.TextPrimary,
    surfaceVariant = LexaColors.SurfaceHighlight,
    onSurfaceVariant = LexaColors.TextSecondary,
    error = LexaColors.ErrorRed,
    onError = LexaColors.White,
    outline = LexaColors.WhiteAlpha10,
)

val LocalLexaColors = staticCompositionLocalOf { LexaColors }

@Composable
fun LexaWallTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLexaColors provides LexaColors) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = LexaTypography,
            content = content
        )
    }
}
EOF

echo "✅ Core UI layer done"

# ============================================================================
# DOMAIN
# ============================================================================

cat > "$SRC/domain/model/Models.kt" << 'EOF'
package com.lexawall.domain.model

data class Word(
    val id: String,
    val text: String,
    val partOfSpeech: String,
    val level: String,
    val definition: String,
    val exampleSentence: String,
    val pronunciation: String = "",
    val etymology: String = "",
)

data class QuizQuestion(
    val word: Word,
    val options: List<String>,
    val correctIndex: Int,
)

data class QuizResult(
    val score: Int,
    val total: Int,
    val headline: String,
)

data class FlashcardItem(
    val word: Word,
    val isRevealed: Boolean = false,
)

data class FlashcardSessionResult(
    val knewIt: Int,
    val stillLearning: Int,
)

data class UserStats(
    val streakDays: Int,
    val wordsMastered: Int,
    val quizScore: Int,
    val quizTotal: Int,
)

data class ThemeOption(
    val id: String,
    val name: String,
    val isActive: Boolean = false,
)

sealed class QuizOptionState {
    data object Default : QuizOptionState()
    data object Selected : QuizOptionState()
    data object Correct : QuizOptionState()
    data object Wrong : QuizOptionState()
}
EOF

cat > "$SRC/domain/repository/Repositories.kt" << 'EOF'
package com.lexawall.domain.repository

import com.lexawall.domain.model.*

interface WordRepository {
    fun getWallpaperWord(): Word
    fun getWordById(id: String): Word
    fun getFlashcardWords(): List<Word>
}

interface QuizRepository {
    fun getQuizQuestions(): List<QuizQuestion>
    fun getQuizResult(score: Int, total: Int): QuizResult
}

interface UserRepository {
    fun getUserStats(): UserStats
    fun getGreeting(): String
}

interface ThemeRepository {
    fun getThemes(): List<ThemeOption>
}
EOF

echo "✅ Domain layer done"

# ============================================================================
# DATA / FAKE
# ============================================================================

cat > "$SRC/data/fake/FakeRepositories.kt" << 'DATAEOF'
package com.lexawall.data.fake

import com.lexawall.domain.model.*
import com.lexawall.domain.repository.*
import java.util.Calendar

object FakeWords {
    val ephemeral = Word(
        id = "ephemeral", text = "ephemeral", partOfSpeech = "Adjective", level = "B2",
        definition = "Lasting for a very short time.",
        exampleSentence = "Fashions are ephemeral, changing with every season.",
        pronunciation = "/əˈfem(ə)rəl/",
        etymology = "From Greek ephēmeros 'lasting only a day'."
    )
    val ambiguous = Word(
        id = "ambiguous", text = "Ambiguous", partOfSpeech = "Adjective", level = "B2",
        definition = "Open to more than one interpretation; having a double meaning.",
        exampleSentence = "The results of the experiment were ambiguous.",
        pronunciation = "/amˈbɪɡjuəs/",
        etymology = "From Latin ambiguus 'doubtful'."
    )
    val serendipity = Word(
        id = "serendipity", text = "Serendipity", partOfSpeech = "Noun", level = "C1",
        definition = "The occurrence of events by chance in a happy way.",
        exampleSentence = "A fortunate stroke of serendipity brought them together.",
        pronunciation = "/ˌsɛr.ənˈdɪp.ɪ.ti/",
        etymology = "Coined by Horace Walpole in 1754."
    )
    val resilient = Word(
        id = "resilient", text = "Resilient", partOfSpeech = "Adjective", level = "B2",
        definition = "Able to withstand or recover quickly from difficult conditions.",
        exampleSentence = "The community remained resilient after the storm.",
        pronunciation = "/rɪˈzɪl.i.ənt/",
        etymology = "From Latin resilire 'to recoil'."
    )
    val nuance = Word(
        id = "nuance", text = "Nuance", partOfSpeech = "Noun", level = "C1",
        definition = "A subtle difference in or shade of meaning, expression, or sound.",
        exampleSentence = "The nuances of the language are difficult to master.",
        pronunciation = "/ˈnjuː.ɑːns/",
        etymology = "From French nuer 'to shade'."
    )
    val allWords = listOf(ambiguous, ephemeral, serendipity, resilient, nuance)
    val flashcardWords = listOf(resilient, ephemeral, nuance, serendipity)
}

class FakeWordRepository : WordRepository {
    override fun getWallpaperWord(): Word = FakeWords.ephemeral
    override fun getWordById(id: String): Word =
        FakeWords.allWords.firstOrNull { it.id == id } ?: FakeWords.ephemeral
    override fun getFlashcardWords(): List<Word> = FakeWords.flashcardWords
}

class FakeQuizRepository : QuizRepository {
    override fun getQuizQuestions(): List<QuizQuestion> = listOf(
        QuizQuestion(word = FakeWords.ambiguous, options = listOf(
            "Open to more than one interpretation; having a double meaning.",
            "Clear and precise; without any doubt or confusion.",
            "Relating to the surrounding area or environment."
        ), correctIndex = 0),
        QuizQuestion(word = FakeWords.ephemeral, options = listOf(
            "Extremely large or great.",
            "Lasting for a very short time.",
            "Existing since the beginning of time."
        ), correctIndex = 1),
        QuizQuestion(word = FakeWords.serendipity, options = listOf(
            "A feeling of intense longing.",
            "A tendency to see the worst in things.",
            "The occurrence of events by chance in a happy way."
        ), correctIndex = 2),
        QuizQuestion(word = FakeWords.resilient, options = listOf(
            "Able to withstand or recover quickly from difficult conditions.",
            "Tending to give in to pressure easily.",
            "Showing no emotion or feeling."
        ), correctIndex = 0),
        QuizQuestion(word = FakeWords.nuance, options = listOf(
            "A loud and obvious statement.",
            "A subtle difference in meaning, expression, or sound.",
            "An extreme reaction to a situation."
        ), correctIndex = 1)
    )
    override fun getQuizResult(score: Int, total: Int): QuizResult = QuizResult(
        score = score, total = total,
        headline = when {
            score == total -> "Flawless."
            score >= total * 0.8 -> "Honestly impressive."
            score >= total * 0.5 -> "Solid effort."
            else -> "Keep going."
        }
    )
}

class FakeUserRepository : UserRepository {
    override fun getUserStats(): UserStats = UserStats(
        streakDays = 12, wordsMastered = 42, quizScore = 11, quizTotal = 12
    )
    override fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "GOOD MORNING"
            hour < 17 -> "GOOD AFTERNOON"
            else -> "GOOD EVENING"
        }
    }
}

class FakeThemeRepository : ThemeRepository {
    override fun getThemes(): List<ThemeOption> = listOf(
        ThemeOption(id = "midnight", name = "Midnight", isActive = true),
        ThemeOption(id = "savanna", name = "Savanna"),
        ThemeOption(id = "lagoon", name = "Lagoon"),
        ThemeOption(id = "harmattan", name = "Harmattan"),
    )
}
DATAEOF

echo "✅ Data layer done"

# ============================================================================
# NAVIGATION
# ============================================================================

cat > "$SRC/core/navigation/Screen.kt" << 'EOF'
package com.lexawall.core.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object OnboardingWallpaper : Screen("onboarding_wallpaper")
    data object OnboardingSetup : Screen("onboarding_setup")
    data object OnboardingNotifications : Screen("onboarding_notifications")
    data object Home : Screen("home")
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
    data object FlashcardsWord : Screen("flashcards_word/{cardIndex}") {
        fun createRoute(cardIndex: Int) = "flashcards_word/$cardIndex"
    }
    data object FlashcardsRevealed : Screen("flashcards_revealed/{cardIndex}") {
        fun createRoute(cardIndex: Int) = "flashcards_revealed/$cardIndex"
    }
    data object FlashcardsSummary : Screen("flashcards_summary")
    data object WordDetail : Screen("word_detail/{wordId}") {
        fun createRoute(wordId: String) = "word_detail/$wordId"
    }
    data object MiniQuizQuestion : Screen("mini_quiz_question/{wordId}") {
        fun createRoute(wordId: String) = "mini_quiz_question/$wordId"
    }
    data object MiniQuizFeedback : Screen("mini_quiz_feedback/{wordId}/{isCorrect}") {
        fun createRoute(wordId: String, isCorrect: Boolean) = "mini_quiz_feedback/$wordId/$isCorrect"
    }
    data object Settings : Screen("settings")
    data object ThemePicker : Screen("theme_picker")
    data object Graduation : Screen("graduation")
    data object LevelCompletion : Screen("level_completion")
    data object Day30Summary : Screen("day_30_summary")
}
EOF

echo "✅ Navigation Screen.kt done"
echo "📝 Writing NavGraph.kt..."

cat > "$SRC/core/navigation/NavGraph.kt" << 'EOF'
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
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(onTimeout = {
                navController.navigate(Screen.OnboardingWallpaper.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }
        composable(Screen.OnboardingWallpaper.route) {
            OnboardingWallpaperScreen(onSetWallpaper = {
                navController.navigate(Screen.OnboardingSetup.route)
            })
        }
        composable(Screen.OnboardingSetup.route) {
            OnboardingSetupScreen(
                onBack = { navController.popBackStack() },
                onOpenPicker = { navController.navigate(Screen.OnboardingNotifications.route) },
                onNotNow = { navController.navigate(Screen.OnboardingNotifications.route) }
            )
        }
        composable(Screen.OnboardingNotifications.route) {
            OnboardingNotificationsScreen(
                onAllow = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Splash.route) { inclusive = true } } },
                onNoThanks = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Splash.route) { inclusive = true } } }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onStartQuiz = { navController.navigate(Screen.QuizQuestion.createRoute(0)) },
                onOpenFlashcards = { navController.navigate(Screen.FlashcardsWord.createRoute(0)) },
                onWordTap = { wordId -> navController.navigate(Screen.WordDetail.createRoute(wordId)) }
            )
        }
        composable(Screen.QuizQuestion.route, arguments = listOf(navArgument("questionIndex") { type = NavType.IntType })) { entry ->
            val idx = entry.arguments?.getInt("questionIndex") ?: 0
            QuizQuestionScreen(questionIndex = idx,
                onCorrect = { navController.navigate(Screen.QuizCorrect.createRoute(idx)) { popUpTo(Screen.QuizQuestion.createRoute(idx)) { inclusive = true } } },
                onWrong = { navController.navigate(Screen.QuizWrong.createRoute(idx)) { popUpTo(Screen.QuizQuestion.createRoute(idx)) { inclusive = true } } }
            )
        }
        composable(Screen.QuizCorrect.route, arguments = listOf(navArgument("questionIndex") { type = NavType.IntType })) { entry ->
            val idx = entry.arguments?.getInt("questionIndex") ?: 0
            QuizCorrectScreen(questionIndex = idx, onContinue = { next, isLast ->
                if (isLast) navController.navigate(Screen.QuizEnd.createRoute(11, 13)) { popUpTo(Screen.Home.route) }
                else navController.navigate(Screen.QuizQuestion.createRoute(next)) { popUpTo(Screen.Home.route) }
            })
        }
        composable(Screen.QuizWrong.route, arguments = listOf(navArgument("questionIndex") { type = NavType.IntType })) { entry ->
            val idx = entry.arguments?.getInt("questionIndex") ?: 0
            QuizWrongScreen(questionIndex = idx, onGotIt = { next, isLast ->
                if (isLast) navController.navigate(Screen.QuizEnd.createRoute(11, 13)) { popUpTo(Screen.Home.route) }
                else navController.navigate(Screen.QuizQuestion.createRoute(next)) { popUpTo(Screen.Home.route) }
            })
        }
        composable(Screen.QuizEnd.route, arguments = listOf(navArgument("score") { type = NavType.IntType }, navArgument("total") { type = NavType.IntType })) { entry ->
            QuizEndScreen(score = entry.arguments?.getInt("score") ?: 11, total = entry.arguments?.getInt("total") ?: 13,
                onBackToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } })
        }
        composable(Screen.FlashcardsWord.route, arguments = listOf(navArgument("cardIndex") { type = NavType.IntType })) { entry ->
            val idx = entry.arguments?.getInt("cardIndex") ?: 0
            FlashcardsWordScreen(cardIndex = idx,
                onReveal = { navController.navigate(Screen.FlashcardsRevealed.createRoute(idx)) { popUpTo(Screen.FlashcardsWord.createRoute(idx)) { inclusive = true } } },
                onStillLearning = { n, l -> if (l) navController.navigate(Screen.FlashcardsSummary.route) { popUpTo(Screen.Home.route) } else navController.navigate(Screen.FlashcardsWord.createRoute(n)) { popUpTo(Screen.Home.route) } },
                onKnowIt = { n, l -> if (l) navController.navigate(Screen.FlashcardsSummary.route) { popUpTo(Screen.Home.route) } else navController.navigate(Screen.FlashcardsWord.createRoute(n)) { popUpTo(Screen.Home.route) } },
                onClose = { navController.popBackStack() }
            )
        }
        composable(Screen.FlashcardsRevealed.route, arguments = listOf(navArgument("cardIndex") { type = NavType.IntType })) { entry ->
            val idx = entry.arguments?.getInt("cardIndex") ?: 0
            FlashcardsRevealedScreen(cardIndex = idx,
                onFlipBack = { navController.popBackStack() },
                onStillLearning = { n, l -> if (l) navController.navigate(Screen.FlashcardsSummary.route) { popUpTo(Screen.Home.route) } else navController.navigate(Screen.FlashcardsWord.createRoute(n)) { popUpTo(Screen.Home.route) } },
                onKnowIt = { n, l -> if (l) navController.navigate(Screen.FlashcardsSummary.route) { popUpTo(Screen.Home.route) } else navController.navigate(Screen.FlashcardsWord.createRoute(n)) { popUpTo(Screen.Home.route) } },
                onClose = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } }
            )
        }
        composable(Screen.FlashcardsSummary.route) {
            FlashcardsSessionSummaryScreen(
                onStartQuiz = { navController.navigate(Screen.QuizQuestion.createRoute(0)) { popUpTo(Screen.Home.route) } },
                onBackToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } }
            )
        }
        composable(Screen.WordDetail.route, arguments = listOf(navArgument("wordId") { type = NavType.StringType })) { entry ->
            val wId = entry.arguments?.getString("wordId") ?: "ephemeral"
            WordDetailScreen(wordId = wId, onBack = { navController.popBackStack() },
                onQuizMe = { navController.navigate(Screen.MiniQuizQuestion.createRoute(it)) })
        }
        composable(Screen.MiniQuizQuestion.route, arguments = listOf(navArgument("wordId") { type = NavType.StringType })) { entry ->
            val wId = entry.arguments?.getString("wordId") ?: "ephemeral"
            MiniQuizQuestionScreen(wordId = wId, onAnswer = { isCorrect ->
                navController.navigate(Screen.MiniQuizFeedback.createRoute(wId, isCorrect)) { popUpTo(Screen.MiniQuizQuestion.createRoute(wId)) { inclusive = true } }
            })
        }
        composable(Screen.MiniQuizFeedback.route, arguments = listOf(navArgument("wordId") { type = NavType.StringType }, navArgument("isCorrect") { type = NavType.BoolType })) { entry ->
            val wId = entry.arguments?.getString("wordId") ?: "ephemeral"
            val ok = entry.arguments?.getBoolean("isCorrect") ?: true
            MiniQuizFeedbackScreen(wordId = wId, isCorrect = ok, onBackToWord = {
                navController.navigate(Screen.WordDetail.createRoute(wId)) { popUpTo(Screen.WordDetail.createRoute(wId)) { inclusive = true } }
            })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() }, onThemeTap = { navController.navigate(Screen.ThemePicker.route) },
                onResetConfirmed = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } })
        }
        composable(Screen.ThemePicker.route) { ThemePickerScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.Graduation.route) { GraduationScreen(onKeepGoing = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } }) }
        composable(Screen.LevelCompletion.route) { LevelCompletionScreen(onContinue = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } }) }
        composable(Screen.Day30Summary.route) { Day30SummaryScreen(onBackToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } }) }
    }
}
EOF

echo "✅ NavGraph done"

# ============================================================================
# SHARED COMPONENTS
# ============================================================================

cat > "$SRC/core/components/Components.kt" << 'EOF'
package com.lexawall.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.ui.*
import com.lexawall.domain.model.QuizOptionState

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, showArrow: Boolean = false, enabled: Boolean = true) {
    Button(onClick = onClick, modifier = modifier.fillMaxWidth().height(56.dp), enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = LexaColors.Primary, contentColor = LexaColors.BackgroundDark,
            disabledContainerColor = LexaColors.Primary.copy(alpha = 0.4f), disabledContentColor = LexaColors.BackgroundDark.copy(alpha = 0.5f)),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text = text, style = LexaType.labelLarge)
        if (showArrow) { Spacer(Modifier.width(8.dp)); Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(20.dp)) }
    }
}

@Composable
fun GhostButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, textColor: Color = LexaColors.TextMuted) {
    TextButton(onClick = onClick, modifier = modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
        Text(text = text, style = LexaType.labelMedium, color = textColor)
    }
}

@Composable
fun OverlineTag(text: String, modifier: Modifier = Modifier, color: Color = LexaColors.Primary) {
    Text(text = text.uppercase(), style = LexaType.overline, color = color, modifier = modifier)
}

@Composable
fun SmallTag(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(4.dp)).background(LexaColors.SurfaceHighlight).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(text = text.uppercase(), style = LexaType.overlineSmall, color = LexaColors.TextSecondary)
    }
}

@Composable
fun ProgressDotsRow(total: Int, current: Int, answered: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 0 until total) {
            val color = when { i < answered -> LexaColors.Primary; i == current -> LexaColors.TextPrimary.copy(alpha = 0.9f); else -> LexaColors.SurfaceHighlight }
            Box(modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(color))
        }
    }
}

@Composable
fun QuizOptionRow(text: String, state: QuizOptionState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (bgColor, borderColor, radioColor) = when (state) {
        QuizOptionState.Default -> Triple(LexaColors.SurfaceDark, LexaColors.SurfaceHighlight, LexaColors.TextMuted)
        QuizOptionState.Selected -> Triple(LexaColors.Primary.copy(alpha = 0.1f), LexaColors.Primary, LexaColors.Primary)
        QuizOptionState.Correct -> Triple(LexaColors.Success.copy(alpha = 0.1f), LexaColors.Success, LexaColors.Success)
        QuizOptionState.Wrong -> Triple(LexaColors.ErrorRed.copy(alpha = 0.1f), LexaColors.ErrorRed, LexaColors.ErrorRed)
    }
    Row(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bgColor)
        .border(width = if (state == QuizOptionState.Default) 1.dp else 2.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
        .clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(24.dp).clip(CircleShape).then(
            when (state) { QuizOptionState.Default -> Modifier.border(2.dp, radioColor, CircleShape); else -> Modifier.background(radioColor) }
        ), contentAlignment = Alignment.Center) {
            if (state == QuizOptionState.Selected || state == QuizOptionState.Correct) Icon(Icons.Default.Check, null, Modifier.size(14.dp), tint = LexaColors.BackgroundDark)
            else if (state == QuizOptionState.Wrong) Icon(Icons.Default.Close, null, Modifier.size(14.dp), tint = LexaColors.White)
        }
        Text(text = text, style = LexaType.bodyMedium, color = LexaColors.TextPrimary, modifier = Modifier.weight(1f))
    }
}

@Composable
fun GoldDivider(modifier: Modifier = Modifier) {
    Box(modifier = modifier.width(48.dp).height(1.dp).background(LexaColors.Primary.copy(alpha = 0.5f)))
}

@Composable
fun StreakBadge(days: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.clip(RoundedCornerShape(20.dp)).background(LexaColors.Primary.copy(alpha = 0.15f)).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("🔥", fontSize = 14.sp); Text("$days day streak", style = LexaType.monoSmall, color = LexaColors.Primary)
    }
}

@Composable
fun TopBarRow(modifier: Modifier = Modifier, onBack: (() -> Unit)? = null, onClose: (() -> Unit)? = null, title: String? = null) {
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = LexaColors.TextPrimary) } else Spacer(Modifier.size(48.dp))
        if (title != null) Text(title, style = LexaType.titleMedium, color = LexaColors.TextPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center) else Spacer(Modifier.weight(1f))
        if (onClose != null) IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Close", tint = LexaColors.TextPrimary) } else Spacer(Modifier.size(48.dp))
    }
}

@Composable
fun GlowCircle(icon: @Composable () -> Unit, glowColor: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(96.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(96.dp).clip(CircleShape).background(glowColor.copy(alpha = 0.15f)))
        Box(Modifier.size(72.dp).clip(CircleShape).background(glowColor.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) { icon() }
    }
}

@Composable
fun PillBadge(text: String, textColor: Color, backgroundColor: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(20.dp)).background(backgroundColor).padding(horizontal = 14.dp, vertical = 6.dp)) {
        Text(text, style = LexaType.monoSmall, color = textColor)
    }
}
EOF

echo "✅ Shared components done"

# ============================================================================
# FEATURE: ONBOARDING
# ============================================================================

cat > "$SRC/feature/onboarding/SplashScreen.kt" << 'EOF'
package com.lexawall.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.ui.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) { delay(1800L); onTimeout() }
    Box(Modifier.fillMaxSize().background(LexaColors.SplashBg), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("LexaWall", style = LexaType.displayLarge.copy(fontFamily = PlayfairDisplay, fontStyle = FontStyle.Italic, fontSize = 42.sp, letterSpacing = (-0.5).sp), color = LexaColors.BrandGold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text("Your wallpaper teaches you.", style = LexaType.bodyLarge.copy(fontSize = 18.sp, letterSpacing = 0.5.sp), color = LexaColors.TextMuted, textAlign = TextAlign.Center)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun SplashScreenPreview() { LexaWallTheme { SplashScreen {} } }
EOF

cat > "$SRC/feature/onboarding/OnboardingWallpaperScreen.kt" << 'EOF'
package com.lexawall.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.*
import com.lexawall.core.ui.*

@Composable
fun OnboardingWallpaperScreen(onSetWallpaper: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF181510), Color(0xFF0D0B08))))) {
        Column(Modifier.fillMaxSize().padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("ADJECTIVE", style = LexaType.overline.copy(fontFamily = DmMono, letterSpacing = 2.sp), color = LexaColors.Primary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text("ephemeral", style = LexaType.wordDisplay.copy(fontFamily = PlayfairDisplay, fontStyle = FontStyle.Italic, fontSize = 48.sp), color = LexaColors.BrandGold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp)); GoldDivider(); Spacer(Modifier.height(24.dp))
            Text("lasting only a short time", style = LexaType.bodyLarge.copy(fontSize = 18.sp), color = Color(0xFFD4B870), textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("\"fashions are ephemeral, changing with every season\"", style = LexaType.bodyMedium.copy(fontStyle = FontStyle.Italic), color = Color(0xFFD4B870).copy(alpha = 0.6f), textAlign = TextAlign.Center)
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, LexaColors.BackgroundDark.copy(alpha = 0.9f), LexaColors.BackgroundDark))).padding(horizontal = 24.dp).padding(bottom = 40.dp, top = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            PrimaryButton("Set as my wallpaper", onSetWallpaper); Spacer(Modifier.height(12.dp))
            Text("Takes ~10 seconds.", style = LexaType.bodySmall, color = LexaColors.TextSecondary.copy(alpha = 0.7f), textAlign = TextAlign.Center)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { OnboardingWallpaperScreen {} } }
EOF

cat > "$SRC/feature/onboarding/OnboardingSetupScreen.kt" << 'EOF'
package com.lexawall.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lexawall.core.components.*
import com.lexawall.core.ui.*

@Composable
fun OnboardingSetupScreen(onBack: () -> Unit, onOpenPicker: () -> Unit, onNotNow: () -> Unit) {
    Column(Modifier.fillMaxSize().background(LexaColors.BackgroundDark)) {
        TopBarRow(onBack = onBack)
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(LexaColors.SurfaceDark)) {
                Box(Modifier.fillMaxWidth().height(180.dp).background(Brush.verticalGradient(listOf(LexaColors.SurfaceHighlight, LexaColors.SurfaceDark))))
                Column(Modifier.fillMaxWidth().padding(24.dp)) {
                    Text("Quick setup", style = LexaType.headlineSmall, color = LexaColors.TextPrimary); Spacer(Modifier.height(20.dp))
                    SetupStep(1, "Tap Set wallpaper"); Spacer(Modifier.height(16.dp))
                    SetupStep(2, "Choose Home screen or Both"); Spacer(Modifier.height(28.dp))
                    PrimaryButton("Open picker", onOpenPicker); Spacer(Modifier.height(8.dp))
                    GhostButton("Not now", onNotNow)
                }
            }
        }
    }
}

@Composable private fun SetupStep(number: Int, text: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(LexaColors.Primary), contentAlignment = Alignment.Center) {
            Text("$number", style = LexaType.labelMedium, color = LexaColors.BackgroundDark, textAlign = TextAlign.Center)
        }; Spacer(Modifier.width(12.dp))
        Text(text, style = LexaType.bodyLarge, color = LexaColors.TextPrimary)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { OnboardingSetupScreen({}, {}, {}) } }
EOF

cat > "$SRC/feature/onboarding/OnboardingNotificationsScreen.kt" << 'EOF'
package com.lexawall.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lexawall.core.components.*
import com.lexawall.core.ui.*

@Composable
fun OnboardingNotificationsScreen(onAllow: () -> Unit, onNoThanks: () -> Unit) {
    Column(Modifier.fillMaxSize().background(LexaColors.BackgroundDark), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).clip(RoundedCornerShape(24.dp)).background(LexaColors.SurfaceDark)) {
            Box(Modifier.fillMaxWidth().height(180.dp).background(Brush.verticalGradient(listOf(LexaColors.SurfaceHighlight, LexaColors.SurfaceDark)))) {
                Box(Modifier.align(Alignment.TopEnd).padding(16.dp).size(40.dp).clip(CircleShape).background(LexaColors.Primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Notifications, null, tint = LexaColors.Primary, modifier = Modifier.size(20.dp))
                }
            }
            Column(Modifier.fillMaxWidth().padding(24.dp)) {
                Text("Remind you about the daily quiz?", style = LexaType.headlineSmall, color = LexaColors.TextPrimary); Spacer(Modifier.height(8.dp))
                Text("One notification a day. Optional.", style = LexaType.bodyMedium, color = LexaColors.TextSecondary); Spacer(Modifier.height(28.dp))
                PrimaryButton("Allow reminders", onAllow); Spacer(Modifier.height(8.dp))
                GhostButton("No thanks", onNoThanks)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { OnboardingNotificationsScreen({}, {}) } }
EOF

echo "✅ Onboarding screens done"

# ============================================================================
# FEATURE: HOME
# ============================================================================

cat > "$SRC/feature/home/HomeUiState.kt" << 'EOF'
package com.lexawall.feature.home

import com.lexawall.domain.model.*

data class HomeUiState(
    val greeting: String = "GOOD MORNING",
    val screenState: HomeScreenState = HomeScreenState.DEFAULT,
    val wallpaperWord: Word = Word("", "", "", "", "", ""),
    val stats: UserStats = UserStats(0, 0, 0, 0),
    val showWallpaperBanner: Boolean = false,
)

enum class HomeScreenState { DEFAULT, QUIZ_COMPLETED, WELCOME_BACK, RESUME_QUIZ, WALLPAPER_NOT_SET, DAY_1 }
EOF

cat > "$SRC/feature/home/HomeViewModel.kt" << 'EOF'
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
    init { loadData() }
    private fun loadData() {
        _uiState.update { it.copy(greeting = userRepository.getGreeting(), wallpaperWord = wordRepository.getWallpaperWord(), stats = userRepository.getUserStats(), screenState = HomeScreenState.DEFAULT) }
    }
    fun cycleState() {
        val nextIndex = (allStates.indexOf(_uiState.value.screenState) + 1) % allStates.size
        val nextState = allStates[nextIndex]
        _uiState.update { it.copy(screenState = nextState, showWallpaperBanner = nextState == HomeScreenState.WALLPAPER_NOT_SET) }
    }
    fun dismissBanner() { _uiState.update { it.copy(showWallpaperBanner = false) } }
}
EOF

cat > "$SRC/feature/home/HomeScreen.kt" << 'EOF'
package com.lexawall.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lexawall.core.components.*
import com.lexawall.core.ui.*
import com.lexawall.domain.model.*

@Composable
fun HomeScreen(onNavigateToSettings: () -> Unit, onStartQuiz: () -> Unit, onOpenFlashcards: () -> Unit, onWordTap: (String) -> Unit, viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    HomeContent(uiState, onNavigateToSettings, onStartQuiz, onOpenFlashcards, onWordTap, viewModel::cycleState, viewModel::dismissBanner)
}

@Composable
private fun HomeContent(uiState: HomeUiState, onSettings: () -> Unit, onStartQuiz: () -> Unit, onFlashcards: () -> Unit, onWordTap: (String) -> Unit, onCycleState: () -> Unit, onDismissBanner: () -> Unit) {
    Column(Modifier.fillMaxSize().background(LexaColors.BackgroundDark)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { OverlineTag(uiState.greeting, color = LexaColors.Primary)
                if (uiState.screenState != HomeScreenState.DAY_1) { Spacer(Modifier.height(4.dp)); StreakBadge(uiState.stats.streakDays) }
            }
            IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Settings", tint = LexaColors.TextPrimary, modifier = Modifier.size(24.dp)) }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            if (uiState.showWallpaperBanner) { WallpaperBanner(onDismissBanner); Spacer(Modifier.height(16.dp)) }
            if (uiState.screenState == HomeScreenState.WELCOME_BACK) {
                Text("Welcome back.", style = LexaType.headlineLarge, color = LexaColors.TextPrimary); Spacer(Modifier.height(4.dp))
                Text("Your words are waiting.", style = LexaType.bodyLarge, color = LexaColors.TextSecondary); Spacer(Modifier.height(24.dp))
            }
            when (uiState.screenState) {
                HomeScreenState.DEFAULT, HomeScreenState.WALLPAPER_NOT_SET, HomeScreenState.WELCOME_BACK -> QuizAvailableCard(onStartQuiz)
                HomeScreenState.QUIZ_COMPLETED -> QuizCompletedCard()
                HomeScreenState.RESUME_QUIZ -> ResumeQuizCard(onStartQuiz)
                HomeScreenState.DAY_1 -> Day1QuizCard(onStartQuiz)
            }
            Spacer(Modifier.height(20.dp)); WallpaperWordCard(uiState.wallpaperWord) { onWordTap(uiState.wallpaperWord.id) }
            Spacer(Modifier.height(20.dp))
            if (uiState.screenState != HomeScreenState.DAY_1) TextButton(onClick = onFlashcards) { Text("Practice flashcards →", style = LexaType.labelMedium, color = LexaColors.Primary) }
            Spacer(Modifier.height(40.dp))
            TextButton(onClick = onCycleState, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Demo: cycle state → (${uiState.screenState.name})", style = LexaType.monoSmall, color = LexaColors.TextMuted) }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable private fun QuizAvailableCard(onStart: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LexaColors.CardDark).border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(16.dp)).padding(24.dp)) {
        OverlineTag("TODAY'S QUIZ"); Spacer(Modifier.height(8.dp))
        Text("Daily Vocabulary", style = LexaType.headlineLarge.copy(fontFamily = PlayfairDisplay), color = LexaColors.TextPrimary); Spacer(Modifier.height(8.dp))
        Text("12 questions · ~4 minutes", style = LexaType.bodyMedium, color = LexaColors.TextMuted); Spacer(Modifier.height(20.dp))
        PrimaryButton("Start quiz", onStart, showArrow = true)
    }
}

@Composable private fun QuizCompletedCard() {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LexaColors.CardDark).border(1.dp, LexaColors.Success.copy(alpha = 0.3f), RoundedCornerShape(16.dp)).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(LexaColors.Success.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, null, tint = LexaColors.Success, modifier = Modifier.size(24.dp)) }
        Spacer(Modifier.height(16.dp)); Text("Done for today.", style = LexaType.headlineSmall, color = LexaColors.Success, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp)); Text("See you tomorrow.", style = LexaType.bodyMedium, color = LexaColors.TextMuted, textAlign = TextAlign.Center)
    }
}

@Composable private fun ResumeQuizCard(onContinue: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LexaColors.CardDark).border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(16.dp)).padding(24.dp)) {
        Text("Continue your quiz?", style = LexaType.titleMedium, color = LexaColors.TextPrimary); Spacer(Modifier.height(4.dp))
        Text("Question 6 of 12", style = LexaType.bodyMedium, color = LexaColors.TextMuted); Spacer(Modifier.height(20.dp))
        PrimaryButton("Continue", onContinue)
    }
}

@Composable private fun Day1QuizCard(onStart: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LexaColors.CardDark).border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(16.dp)).padding(24.dp)) {
        OverlineTag("TODAY'S QUIZ"); Spacer(Modifier.height(8.dp))
        Text("Your first quiz", style = LexaType.headlineLarge.copy(fontFamily = PlayfairDisplay), color = LexaColors.TextPrimary); Spacer(Modifier.height(8.dp))
        Text("12 questions · ~4 minutes", style = LexaType.bodyMedium, color = LexaColors.TextMuted); Spacer(Modifier.height(20.dp))
        PrimaryButton("Start quiz", onStart, showArrow = true)
    }
}

@Composable private fun WallpaperWordCard(word: Word, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LexaColors.CardDark).border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(24.dp)) {
        OverlineTag("ON YOUR WALLPAPER NOW", color = LexaColors.TextMuted); Spacer(Modifier.height(12.dp))
        Text(word.text, style = LexaType.wordDisplay.copy(fontSize = 40.sp), color = LexaColors.TextPrimary); Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("[${word.partOfSpeech.lowercase().take(3)}.]", style = LexaType.monoSmall, color = LexaColors.Primary)
            Text(word.definition.lowercase(), style = LexaType.bodyMedium, color = LexaColors.TextMuted)
        }; Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { SmallTag(word.partOfSpeech); SmallTag(word.level) }
    }
}

@Composable private fun WallpaperBanner(onDismiss: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(LexaColors.Primary.copy(alpha = 0.15f)).border(1.dp, LexaColors.Primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp)).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Set LexaWall as your wallpaper to learn passively. Set now →", style = LexaType.bodySmall, color = LexaColors.Primary, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, "Dismiss", tint = LexaColors.Primary, modifier = Modifier.size(16.dp)) }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { HomeContent(HomeUiState(greeting = "GOOD MORNING", screenState = HomeScreenState.DEFAULT, wallpaperWord = Word("ephemeral", "ephemeral", "Adjective", "B2", "Lasting for a very short time.", "Fashions are ephemeral, changing with every season."), stats = UserStats(12, 42, 11, 12)), {}, {}, {}, {}, {}, {}) } }
EOF

echo "✅ Home screen done"

# ============================================================================
# FEATURE: QUIZ
# ============================================================================

cat > "$SRC/feature/quiz/QuizUiState.kt" << 'EOF'
package com.lexawall.feature.quiz

import com.lexawall.domain.model.*

data class QuizUiState(val questions: List<QuizQuestion> = emptyList(), val currentIndex: Int = 0, val selectedOption: Int? = null, val optionStates: List<QuizOptionState> = emptyList(), val score: Int = 0)
EOF

cat > "$SRC/feature/quiz/QuizViewModel.kt" << 'EOF'
package com.lexawall.feature.quiz

import androidx.lifecycle.ViewModel
import com.lexawall.data.fake.FakeQuizRepository
import com.lexawall.domain.model.QuizOptionState
import com.lexawall.domain.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class QuizViewModel(private val quizRepository: QuizRepository = FakeQuizRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()
    init { val q = quizRepository.getQuizQuestions(); _uiState.update { it.copy(questions = q, optionStates = List(q.firstOrNull()?.options?.size ?: 3) { QuizOptionState.Default }) } }
    fun getQuestion(index: Int) = _uiState.value.questions.getOrNull(index.coerceIn(0, _uiState.value.questions.lastIndex.coerceAtLeast(0)))
    fun getTotalQuestions() = _uiState.value.questions.size
    fun isLastQuestion(index: Int) = index >= _uiState.value.questions.lastIndex
}
EOF

cat > "$SRC/feature/quiz/QuizQuestionScreen.kt" << 'EOF'
package com.lexawall.feature.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lexawall.core.components.*
import com.lexawall.core.ui.*
import com.lexawall.domain.model.QuizOptionState

@Composable
fun QuizQuestionScreen(questionIndex: Int, onCorrect: () -> Unit, onWrong: () -> Unit, quizViewModel: QuizViewModel = viewModel()) {
    val uiState by quizViewModel.uiState.collectAsState()
    val question = quizViewModel.getQuestion(questionIndex) ?: return
    val total = quizViewModel.getTotalQuestions()
    var selectedOption by remember(questionIndex) { mutableIntStateOf(-1) }
    Column(Modifier.fillMaxSize().background(LexaColors.BackgroundDark).padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(48.dp))
        ProgressDotsRow(total = total, current = questionIndex, answered = questionIndex)
        Spacer(Modifier.height(24.dp))
        Text("What does this word mean?", style = LexaType.overline, color = LexaColors.TextSecondary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        Text(question.word.text, style = LexaType.displaySmall.copy(fontFamily = PlayfairDisplay, fontSize = 48.sp), color = LexaColors.TextPrimary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { SmallTag(question.word.partOfSpeech); Spacer(Modifier.padding(horizontal = 4.dp)); SmallTag(question.word.level) }
        Spacer(Modifier.height(40.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.options.forEachIndexed { index, optionText ->
                val state = if (selectedOption == -1) QuizOptionState.Default else if (index == selectedOption) { if (index == question.correctIndex) QuizOptionState.Selected else QuizOptionState.Wrong } else QuizOptionState.Default
                QuizOptionRow(optionText, state) { if (selectedOption == -1) { selectedOption = index; if (index == question.correctIndex) onCorrect() else onWrong() } }
            }
        }
        Spacer(Modifier.weight(1f))
        Text("${questionIndex + 1} of $total", style = LexaType.mono, color = LexaColors.TextMuted, modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { QuizQuestionScreen(0, {}, {}) } }
EOF

cat > "$SRC/feature/quiz/QuizCorrectScreen.kt" << 'EOF'
package com.lexawall.feature.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lexawall.core.components.*
import com.lexawall.core.ui.*

@Composable
fun QuizCorrectScreen(questionIndex: Int, onContinue: (Int, Boolean) -> Unit, quizViewModel: QuizViewModel = viewModel()) {
    val question = quizViewModel.getQuestion(questionIndex) ?: return
    val isLast = quizViewModel.isLastQuestion(questionIndex)
    Column(Modifier.fillMaxSize().background(LexaColors.BackgroundDark)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(80.dp))
            GlowCircle(icon = { Icon(Icons.Default.Check, null, tint = LexaColors.Success, modifier = Modifier.size(36.dp)) }, glowColor = LexaColors.Success)
            Spacer(Modifier.height(24.dp)); Text("That's it.", style = LexaType.headlineLarge, color = LexaColors.TextPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp)); GoldDivider(); Spacer(Modifier.height(24.dp))
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LexaColors.SurfaceDark).padding(20.dp)) {
                Text(question.word.text, style = LexaType.titleLarge, color = LexaColors.Primary); Spacer(Modifier.height(8.dp))
                Text(question.word.definition, style = LexaType.bodyLarge, color = LexaColors.TextSecondary); Spacer(Modifier.height(12.dp))
                Text("\"${question.word.exampleSentence}\"", style = LexaType.bodyMedium.copy(fontStyle = FontStyle.Italic), color = LexaColors.TextPrimary)
            }; Spacer(Modifier.height(16.dp))
            PillBadge("Next review in 4 days", LexaColors.Success, LexaColors.Success.copy(alpha = 0.15f)); Spacer(Modifier.height(32.dp))
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp)) { PrimaryButton("Continue", { onContinue(questionIndex + 1, isLast) }, showArrow = true) }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { QuizCorrectScreen(0, { _, _ -> }) } }
EOF

cat > "$SRC/feature/quiz/QuizWrongScreen.kt" << 'EOF'
package com.lexawall.feature.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lexawall.core.components.*
import com.lexawall.core.ui.*

@Composable
fun QuizWrongScreen(questionIndex: Int, onGotIt: (Int, Boolean) -> Unit, quizViewModel: QuizViewModel = viewModel()) {
    val question = quizViewModel.getQuestion(questionIndex) ?: return
    val isLast = quizViewModel.isLastQuestion(questionIndex)
    Column(Modifier.fillMaxSize().background(LexaColors.BackgroundDark)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(80.dp))
            GlowCircle(icon = { Icon(Icons.Default.Close, null, tint = LexaColors.ErrorRed, modifier = Modifier.size(36.dp)) }, glowColor = LexaColors.ErrorRed)
            Spacer(Modifier.height(24.dp)); Text("Not yet.", style = LexaType.headlineLarge, color = LexaColors.TextPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp)); Text(question.word.text, style = LexaType.titleLarge, color = LexaColors.TextSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LexaColors.SurfaceDark).padding(20.dp)) {
                Text("Correct meaning", style = LexaType.overline, color = LexaColors.Success); Spacer(Modifier.height(12.dp))
                Text(question.word.definition, style = LexaType.bodyLarge, color = LexaColors.Success); Spacer(Modifier.height(12.dp))
                Text("\"${question.word.exampleSentence}\"", style = LexaType.bodyMedium.copy(fontStyle = FontStyle.Italic), color = LexaColors.TextPrimary)
            }; Spacer(Modifier.height(16.dp))
            PillBadge("Back tomorrow.", LexaColors.ErrorRed, LexaColors.ErrorRed.copy(alpha = 0.15f)); Spacer(Modifier.height(32.dp))
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp)) { PrimaryButton("Got it", { onGotIt(questionIndex + 1, isLast) }, showArrow = true) }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { QuizWrongScreen(0, { _, _ -> }) } }
EOF

cat > "$SRC/feature/quiz/QuizEndScreen.kt" << 'EOF'
package com.lexawall.feature.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.ui.*

@Composable
fun QuizEndScreen(score: Int, total: Int, onBackToHome: () -> Unit) {
    Column(Modifier.fillMaxSize().background(LexaColors.BackgroundDark), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(Modifier.weight(1f).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("$score/$total", style = LexaType.scoreDisplay, color = LexaColors.TextPrimary, textAlign = TextAlign.Center); Spacer(Modifier.height(16.dp))
            Text(when { score == total -> "Flawless."; score >= (total * 0.8) -> "Honestly impressive."; score >= (total * 0.5) -> "Solid effort."; else -> "Keep going." }, style = LexaType.headlineLarge, color = LexaColors.TextPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp)); Text("See you tomorrow.", style = LexaType.bodyLarge, color = LexaColors.TextMuted, textAlign = TextAlign.Center)
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp)) { PrimaryButton("Back to home", onBackToHome) }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { QuizEndScreen(11, 13) {} } }
EOF

echo "✅ Quiz screens done"

# ============================================================================
# FEATURE: FLASHCARDS
# ============================================================================

cat > "$SRC/feature/flashcards/FlashcardsWordScreen.kt" << 'EOF'
package com.lexawall.feature.flashcards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.*
import com.lexawall.core.ui.*
import com.lexawall.data.fake.FakeWords

@Composable
fun FlashcardsWordScreen(cardIndex: Int, onReveal: () -> Unit, onStillLearning: (Int, Boolean) -> Unit, onKnowIt: (Int, Boolean) -> Unit, onClose: () -> Unit) {
    val words = FakeWords.flashcardWords; val word = words.getOrNull(cardIndex) ?: words.first()
    val total = words.size; val isLast = cardIndex >= words.lastIndex
    Column(Modifier.fillMaxSize().background(LexaColors.BackgroundDark).padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(48.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${cardIndex + 1} OF $total", style = LexaType.overline.copy(fontFamily = DmMono), color = LexaColors.TextSecondary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).drawBehind { val s = Stroke(3.dp.toPx(), cap = StrokeCap.Round); drawArc(LexaColors.SurfaceHighlight, -90f, 360f, false, s, Offset(s.width/2, s.width/2), Size(size.width-s.width, size.height-s.width)); drawArc(LexaColors.Primary, -90f, 270f, false, s, Offset(s.width/2, s.width/2), Size(size.width-s.width, size.height-s.width)) }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Timer, null, tint = LexaColors.Primary, modifier = Modifier.size(14.dp)) }
                Spacer(Modifier.width(8.dp)); IconButton(onClick = onClose, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Close, "Close", tint = LexaColors.TextPrimary) }
            }
        }
        Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(LexaColors.CardDark).border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(24.dp)).clickable(onClick = onReveal).padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TAP TO REVEAL", style = LexaType.overline.copy(fontFamily = DmMono), color = LexaColors.TextMuted, textAlign = TextAlign.Center); Spacer(Modifier.height(24.dp))
                Text(word.text.lowercase(), style = LexaType.wordDisplay.copy(fontFamily = PlayfairDisplay, fontSize = 44.sp), color = LexaColors.TextPrimary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp)); GoldDivider(); Spacer(Modifier.height(24.dp)); SmallTag("${word.partOfSpeech}  ${word.level}")
            }
        }
        Row(Modifier.fillMaxWidth().padding(bottom = 32.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(12.dp)).background(LexaColors.ErrorRed.copy(alpha = 0.08f)).border(1.dp, LexaColors.ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).clickable { onStillLearning(cardIndex + 1, isLast) }, contentAlignment = Alignment.Center) { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Icon(Icons.Default.History, null, tint = LexaColors.ErrorRed.copy(alpha = 0.8f), modifier = Modifier.size(20.dp)); Text("Still learning", style = LexaType.labelMedium, color = LexaColors.ErrorRed.copy(alpha = 0.8f)) } }
            Box(Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(12.dp)).background(LexaColors.Primary).clickable { onKnowIt(cardIndex + 1, isLast) }, contentAlignment = Alignment.Center) { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Text("I know this", style = LexaType.labelMedium, color = LexaColors.BackgroundDark); Icon(Icons.Default.Check, null, tint = LexaColors.BackgroundDark, modifier = Modifier.size(20.dp)) } }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { FlashcardsWordScreen(0, {}, { _, _ -> }, { _, _ -> }, {}) } }
EOF

cat > "$SRC/feature/flashcards/FlashcardsRevealedScreen.kt" << 'EOF'
package com.lexawall.feature.flashcards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.*
import com.lexawall.core.ui.*
import com.lexawall.data.fake.FakeWords

@Composable
fun FlashcardsRevealedScreen(cardIndex: Int, onFlipBack: () -> Unit, onStillLearning: (Int, Boolean) -> Unit, onKnowIt: (Int, Boolean) -> Unit, onClose: () -> Unit) {
    val words = FakeWords.flashcardWords; val word = words.getOrNull(cardIndex) ?: words.first()
    val total = words.size; val isLast = cardIndex >= words.lastIndex
    Column(Modifier.fillMaxSize().background(LexaColors.BackgroundDark).padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(48.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${cardIndex + 1} OF $total", style = LexaType.overline.copy(fontFamily = DmMono), color = LexaColors.TextSecondary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).drawBehind { val s = Stroke(3.dp.toPx(), cap = StrokeCap.Round); drawArc(LexaColors.SurfaceHighlight, -90f, 360f, false, s, Offset(s.width/2, s.width/2), Size(size.width-s.width, size.height-s.width)); drawArc(LexaColors.Primary, -90f, 216f, false, s, Offset(s.width/2, s.width/2), Size(size.width-s.width, size.height-s.width)) }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Timer, null, tint = LexaColors.Primary, modifier = Modifier.size(14.dp)) }
                Spacer(Modifier.width(8.dp)); IconButton(onClick = onClose, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Close, "Close", tint = LexaColors.TextPrimary) }
            }
        }
        Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(LexaColors.CardDark).border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(24.dp)).clickable(onClick = onFlipBack).padding(36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                SmallTag(word.partOfSpeech); Spacer(Modifier.height(16.dp))
                Text(word.text, style = LexaType.wordDisplay.copy(fontFamily = PlayfairDisplay, fontSize = 40.sp), color = LexaColors.TextPrimary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp)); GoldDivider(); Spacer(Modifier.height(16.dp))
                Text(word.definition, style = LexaType.bodyLarge, color = LexaColors.TextSecondary, textAlign = TextAlign.Center); Spacer(Modifier.height(12.dp))
                Text("\"${word.exampleSentence}\"", style = LexaType.bodyMedium.copy(fontStyle = FontStyle.Italic), color = LexaColors.TextMuted, textAlign = TextAlign.Center)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(12.dp)).background(LexaColors.ErrorRed.copy(alpha = 0.08f)).border(1.dp, LexaColors.ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).clickable { onStillLearning(cardIndex + 1, isLast) }, contentAlignment = Alignment.Center) { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Icon(Icons.Default.History, null, tint = LexaColors.ErrorRed.copy(alpha = 0.8f), modifier = Modifier.size(20.dp)); Text("Still learning", style = LexaType.labelMedium, color = LexaColors.ErrorRed.copy(alpha = 0.8f)) } }
            Box(Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(12.dp)).background(LexaColors.Primary).clickable { onKnowIt(cardIndex + 1, isLast) }, contentAlignment = Alignment.Center) { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Text("I know this", style = LexaType.labelMedium, color = LexaColors.BackgroundDark); Icon(Icons.Default.Check, null, tint = LexaColors.BackgroundDark, modifier = Modifier.size(20.dp)) } }
        }
        Text("TAP CARD TO FLIP BACK", style = LexaType.overlineSmall.copy(fontFamily = DmMono), color = LexaColors.TextMuted, modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { FlashcardsRevealedScreen(0, {}, { _, _ -> }, { _, _ -> }, {}) } }
EOF

cat > "$SRC/feature/flashcards/FlashcardsSessionSummaryScreen.kt" << 'EOF'
package com.lexawall.feature.flashcards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lexawall.core.components.*
import com.lexawall.core.ui.*

@Composable
fun FlashcardsSessionSummaryScreen(onStartQuiz: () -> Unit, onBackToHome: () -> Unit) {
    Column(Modifier.fillMaxSize().background(LexaColors.BackgroundDark), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(Modifier.weight(1f).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            OverlineTag("STACK COMPLETE"); Spacer(Modifier.height(24.dp))
            Text("11 felt familiar", style = LexaType.headlineLarge, color = LexaColors.TextPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(48.dp))
            Column(Modifier.fillMaxWidth(0.75f)) {
                Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { Box(Modifier.size(24.dp).clip(CircleShape).background(LexaColors.Success.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, null, tint = LexaColors.Success, modifier = Modifier.size(14.dp)) }; Text("Knew it", style = LexaType.bodyLarge, color = LexaColors.TextSecondary) }
                    Text("11", style = LexaType.mono, color = LexaColors.TextPrimary)
                }
                HorizontalDivider(color = LexaColors.SurfaceHighlight.copy(alpha = 0.5f))
                Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { Box(Modifier.size(24.dp).clip(CircleShape).background(Color(0xFFFFC107).copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.School, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp)) }; Text("Still learning", style = LexaType.bodyLarge, color = LexaColors.TextSecondary) }
                    Text("4", style = LexaType.mono, color = LexaColors.TextPrimary)
                }
                HorizontalDivider(color = LexaColors.SurfaceHighlight.copy(alpha = 0.5f))
            }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { PrimaryButton("Start today's quiz", onStartQuiz, showArrow = true); GhostButton("Back to home", onBackToHome) }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { FlashcardsSessionSummaryScreen({}, {}) } }
EOF

echo "✅ Flashcards screens done"

# ============================================================================
# FEATURE: WORD DETAIL
# ============================================================================

cat > "$SRC/feature/worddetail/WordDetailScreen.kt" << 'EOF'
package com.lexawall.feature.worddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.*
import com.lexawall.core.ui.*
import com.lexawall.data.fake.FakeWordRepository

@Composable
fun WordDetailScreen(wordId: String, onBack: () -> Unit, onQuizMe: (String) -> Unit) {
    val word = FakeWordRepository().getWordById(wordId)
    Box(Modifier.fillMaxSize().background(LexaColors.BackgroundDark)) {
        Column(Modifier.fillMaxSize()) {
            TopBarRow(onBack = onBack)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(32.dp))
                Box(Modifier.clip(RoundedCornerShape(20.dp)).border(1.dp, LexaColors.Primary.copy(alpha = 0.2f), RoundedCornerShape(20.dp)).background(LexaColors.Primary.copy(alpha = 0.1f)).padding(horizontal = 16.dp, vertical = 6.dp)) { Text("${word.level} · ${word.partOfSpeech}", style = LexaType.overline.copy(fontFamily = DmMono), color = LexaColors.Primary) }
                Spacer(Modifier.height(24.dp))
                Text(word.text, style = LexaType.wordDisplay.copy(fontFamily = PlayfairDisplay, fontSize = 48.sp, fontStyle = FontStyle.Normal), color = LexaColors.TextPrimary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                if (word.pronunciation.isNotEmpty()) Box(Modifier.clip(RoundedCornerShape(20.dp)).background(LexaColors.SurfaceDark).padding(horizontal = 16.dp, vertical = 8.dp)) { Text(word.pronunciation, style = LexaType.mono, color = LexaColors.TextSecondary) }
                Spacer(Modifier.height(24.dp))
                Text(word.definition, style = LexaType.bodyLarge.copy(fontSize = 20.sp), color = LexaColors.TextSecondary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(32.dp)); GoldDivider(); Spacer(Modifier.height(32.dp))
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LexaColors.SurfaceDark).border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(16.dp)).padding(20.dp), horizontalAlignment = Alignment.Start) {
                    OverlineTag("EXAMPLE"); Spacer(Modifier.height(12.dp))
                    Text("\"${word.exampleSentence}\"", style = LexaType.bodyLarge.copy(fontFamily = PlayfairDisplay, fontStyle = FontStyle.Italic), color = LexaColors.TextPrimary)
                }
                Spacer(Modifier.height(24.dp))
                if (word.etymology.isNotEmpty()) Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) { Text("ETYMOLOGY", style = LexaType.overlineSmall, color = LexaColors.TextSecondary); Spacer(Modifier.height(8.dp)); Text(word.etymology, style = LexaType.bodyMedium, color = LexaColors.TextMuted, textAlign = TextAlign.Center) }
                Spacer(Modifier.height(100.dp))
            }
        }
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Brush.verticalGradient(listOf(LexaColors.BackgroundDark.copy(alpha = 0f), LexaColors.BackgroundDark, LexaColors.BackgroundDark))).padding(horizontal = 24.dp).padding(top = 48.dp, bottom = 24.dp)) {
            PrimaryButton("Quiz me on this word") { onQuizMe(word.id) }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { WordDetailScreen("ephemeral", {}, {}) } }
EOF

cat > "$SRC/feature/worddetail/MiniQuizQuestionScreen.kt" << 'EOF'
package com.lexawall.feature.worddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.QuizOptionRow
import com.lexawall.core.ui.*
import com.lexawall.data.fake.FakeWordRepository
import com.lexawall.domain.model.QuizOptionState

@Composable
fun MiniQuizQuestionScreen(wordId: String, onAnswer: (Boolean) -> Unit) {
    val word = FakeWordRepository().getWordById(wordId)
    val options = listOf(word.definition, "Extremely large or great in scale.", "Happening at the same time.")
    var selected by remember { mutableIntStateOf(-1) }
    Column(Modifier.fillMaxSize().background(LexaColors.BackgroundDark).padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(80.dp))
        Text(word.text, style = LexaType.wordDisplay.copy(fontFamily = PlayfairDisplay, fontStyle = FontStyle.Italic, fontSize = 40.sp), color = LexaColors.TextPrimary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { Text("—", style = LexaType.bodyLarge, color = LexaColors.Primary); Text("  Meaning?  ", style = LexaType.bodyLarge, color = LexaColors.TextSecondary); Text("—", style = LexaType.bodyLarge, color = LexaColors.Primary) }
        Spacer(Modifier.height(40.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEachIndexed { i, t ->
                val state = when { selected == -1 -> QuizOptionState.Default; i == selected && i == 0 -> QuizOptionState.Correct; i == selected -> QuizOptionState.Wrong; else -> QuizOptionState.Default }
                QuizOptionRow(t, state) { if (selected == -1) { selected = i; onAnswer(i == 0) } }
            }
        }
        Spacer(Modifier.weight(1f))
        Text("Doesn't affect streak.", style = LexaType.monoSmall.copy(fontFamily = DmMono), color = LexaColors.TextMuted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(progress = { 0.5f }, modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)), color = LexaColors.Primary, trackColor = LexaColors.SurfaceHighlight)
        Spacer(Modifier.height(32.dp))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { MiniQuizQuestionScreen("ephemeral") {} } }
EOF

cat > "$SRC/feature/worddetail/MiniQuizFeedbackScreen.kt" << 'EOF'
package com.lexawall.feature.worddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lexawall.core.components.*
import com.lexawall.core.ui.*
import com.lexawall.data.fake.FakeWordRepository

@Composable
fun MiniQuizFeedbackScreen(wordId: String, isCorrect: Boolean, onBackToWord: () -> Unit) {
    val word = FakeWordRepository().getWordById(wordId)
    val color = if (isCorrect) LexaColors.Success else LexaColors.ErrorRed
    Column(Modifier.fillMaxSize().background(LexaColors.BackgroundDark).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Spacer(Modifier.weight(0.3f))
        GlowCircle(icon = { Icon(if (isCorrect) Icons.Default.Check else Icons.Default.Close, null, tint = color, modifier = Modifier.size(36.dp)) }, glowColor = color)
        Spacer(Modifier.height(24.dp))
        Text(if (isCorrect) "That's it!" else "Not quite.", style = LexaType.headlineLarge, color = LexaColors.TextPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp)); GoldDivider(); Spacer(Modifier.height(24.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LexaColors.SurfaceDark).padding(20.dp)) {
            Text(word.text, style = LexaType.titleLarge, color = LexaColors.Primary); Spacer(Modifier.height(8.dp))
            Text(word.definition, style = LexaType.bodyLarge, color = LexaColors.TextSecondary); Spacer(Modifier.height(12.dp))
            Text("\"${word.exampleSentence}\"", style = LexaType.bodyMedium.copy(fontStyle = FontStyle.Italic), color = LexaColors.TextPrimary)
        }
        Spacer(Modifier.weight(0.5f))
        GhostButton("Back to word →", onBackToWord); Spacer(Modifier.height(32.dp))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun PreviewCorrect() { LexaWallTheme { MiniQuizFeedbackScreen("ephemeral", true) {} } }
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun PreviewWrong() { LexaWallTheme { MiniQuizFeedbackScreen("ephemeral", false) {} } }
EOF

echo "✅ Word detail screens done"

# ============================================================================
# FEATURE: SETTINGS
# ============================================================================

cat > "$SRC/feature/settings/SettingsScreen.kt" << 'EOF'
package com.lexawall.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lexawall.core.ui.*

@Composable
fun SettingsScreen(onBack: () -> Unit, onThemeTap: () -> Unit, onResetConfirmed: () -> Unit) {
    var showResetDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(LexaColors.BackgroundDark)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp).padding(top = 40.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = LexaColors.TextPrimary) }
                Text("Settings", style = LexaType.titleMedium, color = LexaColors.TextPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center); Spacer(Modifier.size(48.dp))
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Spacer(Modifier.height(8.dp))
                SettingsRow(Icons.Default.Palette, LexaColors.Primary, "Theme", "Midnight", onThemeTap)
                SettingsRow(Icons.Default.Schedule, Color(0xFF9C27B0), "Rotation Speed", "10s") {}
                SettingsRow(Icons.Default.School, LexaColors.Success, "My Level", "B2") {}
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LexaColors.SurfaceDark).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFF9800).copy(alpha = 0.15f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Notifications, null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(16.dp)); Text("Notifications", style = LexaType.bodyLarge, color = LexaColors.TextPrimary, modifier = Modifier.weight(1f))
                    Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = LexaColors.White, checkedTrackColor = LexaColors.Primary, uncheckedThumbColor = LexaColors.White, uncheckedTrackColor = LexaColors.SurfaceHighlight))
                }
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LexaColors.ErrorRed.copy(alpha = 0.08f)).border(1.dp, LexaColors.ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(16.dp)).clickable { showResetDialog = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.RestartAlt, null, tint = LexaColors.ErrorRed, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Reset progress", style = LexaType.labelLarge, color = LexaColors.ErrorRed)
                }
                Spacer(Modifier.height(32.dp)); Text("LexaWall v2.4.0", style = LexaType.bodySmall, color = LexaColors.TextMuted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center); Spacer(Modifier.height(32.dp))
            }
        }
        if (showResetDialog) ResetModal(onReset = { showResetDialog = false; onResetConfirmed() }, onCancel = { showResetDialog = false })
    }
}

@Composable private fun SettingsRow(icon: ImageVector, iconColor: Color, title: String, subtitle: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LexaColors.SurfaceDark).clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.width(16.dp)); Column(Modifier.weight(1f)) { Text(title, style = LexaType.bodyLarge, color = LexaColors.TextPrimary); Text(subtitle, style = LexaType.bodySmall, color = LexaColors.TextMuted) }
        Icon(Icons.Default.ChevronRight, null, tint = LexaColors.TextMuted, modifier = Modifier.size(20.dp))
    }
}

@Composable private fun ResetModal(onReset: () -> Unit, onCancel: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).clickable(onClick = onCancel), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(horizontal = 32.dp).clip(RoundedCornerShape(20.dp)).background(LexaColors.SurfaceDark).border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(20.dp)).clickable(enabled = false, onClick = {}).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(24.dp)).background(LexaColors.ErrorRed.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Warning, null, tint = LexaColors.ErrorRed, modifier = Modifier.size(28.dp)) }
            Spacer(Modifier.height(16.dp)); Text("Reset progress?", style = LexaType.titleLarge, color = LexaColors.TextPrimary, textAlign = TextAlign.Center); Spacer(Modifier.height(8.dp))
            Text("Are you sure you want to reset your learning progress? This action cannot be undone and you will lose all statistics.", style = LexaType.bodyMedium, color = LexaColors.TextMuted, textAlign = TextAlign.Center); Spacer(Modifier.height(24.dp))
            Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, LexaColors.ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).clickable(onClick = onReset), contentAlignment = Alignment.Center) { Text("Reset Progress", style = LexaType.labelMedium, color = LexaColors.ErrorRed) }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp)).background(LexaColors.SurfaceHighlight).clickable(onClick = onCancel), contentAlignment = Alignment.Center) { Text("Cancel", style = LexaType.labelMedium, color = LexaColors.TextSecondary) }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { SettingsScreen({}, {}, {}) } }
EOF

cat > "$SRC/feature/settings/ThemePickerScreen.kt" << 'EOF'
package com.lexawall.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.TopBarRow
import com.lexawall.core.ui.*
import kotlinx.coroutines.launch

private data class ThemeCard(val id: String, val name: String, val top: Color, val bottom: Color, val accent: Color, val textColor: Color, val word: String, val pos: String, val def: String)
private val cards = listOf(
    ThemeCard("midnight", "Midnight", Color(0xFF1A1F35), Color(0xFF0F1220), LexaColors.Primary, Color.White, "Serendipity", "Noun", "The occurrence of events by chance in a happy way."),
    ThemeCard("savanna", "Savanna", Color(0xFF5C4D3C), Color(0xFF2B251E), Color(0xFFE6B489), Color(0xFFF3E5D8), "Ephemeral", "Adj", "Lasting for a very short time."),
    ThemeCard("lagoon", "Lagoon", Color(0xFF2C5361), Color(0xFF12242B), Color(0xFF8EE3F5), Color.White, "Meander", "Verb", "To follow a winding course."),
    ThemeCard("harmattan", "Harmattan", Color(0xFFD2B48C), Color(0xFF5D4037), Color(0xFF3E2723), Color(0xFF3E2723), "Petrichor", "Noun", "A pleasant smell after first rain."),
)

@Composable
fun ThemePickerScreen(onBack: () -> Unit) {
    var selected by remember { mutableStateOf("midnight") }
    val snackbar = remember { SnackbarHostState() }; val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize().background(LexaColors.BackgroundDark)) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(32.dp)); TopBarRow(onBack = onBack, title = "Theme")
            LazyVerticalGrid(GridCells.Fixed(2), Modifier.fillMaxSize().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(cards) { theme ->
                    val active = theme.id == selected
                    Column {
                        Box(Modifier.fillMaxWidth().aspectRatio(4f / 5f).clip(RoundedCornerShape(12.dp)).then(if (active) Modifier.border(2.dp, LexaColors.Primary, RoundedCornerShape(12.dp)) else Modifier).background(Brush.linearGradient(listOf(theme.top, theme.bottom))).clickable { selected = theme.id; scope.launch { snackbar.showSnackbar("Theme updated") } }, contentAlignment = Alignment.Center) {
                            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(theme.pos.uppercase(), style = LexaType.overlineSmall, color = theme.accent.copy(alpha = 0.8f)); Spacer(Modifier.height(4.dp))
                                Text(theme.word, style = LexaType.titleLarge.copy(fontFamily = PlayfairDisplay, fontSize = 20.sp), color = theme.textColor); Spacer(Modifier.height(4.dp))
                                Text(theme.def, style = LexaType.bodySmall.copy(fontSize = 10.sp), color = theme.textColor.copy(alpha = 0.6f), textAlign = TextAlign.Center, maxLines = 2)
                            }
                            if (active) Box(Modifier.align(Alignment.TopEnd).offset(6.dp, (-6).dp).size(24.dp).clip(CircleShape).background(LexaColors.Primary), contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, null, tint = LexaColors.BackgroundDark, modifier = Modifier.size(14.dp)) }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(theme.name, style = LexaType.labelMedium, color = LexaColors.TextPrimary); if (active) Text("Active", style = LexaType.bodySmall, color = LexaColors.Primary) }
                    }
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(16.dp)) { data ->
            Snackbar(Modifier.clip(RoundedCornerShape(12.dp)), containerColor = LexaColors.SurfaceHighlight, contentColor = LexaColors.TextPrimary) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { Icon(Icons.Default.CheckCircle, null, tint = LexaColors.Primary, modifier = Modifier.size(20.dp)); Text(data.visuals.message, style = LexaType.labelMedium, color = LexaColors.TextPrimary) }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { ThemePickerScreen {} } }
EOF

echo "✅ Settings screens done"

# ============================================================================
# FEATURE: MILESTONES
# ============================================================================

cat > "$SRC/feature/milestones/GraduationScreen.kt" << 'EOF'
package com.lexawall.feature.milestones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.*
import com.lexawall.core.ui.*

@Composable
fun GraduationScreen(onKeepGoing: () -> Unit) {
    Column(Modifier.fillMaxSize().background(LexaColors.BackgroundDark), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(Modifier.weight(1f).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(72.dp).clip(CircleShape).background(LexaColors.Primary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.School, null, tint = LexaColors.Primary, modifier = Modifier.size(36.dp)) }
            Spacer(Modifier.height(32.dp))
            Text("ephemeral", style = LexaType.wordDisplay.copy(fontFamily = PlayfairDisplay, fontStyle = FontStyle.Italic, fontSize = 44.sp), color = LexaColors.TextPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp)); GoldDivider(); Spacer(Modifier.height(24.dp))
            Text("You've made this word yours.", style = LexaType.headlineSmall, color = LexaColors.TextPrimary, textAlign = TextAlign.Center); Spacer(Modifier.height(8.dp))
            Text("It's leaving your rotation.", style = LexaType.bodyLarge, color = LexaColors.TextMuted, textAlign = TextAlign.Center)
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp)) { PrimaryButton("Keep going", onKeepGoing, showArrow = true) }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { GraduationScreen {} } }
EOF

cat > "$SRC/feature/milestones/LevelCompletionScreen.kt" << 'EOF'
package com.lexawall.feature.milestones

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.ui.*

@Composable
fun LevelCompletionScreen(onContinue: () -> Unit) {
    Column(Modifier.fillMaxSize().background(LexaColors.BackgroundDark), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(Modifier.weight(1f).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(contentAlignment = Alignment.Center) { Box(Modifier.size(120.dp).clip(CircleShape).background(LexaColors.Primary.copy(alpha = 0.12f))); Box(Modifier.size(96.dp).clip(CircleShape).background(LexaColors.SurfaceDark).border(2.dp, LexaColors.Primary.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Star, null, tint = LexaColors.Primary, modifier = Modifier.size(48.dp)) } }
            Spacer(Modifier.height(32.dp))
            Text(buildAnnotatedString { append("You mastered "); withStyle(SpanStyle(color = LexaColors.Primary)) { append("B2") }; append(".") }, style = LexaType.headlineLarge.copy(fontFamily = PlayfairDisplay), color = LexaColors.TextPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(buildAnnotatedString { append("You're "); withStyle(SpanStyle(color = LexaColors.TextPrimary)) { append("C1") }; append(" now.") }, style = LexaType.titleLarge, color = LexaColors.TextMuted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(40.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(LexaColors.WhiteAlpha10).border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(12.dp)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("2,450", style = LexaType.titleLarge, color = LexaColors.Primary); Spacer(Modifier.height(4.dp)); Text("WORDS", style = LexaType.overlineSmall, color = LexaColors.TextMuted) }
                Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(LexaColors.WhiteAlpha10).border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(12.dp)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("98%", style = LexaType.titleLarge, color = LexaColors.Primary); Spacer(Modifier.height(4.dp)); Text("ACCURACY", style = LexaType.overlineSmall, color = LexaColors.TextMuted) }
            }
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp)) { PrimaryButton("Continue", onContinue, showArrow = true) }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { LevelCompletionScreen {} } }
EOF

cat > "$SRC/feature/milestones/Day30SummaryScreen.kt" << 'EOF'
package com.lexawall.feature.milestones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.ui.*

@Composable
fun Day30SummaryScreen(onBackToHome: () -> Unit) {
    Column(Modifier.fillMaxSize().background(LexaColors.BackgroundDark), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(Modifier.weight(1f).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(72.dp).clip(CircleShape).background(LexaColors.Primary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.EmojiEvents, null, tint = LexaColors.Primary, modifier = Modifier.size(36.dp)) }
            Spacer(Modifier.height(32.dp))
            Text("30 days.", style = LexaType.headlineLarge.copy(fontFamily = PlayfairDisplay, fontSize = 48.sp), color = LexaColors.TextPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp)); Text("You didn't know these words a month ago.", style = LexaType.bodyLarge, color = LexaColors.TextSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(48.dp))
            Text("+42", style = LexaType.scoreDisplay.copy(fontFamily = DmSans, fontSize = 80.sp, letterSpacing = (-3).sp), color = LexaColors.Primary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp)); Text("Words Mastered", style = LexaType.titleMedium, color = LexaColors.TextSecondary.copy(alpha = 0.8f), textAlign = TextAlign.Center)
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp)) { PrimaryButton("Back to home", onBackToHome) }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { Day30SummaryScreen {} } }
EOF

echo "✅ Milestone screens done"

# ============================================================================
# DONE
# ============================================================================

echo ""
echo "══════════════════════════════════════════════════════════"
echo "  ✅  LexaWall project generated successfully!"
echo "══════════════════════════════════════════════════════════"
echo ""
echo "  📂 Location: ./$ROOT"
echo "  📄 34 Kotlin files | 22 screens | 6 home states"
echo ""
echo "  Next steps:"
echo "    1. Open '$ROOT' in Android Studio"
echo "    2. Let Gradle sync (it will download dependencies)"
echo "    3. Build & run on emulator/device"
echo ""
echo "  Navigation flow:"
echo "    Splash → Wallpaper → Setup → Notifications → Home"
echo "    Home → Quiz/Flashcards/WordDetail/Settings"
echo "    Home has 'Demo: cycle state →' button at bottom"
echo "══════════════════════════════════════════════════════════"

