package com.lexawall.feature.worddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.GoldDivider
import com.lexawall.core.components.OverlineTag
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.components.TopBarRow
import com.lexawall.core.ui.DmMono
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LexaWallTheme
import com.lexawall.core.ui.PlayfairDisplay
import com.lexawall.data.fake.FakeWordRepository

@Composable
fun WordDetailScreen(
    wordId: String,
    onBack: () -> Unit,
    onQuizMe: (String) -> Unit
) {
    val word = FakeWordRepository().getWordById(wordId)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LexaColors.BackgroundDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopBarRow(onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(32.dp))

                // Metadata badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, LexaColors.Primary.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .background(LexaColors.Primary.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${word.level} · ${word.partOfSpeech}",
                        style = LexaType.overline.copy(fontFamily = DmMono),
                        color = LexaColors.Primary
                    )
                }
                Spacer(Modifier.height(24.dp))

                // Word
                Text(
                    text = word.text,
                    style = LexaType.wordDisplay.copy(
                        fontFamily = PlayfairDisplay,
                        fontSize = 48.sp,
                        fontStyle = FontStyle.Normal
                    ),
                    color = LexaColors.TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))

                // Pronunciation
                if (word.pronunciation.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(LexaColors.SurfaceDark)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = word.pronunciation,
                            style = LexaType.mono,
                            color = LexaColors.TextSecondary
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))

                // Definition
                Text(
                    text = word.definition,
                    style = LexaType.bodyLarge.copy(fontSize = 20.sp),
                    color = LexaColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(32.dp))

                // Gold divider
                GoldDivider()
                Spacer(Modifier.height(32.dp))

                // Example card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LexaColors.SurfaceDark)
                        .border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    OverlineTag(text = "EXAMPLE")
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "\"${word.exampleSentence}\"",
                        style = LexaType.bodyLarge.copy(
                            fontFamily = PlayfairDisplay,
                            fontStyle = FontStyle.Italic
                        ),
                        color = LexaColors.TextPrimary
                    )
                }
                Spacer(Modifier.height(24.dp))

                // Etymology
                if (word.etymology.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ETYMOLOGY",
                            style = LexaType.overlineSmall,
                            color = LexaColors.TextSecondary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = word.etymology,
                            style = LexaType.bodyMedium,
                            color = LexaColors.TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(100.dp))
            }
        }

        // Sticky bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            LexaColors.BackgroundDark.copy(alpha = 0f),
                            LexaColors.BackgroundDark,
                            LexaColors.BackgroundDark
                        )
                    )
                )
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 24.dp)
        ) {
            PrimaryButton(
                text = "Quiz me on this word",
                onClick = { onQuizMe(word.id) }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun WordDetailScreenPreview() {
    LexaWallTheme {
        WordDetailScreen(wordId = "ephemeral", onBack = {}, onQuizMe = {})
    }
}

