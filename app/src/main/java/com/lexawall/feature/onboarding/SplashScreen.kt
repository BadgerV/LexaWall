package com.lexawall.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.ui.components.LexaMascotAnimated
import com.lexawall.core.ui.*
import com.lexawall.data.local.PrefsManager
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit, onOnboardingComplete: () -> Unit = onTimeout) {
    val context = LocalContext.current
    var showOwl by remember { mutableStateOf(false) }
    var showWordmark by remember { mutableStateOf(false) }
    val owlAlpha by animateFloatAsState(
        targetValue = if (showOwl) 1f else 0f,
        animationSpec = tween(durationMillis = 320),
        label = "splashOwlAlpha"
    )
    val wordmarkAlpha by animateFloatAsState(
        targetValue = if (showWordmark) 1f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "splashWordmarkAlpha"
    )

    LaunchedEffect(Unit) {
        delay(120L)
        showOwl = true
        delay(260L)
        showWordmark = true
    }

    LaunchedEffect(Unit) {
        delay(1800L)
        if (PrefsManager.isOnboardingComplete(context)) {
            onOnboardingComplete()
        } else {
            onTimeout()
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(LexaColors.SplashBg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LexaMascotAnimated(
                modifier = Modifier.alpha(owlAlpha),
                size = 64.dp
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "LEXA",
                style = LexaType.displayLarge.copy(
                    fontFamily = PlayfairDisplay,
                    fontStyle = FontStyle.Italic,
                    fontSize = 42.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = LexaColors.BrandGold,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(wordmarkAlpha)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Your wallpaper teaches you.",
                style = LexaType.bodyLarge.copy(fontSize = 18.sp, letterSpacing = 0.5.sp),
                color = LexaColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(wordmarkAlpha)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun SplashScreenPreview() { LexaWallTheme { SplashScreen(onTimeout = {}) } }
