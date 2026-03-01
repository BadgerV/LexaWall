package com.lexawall.feature.wander

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LocalLexaTheme
import com.lexawall.core.ui.PlayfairDisplay
import com.lexawall.core.ui.components.LexaMascotAnimated
import com.lexawall.data.local.entities.WordEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WanderScreen(
    onWordTap: (Int) -> Unit,
    viewModel: WanderViewModel = viewModel()
) {
    val theme = LocalLexaTheme.current
    val words by viewModel.wanderWords.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val today = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()))
    }

    if (isLoading && words.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .background(Color(theme.backgroundTop)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LexaMascotAnimated(size = 48.dp)
                Spacer(Modifier.size(12.dp))
                Text(
                    "Finding words for you…",
                    style = LexaType.bodyMedium.copy(fontSize = 14.sp),
                    color = Color(0xFF888888),
                    maxLines = 1
                )
            }
        }
        return
    }

    if (!isLoading && words.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .background(Color(theme.backgroundTop)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LexaMascotAnimated(size = 48.dp)
                Spacer(Modifier.size(12.dp))
                Text(
                    "You've wandered far.",
                    style = LexaType.bodyLarge.copy(fontFamily = PlayfairDisplay, fontSize = 16.sp),
                    color = Color.White
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    "Come back tomorrow for more.",
                    style = LexaType.bodySmall.copy(fontSize = 13.sp),
                    color = Color(0xFF888888)
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(Color(theme.backgroundTop))
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LexaMascotAnimated(size = 28.dp)
                Spacer(Modifier.size(12.dp))
                Column {
                    Text(
                        text = "Wander",
                    style = LexaType.displaySmall.copy(
                            fontFamily = PlayfairDisplay,
                            fontSize = 28.sp
                        ),
                        color = Color(theme.accentColor)
                    )
                    Text(
                        text = today,
                        style = LexaType.bodySmall,
                        color = Color(0xFF888888)
                    )
                }
            }
        }

        itemsIndexed(words, key = { _, item -> item.id }) { index, word ->
            WanderWordRow(
                word = word,
                onClick = { onWordTap(index) }
            )
        }
    }
}

@Composable
private fun WanderWordRow(
    word: WordEntity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(LocalLexaTheme.current.surfaceColor))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = word.word,
                style = LexaType.titleLarge.copy(
                    fontFamily = PlayfairDisplay,
                    fontSize = 18.sp
                ),
                color = Color.White
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = word.partOfSpeech.uppercase(Locale.getDefault()),
                style = LexaType.monoSmall.copy(fontSize = 10.sp),
                color = Color(LocalLexaTheme.current.accentColor)
            )
        }
        Spacer(Modifier.size(6.dp))
        Text(
            text = word.definition,
            style = LexaType.bodySmall.copy(fontSize = 13.sp),
            color = Color(0xFF888888),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.size(16.dp))
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(LocalLexaTheme.current.surfaceColor))
        )
    }
}
