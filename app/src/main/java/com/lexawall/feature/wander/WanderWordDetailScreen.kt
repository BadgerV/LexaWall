package com.lexawall.feature.wander

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lexawall.core.ai.LexaSpeaker
import com.lexawall.core.components.*
import com.lexawall.core.ui.*
import com.lexawall.data.local.entities.WordEntity

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun WanderWordDetailScreen(
    initialIndex: Int,
    onBack: () -> Unit,
    viewModel: WanderViewModel = viewModel()
) {
    val words by viewModel.wanderWords.collectAsState()
    val activeTheme = LocalLexaTheme.current

    if (words.isEmpty()) {
        Box(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(LexaColors.BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading...", style = LexaType.bodyMedium, color = LexaColors.TextMuted)
        }
        return
    }

    val pageCount = words.size
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, pageCount - 1),
        pageCount = { pageCount }
    )

    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(LexaColors.BackgroundDark)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val word = words[page]
            WanderWordPage(word = word)
        }

        // Page indicator
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "${pagerState.currentPage + 1} / $pageCount",
                style = LexaType.monoSmall.copy(fontFamily = DmMono, fontSize = 11.sp),
                color = LexaColors.TextMuted
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
                tint = Color(activeTheme.accentColorDim),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun WanderWordPage(word: WordEntity) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(72.dp))
        Box(
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, LexaColors.Primary.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                .background(LexaColors.Primary.copy(alpha = 0.1f))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                "${word.cefrLevel} · ${word.partOfSpeech}",
                style = LexaType.overline.copy(fontFamily = DmMono),
                color = LexaColors.Primary
            )
        }
        Spacer(Modifier.height(24.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                word.word,
                style = LexaType.wordDisplay.copy(
                    fontFamily = PlayfairDisplay,
                    fontSize = 48.sp,
                    fontStyle = FontStyle.Normal
                ),
                color = LexaColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = { LexaSpeaker.speak(context, word.word) },
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
        if (!word.pronunciationIpa.isNullOrEmpty()) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(LexaColors.SurfaceDark)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(word.pronunciationIpa, style = LexaType.mono, color = LexaColors.TextSecondary)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            word.definition,
            style = LexaType.bodyLarge.copy(fontSize = 20.sp),
            color = LexaColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        GoldDivider()
        Spacer(Modifier.height(32.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LexaColors.SurfaceDark)
                .border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            OverlineTag("EXAMPLE")
            Spacer(Modifier.height(12.dp))
            Text(
                "\"${word.exampleSentence}\"",
                style = LexaType.bodyLarge.copy(
                    fontFamily = PlayfairDisplay,
                    fontStyle = FontStyle.Italic
                ),
                color = LexaColors.TextPrimary
            )
        }
        Spacer(Modifier.height(24.dp))
        if (!word.etymology.isNullOrEmpty()) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "ETYMOLOGY",
                    style = LexaType.overlineSmall,
                    color = LexaColors.TextSecondary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    word.etymology ?: "",
                    style = LexaType.bodyMedium,
                    color = LexaColors.TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(100.dp))
    }
}
