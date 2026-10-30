package com.lexawall.feature.flashcards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.GoldDivider
import com.lexawall.core.components.SmallTag
import com.lexawall.core.ui.DmMono
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LexaWallTheme
import com.lexawall.core.ui.PlayfairDisplay
import com.lexawall.data.fake.FakeWords

@Composable
fun FlashcardsRevealedScreen(
    cardIndex: Int,
    onFlipBack: () -> Unit,
    onStillLearning: (nextIndex: Int, isLast: Boolean) -> Unit,
    onKnowIt: (nextIndex: Int, isLast: Boolean) -> Unit,
    onClose: () -> Unit
) {
    val words = FakeWords.flashcardWords
    val word = words.getOrNull(cardIndex) ?: words.first()
    val total = words.size
    val isLast = cardIndex >= words.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexaColors.BackgroundDark)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(48.dp))

        // Top row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${cardIndex + 1} OF $total",
                style = LexaType.overline.copy(fontFamily = DmMono),
                color = LexaColors.TextSecondary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .drawBehind {
                            val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            drawArc(
                                color = LexaColors.SurfaceHighlight,
                                startAngle = -90f, sweepAngle = 360f,
                                useCenter = false, style = stroke,
                                topLeft = Offset(stroke.width / 2, stroke.width / 2),
                                size = Size(size.width - stroke.width, size.height - stroke.width)
                            )
                            drawArc(
                                color = LexaColors.Primary,
                                startAngle = -90f, sweepAngle = 216f,
                                useCenter = false, style = stroke,
                                topLeft = Offset(stroke.width / 2, stroke.width / 2),
                                size = Size(size.width - stroke.width, size.height - stroke.width)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Timer, null, tint = LexaColors.Primary, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onClose, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Close, "Close", tint = LexaColors.TextPrimary)
                }
            }
        }

        // Card - revealed
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(LexaColors.CardDark)
                    .border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(24.dp))
                    .clickable(onClick = onFlipBack)
                    .padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SmallTag(text = word.partOfSpeech)
                Spacer(Modifier.height(16.dp))

                Text(
                    text = word.text,
                    style = LexaType.wordDisplay.copy(fontFamily = PlayfairDisplay, fontSize = 40.sp),
                    color = LexaColors.TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))

                GoldDivider()
                Spacer(Modifier.height(16.dp))

                // Definition (revealed)
                Text(
                    text = word.definition,
                    style = LexaType.bodyLarge,
                    color = LexaColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))

                // Example sentence (revealed)
                Text(
                    text = "\"${word.exampleSentence}\"",
                    style = LexaType.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = LexaColors.TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Bottom action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LexaColors.ErrorRed.copy(alpha = 0.08f))
                    .border(1.dp, LexaColors.ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clickable { onStillLearning(cardIndex + 1, isLast) },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.History, null, tint = LexaColors.ErrorRed.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                    Text("Still learning", style = LexaType.labelMedium, color = LexaColors.ErrorRed.copy(alpha = 0.8f))
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LexaColors.Primary)
                    .clickable { onKnowIt(cardIndex + 1, isLast) },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("I know this", style = LexaType.labelMedium, color = LexaColors.BackgroundDark)
                    Icon(Icons.Default.Check, null, tint = LexaColors.BackgroundDark, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Hint text
        Text(
            text = "TAP CARD TO FLIP BACK",
            style = LexaType.overlineSmall.copy(fontFamily = DmMono),
            color = LexaColors.TextMuted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FlashcardsRevealedScreenPreview() {
    LexaWallTheme {
        FlashcardsRevealedScreen(0, {}, { _, _ -> }, { _, _ -> }, {})
    }
}

