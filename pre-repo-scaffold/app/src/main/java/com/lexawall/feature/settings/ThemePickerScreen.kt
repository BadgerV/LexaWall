package com.lexawall.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.components.TopBarRow
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LexaWallTheme
import com.lexawall.core.ui.PlayfairDisplay
import kotlinx.coroutines.launch

data class ThemeCardData(
    val id: String,
    val name: String,
    val gradientTop: Color,
    val gradientBottom: Color,
    val accentColor: Color,
    val wordTextColor: Color,
    val sampleWord: String,
    val samplePos: String,
    val sampleDef: String,
)

private val themeCards = listOf(
    ThemeCardData(
        "midnight", "Midnight",
        Color(0xFF1A1F35), Color(0xFF0F1220),
        LexaColors.Primary, Color.White,
        "Serendipity", "Noun",
        "The occurrence of events by chance in a happy way."
    ),
    ThemeCardData(
        "savanna", "Savanna",
        Color(0xFF5C4D3C), Color(0xFF2B251E),
        Color(0xFFE6B489), Color(0xFFF3E5D8),
        "Ephemeral", "Adj",
        "Lasting for a very short time."
    ),
    ThemeCardData(
        "lagoon", "Lagoon",
        Color(0xFF2C5361), Color(0xFF12242B),
        Color(0xFF8EE3F5), Color.White,
        "Meander", "Verb",
        "To follow a winding course."
    ),
    ThemeCardData(
        "harmattan", "Harmattan",
        Color(0xFFD2B48C), Color(0xFF5D4037),
        Color(0xFF3E2723), Color(0xFF3E2723),
        "Petrichor", "Noun",
        "A pleasant smell that frequently accompanies the first rain."
    ),
)

@Composable
fun ThemePickerScreen(
    onBack: () -> Unit
) {
    var selectedTheme by remember { mutableStateOf("midnight") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LexaColors.BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(32.dp))
            TopBarRow(onBack = onBack, title = "Theme")

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(themeCards) { theme ->
                    val isActive = theme.id == selectedTheme
                    ThemeCard(
                        data = theme,
                        isActive = isActive,
                        onClick = {
                            selectedTheme = theme.id
                            scope.launch {
                                snackbarHostState.showSnackbar("Theme updated")
                            }
                        }
                    )
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { data ->
            Snackbar(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                containerColor = LexaColors.SurfaceHighlight,
                contentColor = LexaColors.TextPrimary
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = LexaColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = data.visuals.message,
                        style = LexaType.labelMedium,
                        color = LexaColors.TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(
    data: ThemeCardData,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 5f)
                .clip(RoundedCornerShape(12.dp))
                .then(
                    if (isActive) Modifier.border(2.dp, LexaColors.Primary, RoundedCornerShape(12.dp))
                    else Modifier
                )
                .background(
                    Brush.linearGradient(
                        colors = listOf(data.gradientTop, data.gradientBottom)
                    )
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = data.samplePos.uppercase(),
                    style = LexaType.overlineSmall,
                    color = data.accentColor.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = data.sampleWord,
                    style = LexaType.titleLarge.copy(
                        fontFamily = PlayfairDisplay,
                        fontSize = 20.sp
                    ),
                    color = data.wordTextColor,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = data.sampleDef,
                    style = LexaType.bodySmall.copy(fontSize = 10.sp),
                    color = data.wordTextColor.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }

            // Gold check badge for active
            if (isActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(LexaColors.Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = LexaColors.BackgroundDark,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = data.name,
                style = LexaType.labelMedium,
                color = LexaColors.TextPrimary
            )
            if (isActive) {
                Text(
                    text = "Active",
                    style = LexaType.bodySmall,
                    color = LexaColors.Primary
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ThemePickerScreenPreview() {
    LexaWallTheme {
        ThemePickerScreen(onBack = {})
    }
}

