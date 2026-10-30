package com.lexawall.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
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

    // ---------- entrance triggers ----------
    var showMascot by remember { mutableStateOf(false) }
    var showWordmark by remember { mutableStateOf(false) }

    // ---------- mascot: scale from 0.4→1 + fade ----------
    val mascotAlpha by animateFloatAsState(
        targetValue = if (showMascot) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "mascotAlpha"
    )
    val mascotScale by animateFloatAsState(
        targetValue = if (showMascot) 1f else 0.4f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "mascotScale"
    )

    // ---------- mascot idle float loop (subtle Y bob) ----------
    val infiniteTransition = rememberInfiniteTransition(label = "idleFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    // ---------- mascot subtle rotation wiggle ----------
    val rotationZ by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotZ"
    )

    // ---------- wordmark: fade + slide-up ----------
    val wordmarkAlpha by animateFloatAsState(
        targetValue = if (showWordmark) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "wordmarkAlpha"
    )
    val wordmarkOffsetY by animateFloatAsState(
        targetValue = if (showWordmark) 0f else 20f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "wordmarkSlide"
    )

    // ---------- choreography ----------
    LaunchedEffect(Unit) {
        delay(100L)
        showMascot = true
        delay(350L)
        showWordmark = true
    }

    // ---------- navigation timeout ----------
    LaunchedEffect(Unit) {
        delay(2200L)
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
            // Mascot with scale + float + rotation
            LexaMascotAnimated(
                modifier = Modifier
                    .alpha(mascotAlpha)
                    .scale(mascotScale)
                    .graphicsLayer {
                        translationY = floatOffset
                        this.rotationZ = rotationZ
                    },
                size = 64.dp
            )
            Spacer(Modifier.height(14.dp))
            // Wordmark with fade + slide
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
                modifier = Modifier
                    .alpha(wordmarkAlpha)
                    .graphicsLayer { translationY = wordmarkOffsetY }
            )
            Spacer(Modifier.height(12.dp))
            // Tagline with same wordmark animation
            Text(
                "Your wallpaper teaches you.",
                style = LexaType.bodyLarge.copy(fontSize = 18.sp, letterSpacing = 0.5.sp),
                color = LexaColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(wordmarkAlpha)
                    .graphicsLayer { translationY = wordmarkOffsetY }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun SplashScreenPreview() { LexaWallTheme { SplashScreen(onTimeout = {}) } }
