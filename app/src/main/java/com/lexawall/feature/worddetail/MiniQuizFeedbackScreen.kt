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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lexawall.core.components.*
import com.lexawall.core.ui.*
import com.lexawall.data.local.DatabaseModule
import com.lexawall.domain.model.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MiniQuizFeedbackScreen(wordId: String, isCorrect: Boolean, onBackToWord: () -> Unit) {
    val context = LocalContext.current
    val wordRepository = remember { DatabaseModule.provideWordRepository(context) }
    var word by remember(wordId) { mutableStateOf<Word?>(null) }

    LaunchedEffect(wordId) {
        word = withContext(Dispatchers.IO) {
            wordRepository.getWordById(wordId)
        }
    }

    if (word == null) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .background(LexaColors.BackgroundDark)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Loading...", style = LexaType.bodyMedium, color = LexaColors.TextMuted)
        }
        return
    }

    val safeWord = word ?: return
    val color = if (isCorrect) LexaColors.Success else LexaColors.ErrorRed
    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(LexaColors.BackgroundDark)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(0.3f))
        GlowCircle(icon = { Icon(if (isCorrect) Icons.Default.Check else Icons.Default.Close, null, tint = color, modifier = Modifier.size(36.dp)) }, glowColor = color)
        Spacer(Modifier.height(24.dp))
        Text(if (isCorrect) "That's it!" else "Not quite.", style = LexaType.headlineLarge, color = LexaColors.TextPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp)); GoldDivider(); Spacer(Modifier.height(24.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LexaColors.SurfaceDark).padding(20.dp)) {
            Text(safeWord.text, style = LexaType.titleLarge, color = LexaColors.Primary); Spacer(Modifier.height(8.dp))
            Text(safeWord.definition, style = LexaType.bodyLarge, color = LexaColors.TextSecondary); Spacer(Modifier.height(12.dp))
            Text("\"${safeWord.exampleSentence}\"", style = LexaType.bodyMedium.copy(fontStyle = FontStyle.Italic), color = LexaColors.TextPrimary)
        }
        Spacer(Modifier.weight(0.5f))
        GhostButton("Back to word →", onBackToWord); Spacer(Modifier.height(32.dp))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun PreviewCorrect() { LexaWallTheme { MiniQuizFeedbackScreen("ephemeral", true) {} } }
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun PreviewWrong() { LexaWallTheme { MiniQuizFeedbackScreen("ephemeral", false) {} } }
