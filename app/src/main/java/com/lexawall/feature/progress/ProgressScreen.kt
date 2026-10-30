package com.lexawall.feature.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lexawall.core.ui.DmMono
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LocalLexaTheme
import com.lexawall.core.ui.PlayfairDisplay
import com.lexawall.core.ui.components.LexaMascotAnimated
import com.lexawall.data.local.entities.WordEntity
import com.lexawall.domain.engine.XpEngine
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgressScreen(
    onWordTap: (String) -> Unit,
    viewModel: ProgressViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState(initial = ProgressUiState.Loading)
    val theme = LocalLexaTheme.current

    when (val state = uiState) {
        ProgressUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .background(Color(theme.backgroundTop)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(theme.accentColor))
            }
        }

        is ProgressUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .background(Color(theme.backgroundTop)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.message,
                    style = LexaType.bodyMedium,
                    color = LexaColors.TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }

        is ProgressUiState.Success -> {
            val nextLevel = XpEngine.getNextLevel(state.levelEntity.currentXp)
            val subtitle = if (state.lexaMessage.isNotBlank()) {
                state.lexaMessage
            } else {
                "You're building intellectual momentum."
            }
            val badgeVisuals = buildBadgeVisuals(state)
            val words = state.masteredWords

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF050508),
                                Color(theme.backgroundTop)
                            )
                        )
                    )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .padding(horizontal = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LexaMascotAnimated(size = 20.dp)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = subtitle,
                                    style = LexaType.bodyMedium.copy(
                                        fontFamily = PlayfairDisplay,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 13.sp
                                    ),
                                    color = Color(0xFFBBBBBB)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = state.levelEntity.title,
                                style = LexaType.displayLarge.copy(
                                    fontFamily = PlayfairDisplay,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 40.sp
                                ),
                                color = Color(theme.accentColor)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "LEVEL ${state.levelEntity.currentLevel}",
                                style = LexaType.monoSmall.copy(
                                    fontFamily = DmMono,
                                    fontSize = 9.sp,
                                    letterSpacing = 2.sp
                                ),
                                color = Color(theme.accentColor).copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(1.dp)
                                    .background(Color(0xFF1A1A22))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(state.progressFraction.coerceIn(0f, 1f))
                                        .height(1.dp)
                                        .background(Color(theme.accentColor))
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (nextLevel != null) {
                                    "${state.xpToNextLevel} XP TO ${nextLevel.title.uppercase()}"
                                } else {
                                    "MAX LEVEL"
                                },
                                style = LexaType.monoSmall.copy(
                                    fontFamily = DmMono,
                                    fontSize = 8.sp,
                                    letterSpacing = 1.5.sp
                                ),
                                color = Color(0xFF6B6B78)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatCell(number = state.totalMastered.toString(), label = "MASTERED", accent = Color(theme.accentColor))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = state.currentStreak.toString(),
                                        style = LexaType.displayMedium.copy(
                                            fontFamily = PlayfairDisplay,
                                            fontStyle = FontStyle.Italic,
                                            fontSize = 36.sp
                                        ),
                                        color = Color(theme.accentColor).copy(alpha = 0.9f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = Color(theme.accentColor).copy(alpha = 0.65f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Text(
                                    text = "DAY STREAK",
                                    style = LexaType.monoSmall.copy(
                                        fontFamily = DmMono,
                                        fontSize = 8.sp,
                                        letterSpacing = 1.5.sp
                                    ),
                                    color = Color(0xFF6B6B78)
                                )
                            }
                            StatCell(number = "${state.quizAccuracyPercent}%", label = "ACCURACY", accent = Color(theme.accentColor))
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val bars = if (state.weeklyXp.all { it <= 0 }) {
                                listOf(1, 1, 1, 1, 1, 1, 1)
                            } else {
                                val max = state.weeklyXp.maxOrNull()?.coerceAtLeast(1) ?: 1
                                state.weeklyXp.map { ((it.toFloat() / max) * 10f).roundToInt().coerceAtLeast(1) }
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.height(36.dp)
                            ) {
                                bars.forEachIndexed { index, size ->
                                    val alpha = when (index) {
                                        5 -> 0.8f
                                        3 -> 0.6f
                                        1, 6 -> 0.45f
                                        else -> 0.22f
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height((size * 3).dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(theme.accentColor).copy(alpha = alpha))
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Consistency builds fluency",
                                style = LexaType.bodyMedium.copy(
                                    fontFamily = PlayfairDisplay,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 13.sp
                                ),
                                color = Color(0xFF6B6B78).copy(alpha = 0.8f)
                            )
                        }
                    }

                    if (badgeVisuals.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "MILESTONES",
                                style = LexaType.monoSmall.copy(
                                    fontFamily = DmMono,
                                    fontSize = 9.sp,
                                    letterSpacing = 2.sp
                                ),
                                color = Color(theme.accentColor).copy(alpha = 0.65f)
                            )
                            Spacer(Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                badgeVisuals.forEach { badge ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.04f))
                                                .border(0.5.dp, Color.White.copy(alpha = 0.08f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = badge.icon, fontSize = 18.sp)
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = badge.label.uppercase(),
                                            style = LexaType.monoSmall.copy(
                                                fontFamily = DmMono,
                                                fontSize = 7.sp,
                                                letterSpacing = 1.sp
                                            ),
                                            color = Color(0xFF6B6B78),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                    }

                    if (words.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "YOUR WORDS",
                                style = LexaType.monoSmall.copy(
                                    fontFamily = DmMono,
                                    fontSize = 9.sp,
                                    letterSpacing = 2.sp
                                ),
                                color = Color(theme.accentColor).copy(alpha = 0.65f)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "These are part of you now.",
                                style = LexaType.bodyMedium.copy(
                                    fontFamily = PlayfairDisplay,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 13.sp
                                ),
                                color = Color(0xFF6B6B78).copy(alpha = 0.75f)
                            )
                            Spacer(Modifier.height(12.dp))
                            if (words.size <= 20) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    words.forEach { word ->
                                        WordPill(
                                            word = word,
                                            accentColorDim = Color(theme.accentColorDim),
                                            onWordTap = onWordTap
                                        )
                                    }
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 88.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 400.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(words, key = { it.id }) { word ->
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            WordPill(
                                                word = word,
                                                accentColorDim = Color(theme.accentColorDim),
                                                onWordTap = onWordTap
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    }

                    item {
                        Spacer(Modifier.height(120.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WordPill(
    word: WordEntity,
    accentColorDim: Color,
    onWordTap: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .border(0.5.dp, accentColorDim.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
            .clickable { onWordTap(word.id) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = word.word,
            style = LexaType.bodyLarge.copy(
                fontFamily = PlayfairDisplay,
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp
            ),
            color = Color(0xFFE8E8E8)
        )
    }
}

@Composable
private fun StatCell(
    number: String,
    label: String,
    accent: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            style = LexaType.displayMedium.copy(
                fontFamily = PlayfairDisplay,
                fontStyle = FontStyle.Italic,
                fontSize = 36.sp
            ),
            color = accent.copy(alpha = 0.9f)
        )
        Text(
            text = label,
            style = LexaType.monoSmall.copy(
                fontFamily = DmMono,
                fontSize = 8.sp,
                letterSpacing = 1.5.sp
            ),
            color = Color(0xFF6B6B78)
        )
    }
}

private data class BadgeVisual(val icon: String, val label: String)

private fun buildBadgeVisuals(state: ProgressUiState.Success): List<BadgeVisual> {
    if (state.badges.isEmpty()) {
        return emptyList()
    }

    return state.badges.map { badge ->
        when {
            badge.badgeKey.contains("streak") -> BadgeVisual("🔥", "Wildfire")
            badge.badgeKey.contains("word") || badge.badgeKey.contains("first") -> BadgeVisual("📚", "Reader")
            badge.badgeKey.contains("perfect") -> BadgeVisual("⭐", "Elite")
            badge.badgeKey.contains("level") -> BadgeVisual("🏆", "Victor")
            else -> BadgeVisual("🏅", badge.badgeKey.replace('_', ' '))
        }
    }.distinctBy { it.label }.take(4)
}
