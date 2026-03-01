package com.lexawall.feature.milestones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.*
import com.lexawall.core.ui.*
import com.lexawall.data.local.DatabaseModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun GraduationScreen(wordId: String, onKeepGoing: () -> Unit, onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val activeTheme = LocalLexaTheme.current
    val db = remember { DatabaseModule.getInstance(context) }
    var wordText by remember { mutableStateOf("ephemeral") }
    var definitionText by remember { mutableStateOf("You've made this word yours.") }
    var wordVisible by remember { mutableStateOf(false) }
    var particlesVisible by remember { mutableStateOf(false) }
    var messageVisible by remember { mutableStateOf(false) }
    var dividerVisible by remember { mutableStateOf(false) }
    var buttonVisible by remember { mutableStateOf(false) }

    LaunchedEffect(wordId) {
        withContext(Dispatchers.IO) {
            val word = db.wordDao().getWordById(wordId)
            wordText = word?.word ?: "ephemeral"
            definitionText = word?.definition ?: "You've made this word yours."
        }
    }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)
        wordVisible = true
        kotlinx.coroutines.delay(600)
        particlesVisible = true
        kotlinx.coroutines.delay(600)
        messageVisible = true
        dividerVisible = true
        kotlinx.coroutines.delay(400)
        buttonVisible = true
    }

    val wordAlpha by animateFloatAsState(
        targetValue = if (wordVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = "graduationWordAlpha"
    )
    val wordOffset by animateFloatAsState(
        targetValue = if (wordVisible) 0f else 32f,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = "graduationWordOffset"
    )
    val messageAlpha by animateFloatAsState(
        targetValue = if (messageVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
        label = "graduationMessageAlpha"
    )
    val dividerWidth by animateDpAsState(
        targetValue = if (dividerVisible) 48.dp else 0.dp,
        animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
        label = "graduationDividerWidth"
    )
    val particleTravel by animateFloatAsState(
        targetValue = if (particlesVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = "graduationParticleTravel"
    )
    val particleAlpha by animateFloatAsState(
        targetValue = if (particlesVisible) 0f else 1f,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = "graduationParticleAlpha"
    )
    val buttonAlpha by animateFloatAsState(
        targetValue = if (buttonVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "graduationButtonAlpha"
    )
    val particleDirections = remember {
        listOf(
            -1f to 0f,
            1f to 0f,
            0f to -1f,
            0f to 1f,
            -0.75f to -0.75f,
            0.75f to -0.75f,
            -0.75f to 0.75f,
            0.75f to 0.75f
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(LexaColors.BackgroundDark),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.weight(1f).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(72.dp).clip(CircleShape).background(LexaColors.Primary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.School, null, tint = LexaColors.Primary, modifier = Modifier.size(36.dp)) }
            Spacer(Modifier.height(32.dp))
            Box(contentAlignment = Alignment.Center) {
                Text(
                    wordText,
                    style = LexaType.wordDisplay.copy(fontFamily = PlayfairDisplay, fontStyle = FontStyle.Italic, fontSize = 44.sp),
                    color = LexaColors.TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer(
                        alpha = wordAlpha,
                        translationY = with(androidx.compose.ui.platform.LocalDensity.current) { wordOffset.dp.toPx() }
                    )
                )
                if (particlesVisible || particleAlpha > 0f) {
                    particleDirections.forEach { (dx, dy) ->
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = (dx * 44f * particleTravel).dp,
                                    y = (dy * 44f * particleTravel).dp
                                )
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(LexaColors.Primary)
                                .graphicsLayer(alpha = particleAlpha)
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "is yours now.",
                style = LexaType.titleMedium.copy(fontFamily = PlayfairDisplay, fontStyle = FontStyle.Italic),
                color = LexaColors.TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(messageAlpha)
            )
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .width(dividerWidth)
                    .height(1.dp)
                    .background(LexaColors.Primary.copy(alpha = 0.5f))
            )
            Spacer(Modifier.height(20.dp))
            Text(
                definitionText,
                style = LexaType.bodyLarge,
                color = LexaColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(messageAlpha)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "It's leaving your rotation.",
                style = LexaType.bodyMedium,
                color = LexaColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(messageAlpha)
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "+100 XP",
                style = LexaType.titleSmall.copy(fontFamily = DmMono, fontSize = 13.sp),
                color = LexaColors.Primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(buttonAlpha)
            )
        }
        if (buttonVisible || buttonAlpha > 0f) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .graphicsLayer(alpha = buttonAlpha)
            ) {
                PrimaryButton("Keep going", onKeepGoing, showArrow = true)
            }
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
                tint = androidx.compose.ui.graphics.Color(activeTheme.accentColorDim),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { GraduationScreen(wordId = "1", onKeepGoing = {}) } }
