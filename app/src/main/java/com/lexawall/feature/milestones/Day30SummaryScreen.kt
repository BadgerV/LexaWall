package com.lexawall.feature.milestones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.ui.*
import com.lexawall.data.local.DatabaseModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun Day30SummaryScreen(onBackToHome: () -> Unit) {
    val context = LocalContext.current
    val db = remember { DatabaseModule.getInstance(context) }
    var masteredCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            masteredCount = db.userProgressDao().getWordsByState("MASTERED").size
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(LexaColors.BackgroundDark),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.weight(1f).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(72.dp).clip(CircleShape).background(LexaColors.Primary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.EmojiEvents, null, tint = LexaColors.Primary, modifier = Modifier.size(36.dp)) }
            Spacer(Modifier.height(32.dp))
            Text("30 days.", style = LexaType.headlineLarge.copy(fontFamily = PlayfairDisplay, fontSize = 48.sp), color = LexaColors.TextPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp)); Text("You didn't know these words a month ago.", style = LexaType.bodyLarge, color = LexaColors.TextSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(48.dp))
            Text("+$masteredCount", style = LexaType.scoreDisplay.copy(fontFamily = DmSans, fontSize = 80.sp, letterSpacing = (-3).sp), color = LexaColors.Primary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp)); Text("Words Mastered", style = LexaType.titleMedium, color = LexaColors.TextSecondary.copy(alpha = 0.8f), textAlign = TextAlign.Center)
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp)) { PrimaryButton("Back to home", onBackToHome) }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { Day30SummaryScreen {} } }
