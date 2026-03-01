package com.lexawall.feature.settings

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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lexawall.core.ui.DmMono
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LexaWallTheme
import com.lexawall.core.ui.LocalLexaTheme
import com.lexawall.core.ui.LocalLexaThemeState
import com.lexawall.data.local.PrefsManager
import com.lexawall.domain.engine.XpEngine
import com.lexawall.wallpaper.LexaTheme
import com.lexawall.wallpaper.LexaThemes
import com.lexawall.wallpaper.LexaWallWallpaperService
import com.lexawall.wallpaper.WallpaperWordManager
import kotlinx.coroutines.launch

@Composable
fun ThemePickerScreen(
    onBack: () -> Unit,
    viewModel: ThemePickerViewModel = viewModel()
) {
    val context = LocalContext.current
    val themeState = LocalLexaThemeState.current
    val activeTheme = LocalLexaTheme.current
    val currentXp by viewModel.currentXp.collectAsState()
    val unlockedKeys = remember(currentXp) { XpEngine.getAllUnlockedThemes(currentXp).toSet() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedKey by remember(activeTheme.key) { mutableStateOf(activeTheme.key) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(Color(activeTheme.backgroundTop))
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(activeTheme.accentColorDim),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "Themes",
                    style = LexaType.titleMedium,
                    color = Color(0xFFE8E8E8)
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(LexaThemes.all, key = { it.key }) { theme ->
                    val isUnlocked = unlockedKeys.contains(theme.key)
                    val isSelected = selectedKey == theme.key
                    ThemeCell(
                        theme = theme,
                        selected = isSelected,
                        unlocked = isUnlocked,
                        requiredTitle = requiredTitle(theme.unlockLevel)
                    ) {
                        if (!isUnlocked) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Reach ${requiredTitle(theme.unlockLevel)} to unlock this theme."
                                )
                            }
                        } else {
                            selectedKey = theme.key
                            PrefsManager.saveActiveTheme(context, theme.key)
                            WallpaperWordManager.saveActiveTheme(context, theme.key)
                            themeState.value = theme
                            LexaWallWallpaperService.refresh()
                            scope.launch { snackbarHostState.showSnackbar("Theme updated") }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun ThemeCell(
    theme: LexaTheme,
    selected: Boolean,
    unlocked: Boolean,
    requiredTitle: String,
    onClick: () -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) Color(theme.accentColor) else Color(0x33FFFFFF),
                    shape = RoundedCornerShape(12.dp)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(theme.backgroundTop), Color(theme.backgroundBottom))
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .alpha(if (unlocked) 1f else 0.4f)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(width = 56.dp, height = 80.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(theme.backgroundTop), Color(theme.backgroundBottom))
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .border(0.5.dp, Color(0x44FFFFFF), RoundedCornerShape(10.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(8.dp)
                            .background(Color(theme.accentColor), CircleShape)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = theme.displayName,
                    style = LexaType.monoSmall.copy(fontFamily = DmMono, fontSize = 10.sp),
                    color = Color(0xFFE8E8E8)
                )
            }

            if (!unlocked) {
                Text(
                    text = "🔒",
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                )
            }
        }

        if (!unlocked) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Unlock at $requiredTitle",
                style = LexaType.bodySmall.copy(fontSize = 9.sp),
                color = Color(LocalLexaTheme.current.accentColorDim)
            )
        }
    }
}

private fun requiredTitle(unlockLevel: Int): String {
    return XpEngine.LEVELS.firstOrNull { it.level == unlockLevel }?.title ?: "Level $unlockLevel"
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewThemePicker() {
    LexaWallTheme {
        ThemePickerScreen(onBack = {})
    }
}
