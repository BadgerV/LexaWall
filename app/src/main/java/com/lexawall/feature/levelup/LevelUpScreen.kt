package com.lexawall.feature.levelup

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.ui.DmMono
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LocalLexaTheme
import com.lexawall.core.ui.PlayfairDisplay
import com.lexawall.core.utils.DevicePerformance
import com.lexawall.data.local.PrefsManager

@Composable
fun LevelUpScreen(
    newTitle: String,
    unlockedTheme: String,
    unlockedWordTier: String,
    onContinue: () -> Unit,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val activeTheme = LocalLexaTheme.current
    val isLowEnd = remember(context) { DevicePerformance.isLowEndDevice(context) }
    var titleVisible by remember { mutableStateOf(false) }
    var subtitleVisible by remember { mutableStateOf(false) }
    var themePillVisible by remember { mutableStateOf(false) }
    var tierPillVisible by remember { mutableStateOf(false) }
    var particlesVisible by remember { mutableStateOf(false) }
    var continueVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)
        titleVisible = true
        kotlinx.coroutines.delay(500)
        subtitleVisible = true
        kotlinx.coroutines.delay(600)
        if (unlockedTheme.isNotBlank()) {
            themePillVisible = true
            kotlinx.coroutines.delay(200)
        }
        if (unlockedWordTier.isNotBlank()) {
            tierPillVisible = true
            kotlinx.coroutines.delay(200)
        }
        particlesVisible = !isLowEnd
        if (!isLowEnd) {
            kotlinx.coroutines.delay(700)
        }
        continueVisible = true
    }

    val titleAlpha by animateFloatAsState(
        targetValue = if (titleVisible) 1f else 0f,
        animationSpec = tween(500, easing = LinearOutSlowInEasing),
        label = "levelUpTitleAlpha"
    )
    val titleOffset by animateDpAsState(
        targetValue = if (titleVisible) 0.dp else 20.dp,
        animationSpec = tween(500, easing = LinearOutSlowInEasing),
        label = "levelUpTitleOffset"
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (subtitleVisible) 1f else 0f,
        animationSpec = tween(350),
        label = "levelUpSubtitleAlpha"
    )
    val themePillAlpha by animateFloatAsState(
        targetValue = if (themePillVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "levelUpThemePillAlpha"
    )
    val tierPillAlpha by animateFloatAsState(
        targetValue = if (tierPillVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "levelUpTierPillAlpha"
    )
    val particleTravel = if (isLowEnd) {
        1f
    } else {
        animateFloatAsState(
            targetValue = if (particlesVisible) 1f else 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "levelUpParticleTravel"
        ).value
    }
    val particleAlpha = if (isLowEnd) {
        0f
    } else {
        animateFloatAsState(
            targetValue = if (particlesVisible) 0f else 1f,
            animationSpec = tween(durationMillis = 700),
            label = "levelUpParticleAlpha"
        ).value
    }
    val continueAlpha by animateFloatAsState(
        targetValue = if (continueVisible) 1f else 0f,
        animationSpec = tween(200),
        label = "levelUpContinueAlpha"
    )

    val particleDirections = remember {
        listOf(
            -1f to 0f,
            1f to 0f,
            0f to -1f,
            0f to 1f,
            -0.8f to -0.8f,
            0.8f to -0.8f,
            -0.8f to 0.8f,
            0.8f to 0.8f
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color(activeTheme.backgroundTop))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = newTitle,
                style = LexaType.displayMedium.copy(
                    fontFamily = PlayfairDisplay,
                    fontStyle = FontStyle.Italic,
                    fontSize = 48.sp
                ),
                color = Color(activeTheme.accentColor),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(titleAlpha)
                    .offset(y = titleOffset)
            )

            if (!isLowEnd && (particlesVisible || particleAlpha > 0f)) {
                particleDirections.forEach { (dx, dy) ->
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (dx * 56f * particleTravel).dp,
                                y = (dy * 56f * particleTravel).dp
                            )
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color(activeTheme.accentColor))
                            .graphicsLayer(alpha = particleAlpha)
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = "You earned this.",
            style = LexaType.bodyLarge.copy(fontStyle = FontStyle.Italic, fontSize = 16.sp),
            color = Color(0xFFE8E8E8),
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(subtitleAlpha)
        )

        if (unlockedTheme.isNotBlank()) {
            Spacer(Modifier.height(20.dp))
            UnlockPill(
                text = "${unlockedTheme.replaceFirstChar { it.uppercase() }} theme unlocked 🎨",
                alpha = themePillAlpha
            )
        }

        if (unlockedWordTier.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            UnlockPill(
                text = "New words unlocked",
                alpha = tierPillAlpha
            )
        }

        Spacer(Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .width(240.dp)
                .alpha(continueAlpha)
        ) {
            PrimaryButton(
                text = "Continue →",
                onClick = {
                    if (unlockedTheme.isNotBlank()) {
                        PrefsManager.saveActiveTheme(context, unlockedTheme)
                    }
                    if (unlockedWordTier.isNotBlank()) {
                        PrefsManager.saveUnlockedWordTier(context, unlockedWordTier)
                    }
                    onContinue()
                }
            )
        }
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.Start)
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
private fun UnlockPill(text: String, alpha: Float) {
    val theme = LocalLexaTheme.current
    Box(
        modifier = Modifier
            .alpha(alpha)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(theme.surfaceColor))
            .border(0.5.dp, Color(theme.accentColor), RoundedCornerShape(999.dp))
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = LexaType.bodySmall.copy(fontFamily = DmMono, fontSize = 12.sp),
            color = Color(theme.accentColor),
            textAlign = TextAlign.Center
        )
    }
}
