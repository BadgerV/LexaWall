package com.lexawall.feature.worddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.*
import com.lexawall.core.ai.LexaSpeaker
import com.lexawall.core.ui.*
import com.lexawall.data.local.DatabaseModule
import com.lexawall.data.local.entities.WordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun WordDetailScreen(wordId: String, onBack: () -> Unit, onQuizMe: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activeTheme = LocalLexaTheme.current
    val db = remember { DatabaseModule.getInstance(context) }
    var word by remember(wordId) { mutableStateOf<WordEntity?>(null) }
    var isLoaded by remember(wordId) { mutableStateOf(false) }

    LaunchedEffect(wordId) {
        word = withContext(Dispatchers.IO) {
            db.wordDao().getWordById(wordId)
        }
        isLoaded = true
    }

    if (!isLoaded) {
        Box(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .background(LexaColors.BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading...", style = LexaType.bodyMedium, color = LexaColors.TextMuted)
        }
        return
    }

    if (word == null) {
        Box(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .background(LexaColors.BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Text("Word not found.", style = LexaType.headlineSmall, color = LexaColors.TextPrimary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                PrimaryButton("Back", onBack)
            }
        }
        return
    }

    val safeWord = word ?: return

    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(LexaColors.BackgroundDark)
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(32.dp))
                Box(Modifier.clip(RoundedCornerShape(20.dp)).border(1.dp, LexaColors.Primary.copy(alpha = 0.2f), RoundedCornerShape(20.dp)).background(LexaColors.Primary.copy(alpha = 0.1f)).padding(horizontal = 16.dp, vertical = 6.dp)) { Text("${safeWord.cefrLevel} · ${safeWord.partOfSpeech}", style = LexaType.overline.copy(fontFamily = DmMono), color = LexaColors.Primary) }
                Spacer(Modifier.height(24.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        safeWord.word,
                        style = LexaType.wordDisplay.copy(fontFamily = PlayfairDisplay, fontSize = 48.sp, fontStyle = FontStyle.Normal),
                        color = LexaColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    IconButton(
                        onClick = { LexaSpeaker.speak(context, safeWord.word) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Pronounce word",
                            tint = LexaColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                if (!safeWord.pronunciationIpa.isNullOrEmpty()) Box(Modifier.clip(RoundedCornerShape(20.dp)).background(LexaColors.SurfaceDark).padding(horizontal = 16.dp, vertical = 8.dp)) { Text(safeWord.pronunciationIpa, style = LexaType.mono, color = LexaColors.TextSecondary) }
                Spacer(Modifier.height(24.dp))
                Text(safeWord.definition, style = LexaType.bodyLarge.copy(fontSize = 20.sp), color = LexaColors.TextSecondary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(32.dp)); GoldDivider(); Spacer(Modifier.height(32.dp))
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LexaColors.SurfaceDark).border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(16.dp)).padding(20.dp), horizontalAlignment = Alignment.Start) {
                    OverlineTag("EXAMPLE"); Spacer(Modifier.height(12.dp))
                    Text("\"${safeWord.exampleSentence}\"", style = LexaType.bodyLarge.copy(fontFamily = PlayfairDisplay, fontStyle = FontStyle.Italic), color = LexaColors.TextPrimary)
                }
                Spacer(Modifier.height(24.dp))
                if (!safeWord.etymology.isNullOrEmpty()) Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) { Text("ETYMOLOGY", style = LexaType.overlineSmall, color = LexaColors.TextSecondary); Spacer(Modifier.height(8.dp)); Text(safeWord.etymology ?: "", style = LexaType.bodyMedium, color = LexaColors.TextMuted, textAlign = TextAlign.Center) }
                Spacer(Modifier.height(100.dp))
            }
        }
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Brush.verticalGradient(listOf(LexaColors.BackgroundDark.copy(alpha = 0f), LexaColors.BackgroundDark, LexaColors.BackgroundDark))).padding(horizontal = 24.dp).padding(top = 48.dp, bottom = 24.dp)) {
            PrimaryButton(
                text = "Word Drop this",
                onClick = { onQuizMe(safeWord.id) }
            )
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
                tint = androidx.compose.ui.graphics.Color(activeTheme.accentColorDim),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { WordDetailScreen("ephemeral", {}, {}) } }
