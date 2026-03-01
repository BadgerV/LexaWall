package com.lexawall.feature.worddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.QuizOptionRow
import com.lexawall.core.ui.*
import com.lexawall.data.local.DatabaseModule
import com.lexawall.domain.model.Word
import com.lexawall.domain.model.QuizOptionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

@Composable
fun MiniQuizQuestionScreen(wordId: String, onAnswer: (Boolean) -> Unit) {
    val context = LocalContext.current
    val db = remember { DatabaseModule.getInstance(context) }
    val wordRepository = remember { DatabaseModule.provideWordRepository(context) }
    val quizRepository = remember { DatabaseModule.provideQuizRepository(context) }

    var word by remember(wordId) { mutableStateOf<Word?>(null) }
    var options by remember(wordId) { mutableStateOf<List<String>>(emptyList()) }
    var correctIndex by remember(wordId) { mutableIntStateOf(-1) }
    var selected by remember(wordId) { mutableIntStateOf(-1) }

    LaunchedEffect(wordId) {
        val loadedWord = withContext(Dispatchers.IO) { wordRepository.getWordById(wordId) }
        val loadedOptions = withContext(Dispatchers.IO) {
            val allWords = db.wordDao().getAllWords()
            val incorrectDefinitions = allWords
                .asSequence()
                .filter { it.id != loadedWord.id }
                .map { it.definition }
                .distinct()
                .shuffled(Random.Default)
                .take(2)
                .toMutableList()

            if (incorrectDefinitions.size < 2) {
                val fallbackDefinitions = quizRepository.getQuizQuestions()
                    .asSequence()
                    .map { it.word.definition }
                    .filter { it != loadedWord.definition }
                    .distinct()
                    .toList()
                for (definition in fallbackDefinitions) {
                    if (incorrectDefinitions.size >= 2) break
                    incorrectDefinitions.add(definition)
                }
            }

            while (incorrectDefinitions.size < 2) {
                incorrectDefinitions.add(loadedWord.definition)
            }

            val loadedCorrectIndex = Random.nextInt(0, 3)
            val builtOptions = MutableList(3) { "" }
            builtOptions[loadedCorrectIndex] = loadedWord.definition
            var wrongPointer = 0
            for (index in builtOptions.indices) {
                if (builtOptions[index].isEmpty()) {
                    builtOptions[index] = incorrectDefinitions[wrongPointer]
                    wrongPointer++
                }
            }
            Pair(builtOptions, loadedCorrectIndex)
        }

        word = loadedWord
        options = loadedOptions.first
        correctIndex = loadedOptions.second
        selected = -1
    }

    if (word == null || options.size < 3 || correctIndex !in 0..2) {
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
    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(LexaColors.BackgroundDark)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(80.dp))
        Text(safeWord.text, style = LexaType.wordDisplay.copy(fontFamily = PlayfairDisplay, fontStyle = FontStyle.Italic, fontSize = 40.sp), color = LexaColors.TextPrimary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { Text("—", style = LexaType.bodyLarge, color = LexaColors.Primary); Text("  Meaning?  ", style = LexaType.bodyLarge, color = LexaColors.TextSecondary); Text("—", style = LexaType.bodyLarge, color = LexaColors.Primary) }
        Spacer(Modifier.height(40.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEachIndexed { i, t ->
                val state = when { selected == -1 -> QuizOptionState.Default; i == selected && i == correctIndex -> QuizOptionState.Correct; i == selected -> QuizOptionState.Wrong; else -> QuizOptionState.Default }
                QuizOptionRow(
                    text = t,
                    state = state,
                    onClick = {
                        if (selected == -1) {
                            selected = i
                            onAnswer(i == correctIndex)
                        }
                    }
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Text("Doesn't affect streak.", style = LexaType.monoSmall.copy(fontFamily = DmMono), color = LexaColors.TextMuted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(progress = { 0.5f }, modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)), color = LexaColors.Primary, trackColor = LexaColors.SurfaceHighlight)
        Spacer(Modifier.height(32.dp))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { MiniQuizQuestionScreen("ephemeral") {} } }
