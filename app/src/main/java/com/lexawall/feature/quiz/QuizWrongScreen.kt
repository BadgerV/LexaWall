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
    LaunchedEffect(questionIndex) {
        kotlinx.coroutines.delay(400)
        onGotIt(questionIndex + 1, isLast)
    }
    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(LexaColors.BackgroundDark)
    ) {
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
