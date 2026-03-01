package com.lexawall

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.lexawall.core.navigation.NavGraph
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaWallTheme
import com.lexawall.core.ai.LexaSpeaker
import com.lexawall.data.local.DatabaseModule
import com.lexawall.data.local.PrefsManager
import com.lexawall.data.local.seeder.DatabaseSeeder
import com.lexawall.feature.home.HomeViewModel
import com.lexawall.notifications.NotificationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        enableEdgeToEdge()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = DatabaseModule.getInstance(applicationContext)
                if (!PrefsManager.isWordsMigrated(applicationContext)) {
                    database.userProgressDao().clearAll()
                    database.quizSessionDao().clearAll()
                    PrefsManager.saveWordsMigrated(applicationContext, true)
                }
                val seeder = DatabaseSeeder(applicationContext, database)
                seeder.seedIfNeeded()
            } catch (e: Exception) {
                Log.e("LexaWall", "Seeder failed on launch", e)
            }
        }

        intent?.data?.let { data ->
            if (data.scheme == "lexawall" && data.host == "word") {
                val wordId = data.pathSegments.firstOrNull()
                if (!wordId.isNullOrBlank()) {
                    pendingDeepLinkWordId.value = wordId
                }
            }
        }

        setContent {
            LexaWallTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LexaColors.BackgroundDark
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController, homeViewModel = homeViewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        NotificationScheduler.cancelPendingNotification(applicationContext)
        homeViewModel.refreshWallpaperWord()
    }

    override fun onDestroy() {
        super.onDestroy()
        LexaSpeaker.shutdown()
    }

    companion object {
        val pendingDeepLinkWordId: MutableState<String?> = mutableStateOf(null)
    }
}
