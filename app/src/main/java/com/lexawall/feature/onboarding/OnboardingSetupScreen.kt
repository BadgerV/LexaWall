package com.lexawall.feature.onboarding

import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.lexawall.core.components.GhostButton
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.navigation.Screen
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LexaWallTheme
import com.lexawall.core.ui.LocalLexaTheme
import com.lexawall.data.local.PrefsManager
import com.lexawall.utils.LexaWallLogger
import com.lexawall.wallpaper.LexaWallWallpaperService
import com.lexawall.wallpaper.WallpaperSetupUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingSetupScreen(
    navController: NavHostController,
    onBack: () -> Unit,
    onNotNow: () -> Unit
) {
    val activeTheme = LocalLexaTheme.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var setupHandled by remember { mutableStateOf(false) }

    fun completeSetupIfWallpaperActive(): Boolean {
        if (setupHandled) return true
        LexaWallLogger.d(
            "LexaWallSetup",
            "completeSetupIfWallpaperActive check: ${WallpaperSetupUtils.currentComponentDebug(context)}"
        )
        if (WallpaperSetupUtils.isLexaWallpaperActive(context)) {
            setupHandled = true
            PrefsManager.saveWallpaperSet(context, true)
            PrefsManager.saveOnboardingComplete(context, true)
            LexaWallLogger.d("LexaWallSetup", "Wallpaper active -> onboarding complete, navigating Home")
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.OnboardingSetup.route) { inclusive = true }
            }
            return true
        }
        LexaWallLogger.d("LexaWallSetup", "Wallpaper not active yet")
        return false
    }

    val wallpaperLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // when returning from picker, re-check in case user applied or canceled
        LexaWallLogger.d("LexaWallSetup", "Wallpaper picker returned")
        if (completeSetupIfWallpaperActive()) return@rememberLauncherForActivityResult
        scope.launch {
            repeat(20) {
                delay(250L)
                LexaWallLogger.d("LexaWallSetup", "Delayed recheck #${it + 1}")
                if (completeSetupIfWallpaperActive()) return@launch
            }
        }
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_WALLPAPER_CHANGED) {
                    LexaWallLogger.d("LexaWallSetup", "Received ACTION_WALLPAPER_CHANGED")
                    // OEM wallpaper pickers can report component metadata inconsistently right after apply.
                    // If the system confirmed wallpaper changed, treat setup as complete.
                    if (!completeSetupIfWallpaperActive()) {
                        setupHandled = true
                        PrefsManager.saveWallpaperSet(context, true)
                        PrefsManager.saveOnboardingComplete(context, true)
                        LexaWallLogger.d("LexaWallSetup", "Broadcast fallback -> onboarding complete, navigating Home")
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.OnboardingSetup.route) { inclusive = true }
                        }
                    }
                }
            }
        }

        val filter = IntentFilter(Intent.ACTION_WALLPAPER_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                LexaWallLogger.d("LexaWallSetup", "OnboardingSetupScreen ON_RESUME")
                completeSetupIfWallpaperActive()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(LexaColors.BackgroundDark)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(LexaColors.SurfaceDark)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(LexaColors.SurfaceHighlight, LexaColors.SurfaceDark)
                            )
                        )
                )
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text("Two steps and you're in.", style = LexaType.headlineSmall, color = LexaColors.TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text("Set me as your wallpaper, then let me remind you.", style = LexaType.bodyMedium, color = LexaColors.TextSecondary)
                    Spacer(Modifier.height(20.dp))
                    SetupStep(1, "Set Lexa as your wallpaper")
                    Spacer(Modifier.height(16.dp))
                    SetupStep(2, "Let Lexa nudge you.")
                    Spacer(Modifier.height(28.dp))
                    PrimaryButton("Open picker", onClick = {
                        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                            putExtra(
                                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                ComponentName(context, LexaWallWallpaperService::class.java)
                            )
                        }
                        try {
                            LexaWallLogger.d("LexaWallSetup", "Launching ACTION_CHANGE_LIVE_WALLPAPER")
                            wallpaperLauncher.launch(intent)
                        } catch (e: Exception) {
                            LexaWallLogger.w("LexaWallSetup", "Live wallpaper picker failed, falling back to ACTION_SET_WALLPAPER")
                            LexaWallLogger.e("LexaWallSetup", "Picker exception", e)
                            wallpaperLauncher.launch(Intent(Intent.ACTION_SET_WALLPAPER))
                        }
                    })
                    Spacer(Modifier.height(8.dp))
                    GhostButton("Not now", onNotNow)
                }
            }
        }
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(top = 16.dp, start = 8.dp)
                .statusBarsPadding()
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(activeTheme.accentColorDim),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun SetupStep(number: Int, text: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(LexaColors.Primary),
            contentAlignment = Alignment.Center
        ) {
            Text("$number", style = LexaType.labelMedium, color = LexaColors.BackgroundDark, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, style = LexaType.bodyLarge, color = LexaColors.TextPrimary)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun Preview() {
    LexaWallTheme {
        OnboardingSetupScreen(
            navController = rememberNavController(),
            onBack = {},
            onNotNow = {}
        )
    }
}
