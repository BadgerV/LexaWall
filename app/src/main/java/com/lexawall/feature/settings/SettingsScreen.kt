package com.lexawall.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.lexawall.core.ui.*
import com.lexawall.data.local.DatabaseModule
import com.lexawall.data.local.PrefsManager
import com.lexawall.wallpaper.LexaWallWallpaperService
import com.lexawall.wallpaper.LexaThemes
import com.lexawall.wallpaper.WallpaperSetupUtils
import com.lexawall.wallpaper.WallpaperWordManager
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onThemeTap: () -> Unit,
    onLevelChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showRotationDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var rotationSpeed by remember { mutableStateOf(WallpaperWordManager.getRotationSpeed(context)) }
    var activeThemeName by remember { mutableStateOf(LexaThemes.fromKey(PrefsManager.getActiveTheme(context)).displayName) }
    var competitiveModeEnabled by remember { mutableStateOf(PrefsManager.isCompetitiveModeEnabled(context)) }
    var wallpaperSet by remember { mutableStateOf(PrefsManager.isWallpaperSet(context)) }
    val levelTitle by produceState(initialValue = "The Curious", key1 = context) {
        value = withContext(Dispatchers.IO) {
            runCatching { DatabaseModule.getInstance(context).levelDao().getLevel().title }
                .getOrElse { "The Curious" }
        }
    }

    fun syncWallpaperState() {
        val isLexaWallpaper = WallpaperSetupUtils.isLexaWallpaperActive(context)
        if (isLexaWallpaper && !wallpaperSet) {
            PrefsManager.saveWallpaperSet(context, true)
            wallpaperSet = true
        } else if (!isLexaWallpaper && wallpaperSet) {
            PrefsManager.saveWallpaperSet(context, false)
            wallpaperSet = false
        }
    }

    val wallpaperLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Fired as soon as the system wallpaper picker returns, whether set or cancelled
        syncWallpaperState()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                syncWallpaperState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(Modifier.fillMaxSize().systemBarsPadding()) {
        Column(Modifier.fillMaxSize().background(LexaColors.BackgroundDark)) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Spacer(Modifier.height(8.dp))
                SettingsRow(Icons.Default.Palette, LexaColors.Primary, "Theme", activeThemeName, onThemeTap)
                if (!wallpaperSet) {
                    SettingsRow(Icons.Default.Image, Color(0xFF4CAF50), "Set Wallpaper", "Tap to set LexaWall as your wallpaper") {
                        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                            putExtra(
                                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                ComponentName(context, LexaWallWallpaperService::class.java)
                            )
                        }
                        try {
                            wallpaperLauncher.launch(intent)
                        } catch (_: Exception) {
                            wallpaperLauncher.launch(Intent(Intent.ACTION_SET_WALLPAPER))
                        }
                    }
                }
                SettingsRow(Icons.Default.Schedule, Color(0xFF9C27B0), "Rotation Speed", rotationSpeedLabel(rotationSpeed)) {
                    showRotationDialog = true
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LexaColors.SurfaceDark)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Competitive Mode",
                            style = LexaType.bodyLarge,
                            color = LexaColors.TextPrimary
                        )
                        Text(
                            "Faster cards. One session before your Word Drop.",
                            style = LexaType.bodySmall,
                            color = LexaColors.TextMuted
                        )
                    }
                    Switch(
                        checked = competitiveModeEnabled,
                        onCheckedChange = { enabled ->
                            competitiveModeEnabled = enabled
                            PrefsManager.saveCompetitiveModeEnabled(context, enabled)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = LexaColors.Primary,
                            checkedTrackColor = LexaColors.Primary.copy(alpha = 0.4f)
                        )
                    )
                }
                SettingsRow(Icons.Default.School, LexaColors.Success, "My Level", levelTitle)
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LexaColors.SurfaceDark).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFF9800).copy(alpha = 0.15f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Notifications, null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(16.dp)); Text("Notifications", style = LexaType.bodyLarge, color = LexaColors.TextPrimary, modifier = Modifier.weight(1f))
                    Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = LexaColors.White, checkedTrackColor = LexaColors.Primary, uncheckedThumbColor = LexaColors.White, uncheckedTrackColor = LexaColors.SurfaceHighlight))
                }
                Spacer(Modifier.height(32.dp)); Text("LexaWall v2.4.0", style = LexaType.bodySmall, color = LexaColors.TextMuted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center); Spacer(Modifier.height(32.dp))
            }
        }
        if (showRotationDialog) {
            SelectionDialog(
                title = "Rotation speed",
                options = listOf("1 min", "5 min", "10 min"),
                onSelect = { selected ->
                    rotationSpeed = when (selected) {
                        "1 min" -> "1min"
                        "5 min" -> "5min"
                        else -> "10min"
                    }
                    PrefsManager.saveRotationSpeed(context, rotationSpeed)
                    LexaWallWallpaperService.refresh()
                    activeThemeName = LexaThemes.fromKey(PrefsManager.getActiveTheme(context)).displayName
                    showRotationDialog = false
                },
                onDismiss = { showRotationDialog = false }
            )
        }
    }
}

@Composable private fun SettingsRow(icon: ImageVector, iconColor: Color, title: String, subtitle: String, onClick: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LexaColors.SurfaceDark)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.width(16.dp)); Column(Modifier.weight(1f)) { Text(title, style = LexaType.bodyLarge, color = LexaColors.TextPrimary); Text(subtitle, style = LexaType.bodySmall, color = LexaColors.TextMuted) }
        if (onClick != null) {
            Icon(Icons.Default.ChevronRight, null, tint = LexaColors.TextMuted, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = LexaType.titleMedium, color = LexaColors.TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    TextButton(
                        onClick = { onSelect(option) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(option, style = LexaType.bodyLarge, color = LexaColors.TextPrimary)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

private fun rotationSpeedLabel(value: String): String {
    return when (value) {
        "1min" -> "1 min"
        "5min" -> "5 min"
        "10min" -> "10 minutes"
        else -> "10 minutes"
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { SettingsScreen({}, {}) } }
