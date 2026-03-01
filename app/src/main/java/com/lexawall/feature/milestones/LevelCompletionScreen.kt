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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.ui.*
import com.lexawall.data.local.DatabaseModule
import com.lexawall.data.local.PrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LevelCompletionScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    val db = remember { DatabaseModule.getInstance(context) }

    var masteredCount by remember { mutableIntStateOf(0) }
    var currentLevel by remember { mutableStateOf("B2") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            masteredCount = db.userProgressDao().getWordsByState("MASTERED").size
            currentLevel = PrefsManager.getUserLevel(context)
        }
    }

    val nextLevel = when (currentLevel) {
        "A2" -> "B1"
        "B1" -> "B2"
        "B2" -> "C1"
        else -> "C1"
    }

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(LexaColors.BackgroundDark),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.weight(1f).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(contentAlignment = Alignment.Center) { Box(Modifier.size(120.dp).clip(CircleShape).background(LexaColors.Primary.copy(alpha = 0.12f))); Box(Modifier.size(96.dp).clip(CircleShape).background(LexaColors.SurfaceDark).border(2.dp, LexaColors.Primary.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Star, null, tint = LexaColors.Primary, modifier = Modifier.size(48.dp)) } }
            Spacer(Modifier.height(32.dp))
            Text(buildAnnotatedString { append("You mastered "); withStyle(SpanStyle(color = LexaColors.Primary)) { append(currentLevel) }; append(".") }, style = LexaType.headlineLarge.copy(fontFamily = PlayfairDisplay), color = LexaColors.TextPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(buildAnnotatedString { append("You're "); withStyle(SpanStyle(color = LexaColors.TextPrimary)) { append(nextLevel) }; append(" now.") }, style = LexaType.titleLarge, color = LexaColors.TextMuted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(40.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(LexaColors.WhiteAlpha10).border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(12.dp)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(masteredCount.toString(), style = LexaType.titleLarge, color = LexaColors.Primary); Spacer(Modifier.height(4.dp)); Text("WORDS", style = LexaType.overlineSmall, color = LexaColors.TextMuted) }
                Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(LexaColors.WhiteAlpha10).border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(12.dp)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("98%", style = LexaType.titleLarge, color = LexaColors.Primary); Spacer(Modifier.height(4.dp)); Text("ACCURACY", style = LexaType.overlineSmall, color = LexaColors.TextMuted) }
            }
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp)) {
            PrimaryButton("Continue", {
                PrefsManager.saveUserLevel(context, nextLevel)
                onContinue()
            }, showArrow = true)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { LevelCompletionScreen {} } }
