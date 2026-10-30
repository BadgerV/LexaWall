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
import androidx.compose.ui.draw.alpha
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
import com.lexawall.notifications.NotificationScheduler
import com.lexawall.utils.LexaWallLogger
import com.lexawall.wallpaper.CustomImageManager
import com.lexawall.wallpaper.LexaWallWallpaperService
import com.lexawall.wallpaper.LexaThemes
import com.lexawall.wallpaper.WallpaperSetupUtils
import com.lexawall.wallpaper.WallpaperWordManager
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onThemeTap: () -> Unit,
    onLevelChanged: () -> Unit = {},
    onVoicePracticeTap: () -> Unit = {},
    onWordHuntTap: () -> Unit = {},
    onProfileTap: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var showRotationDialog by remember { mutableStateOf(false) }
    var showHabitStackDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var rotationSpeed by remember { mutableStateOf(WallpaperWordManager.getRotationSpeed(context)) }
    var wallpaperQuizzesEnabled by remember { mutableStateOf(PrefsManager.isWallpaperQuizOptedIn(context)) }
    var selectedHabitHour by remember { mutableStateOf(PrefsManager.getHabitStackHour(context)) }
    var activeThemeName by remember { mutableStateOf(LexaThemes.fromKey(PrefsManager.getActiveTheme(context)).displayName) }
    var competitiveModeEnabled by remember { mutableStateOf(PrefsManager.isCompetitiveModeEnabled(context)) }
    var wallpaperSet by remember { mutableStateOf(PrefsManager.isWallpaperSet(context)) }
    var customBackgroundEnabled by remember { mutableStateOf(PrefsManager.isCustomImageEnabled(context)) }
    var customBackgroundPath by remember {
        mutableStateOf(PrefsManager.getCustomImagePath(context)?.takeIf { it.isNotBlank() })
    }
    var showRemoveCustomBackgroundDialog by remember { mutableStateOf(false) }
    val levelTitle by produceState(initialValue = "The Curious", key1 = context) {
        value = withContext(Dispatchers.IO) {
            runCatching { DatabaseModule.getInstance(context).levelDao().getLevel().title }
                .getOrElse { "The Curious" }
        }
    }

    fun syncWallpaperState() {
        val isLexaWallpaper = WallpaperSetupUtils.isLexaWallpaperActive(context)
        LexaWallLogger.d(
            "LexaWallSetup",
            "Settings syncWallpaperState isActive=$isLexaWallpaper component=${WallpaperSetupUtils.currentComponentDebug(context)}"
        )
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
        LexaWallLogger.d("LexaWallSetup", "Settings wallpaper picker returned")
        syncWallpaperState()
        scope.launch {
            repeat(12) {
                delay(250L)
                LexaWallLogger.d("LexaWallSetup", "Settings delayed recheck #${it + 1}")
                syncWallpaperState()
                if (wallpaperSet) return@launch
            }
        }
    }

    val customBackgroundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val result = CustomImageManager.saveImageFromUri(context, uri)
            result.onSuccess { savedPath ->
                PrefsManager.saveCustomImagePath(context, savedPath)
                PrefsManager.saveCustomImageEnabled(context, true)
                customBackgroundPath = savedPath
                customBackgroundEnabled = true
                LexaWallWallpaperService.refresh()
                Toast.makeText(context, "Custom background applied.", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Could not apply custom background.", Toast.LENGTH_SHORT).show()
            }
        }
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

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(contextInner: android.content.Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_WALLPAPER_CHANGED) {
                    LexaWallLogger.d("LexaWallSetup", "Settings received ACTION_WALLPAPER_CHANGED")
                    PrefsManager.saveWallpaperSet(context, true)
                    wallpaperSet = true
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_WALLPAPER_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, android.content.Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    Box(Modifier.fillMaxSize().systemBarsPadding()) {
        Column(Modifier.fillMaxSize().background(LexaColors.BackgroundDark)) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Spacer(Modifier.height(8.dp))
                val themeSubtitle = if (customBackgroundEnabled) {
                    "$activeThemeName · disabled while custom background is active"
                } else {
                    activeThemeName
                }
                SettingsRow(
                    Icons.Default.Palette,
                    LexaColors.Primary,
                    "Theme",
                    themeSubtitle,
                    onClick = {
                        if (customBackgroundEnabled) {
                            Toast.makeText(
                                context,
                                "Disable custom background to change themes.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            onThemeTap()
                        }
                    },
                    rowAlpha = if (customBackgroundEnabled) 0.45f else 1f
                )
                SettingsRow(
                    icon = Icons.Default.Image,
                    iconColor = Color(0xFF4CAF50),
                    title = "Custom Background",
                    subtitle = if (customBackgroundEnabled) "Enabled" else "Off",
                    onClick = {
                        customBackgroundPickerLauncher.launch("image/*")
                    }
                )
                if (customBackgroundEnabled) {
                    TextButton(
                        onClick = { showRemoveCustomBackgroundDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remove custom background", color = LexaColors.TextMuted)
                    }
                }
                if (!wallpaperSet) {
                    SettingsRow(
                        Icons.Default.Image,
                        Color(0xFF4CAF50),
                        "Set Wallpaper",
                        "Tap to set LexaWall as your wallpaper",
                        onClick = {
                        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                            putExtra(
                                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                ComponentName(context, LexaWallWallpaperService::class.java)
                            )
                        }
                        try {
                            LexaWallLogger.d("LexaWallSetup", "Settings launching ACTION_CHANGE_LIVE_WALLPAPER")
                            wallpaperLauncher.launch(intent)
                        } catch (e: Exception) {
                            LexaWallLogger.w("LexaWallSetup", "Settings live wallpaper picker failed, using ACTION_SET_WALLPAPER")
                            LexaWallLogger.e("LexaWallSetup", "Settings picker exception", e)
                            wallpaperLauncher.launch(Intent(Intent.ACTION_SET_WALLPAPER))
                        }
                    })
                }
                SettingsRow(
                    Icons.Default.Schedule,
                    Color(0xFF9C27B0),
                    "Rotation Speed",
                    rotationSpeedLabel(rotationSpeed),
                    onClick = { showRotationDialog = true }
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LexaColors.SurfaceDark)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LexaColors.Primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Casino,
                            contentDescription = null,
                            tint = LexaColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Wallpaper Quizzes",
                            style = LexaType.bodyLarge,
                            color = LexaColors.TextPrimary
                        )
                        Text(
                            "Get random quizzes on your wallpaper for bonus XP",
                            style = LexaType.bodySmall,
                            color = LexaColors.TextMuted
                        )
                    }
                    Switch(
                        checked = wallpaperQuizzesEnabled,
                        onCheckedChange = { enabled ->
                            wallpaperQuizzesEnabled = enabled
                            PrefsManager.saveWallpaperQuizOptedIn(context, enabled)
                            if (enabled) {
                                PrefsManager.saveNotificationQuizEnabled(context, false)
                                PrefsManager.saveNextWallpaperQuizTriggerTime(context, 0L)
                                PrefsManager.saveLastWallpaperQuizTime(context, 0L)
                                PrefsManager.saveWallpaperQuizCountToday(context, 0)
                                LexaWallWallpaperService.onWallpaperQuizPreferenceChanged()
                                Toast.makeText(
                                    context,
                                    "Wallpaper quizzes enabled! (Max 2 per day)",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                PrefsManager.saveNotificationQuizEnabled(context, true)
                                LexaWallWallpaperService.onWallpaperQuizPreferenceChanged()
                                Toast.makeText(
                                    context,
                                    "Wallpaper quizzes disabled. Notification quiz restored.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = LexaColors.Primary,
                            checkedTrackColor = LexaColors.Primary.copy(alpha = 0.4f)
                        )
                    )
                }
                SettingsRow(
                    icon = Icons.Default.Schedule,
                    iconColor = Color(0xFF9C27B0),
                    title = "Learning Routine",
                    subtitle = selectedHabitHour?.let { formatHabitHourLabel(it) } ?: "Not set",
                    onClick = { showHabitStackDialog = true }
                )
                SettingsRow(
                    icon = Icons.Default.CameraAlt,
                    iconColor = LexaColors.Primary,
                    title = "Daily Word Hunt",
                    subtitle = "Find today's word in the real world",
                    onClick = { onWordHuntTap() }
                )
                SettingsRow(
                    icon = Icons.Default.Mic,
                    iconColor = LexaColors.Primary,
                    title = "Voice Practice Mode",
                    subtitle = "Practice pronunciation",
                    onClick = { onVoicePracticeTap() }
                )
                SettingsRow(
                    icon = Icons.Default.Person,
                    iconColor = LexaColors.Primary,
                    title = "Avatar Profile",
                    subtitle = "View outfits and customization",
                    onClick = { onProfileTap() }
                )
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
                    LexaWallWallpaperService.restartRotationTimer()
                    LexaWallWallpaperService.refresh()
                    activeThemeName = LexaThemes.fromKey(PrefsManager.getActiveTheme(context)).displayName
                    showRotationDialog = false
                },
                onDismiss = { showRotationDialog = false }
            )
        }
        if (showHabitStackDialog) {
            HabitStackDialog(
                selectedHour = selectedHabitHour,
                onSelect = { selectedHabitHour = it },
                onSave = {
                    val hour = selectedHabitHour
                    if (hour != null) {
                        PrefsManager.saveHabitStackHour(context, hour)
                        NotificationScheduler.scheduleHabitStackNotification(context)
                    }
                    showHabitStackDialog = false
                },
                onClear = {
                    PrefsManager.clearHabitStackHour(context)
                    NotificationScheduler.cancelHabitStackNotification(context)
                    selectedHabitHour = null
                    showHabitStackDialog = false
                },
                onDismiss = { showHabitStackDialog = false }
            )
        }
        if (showRemoveCustomBackgroundDialog) {
            AlertDialog(
                onDismissRequest = { showRemoveCustomBackgroundDialog = false },
                title = {
                    Text(
                        text = "Remove custom background?",
                        style = LexaType.titleMedium,
                        color = LexaColors.TextPrimary
                    )
                },
                text = {
                    Text(
                        text = "This will restore your theme wallpaper.",
                        style = LexaType.bodyMedium,
                        color = LexaColors.TextMuted
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            CustomImageManager.removeCustomImage(customBackgroundPath)
                            PrefsManager.saveCustomImagePath(context, "")
                            PrefsManager.saveCustomImageEnabled(context, false)
                            customBackgroundPath = null
                            customBackgroundEnabled = false
                            showRemoveCustomBackgroundDialog = false
                            LexaWallWallpaperService.refresh()
                            Toast.makeText(context, "Custom background removed.", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Remove", color = LexaColors.Primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRemoveCustomBackgroundDialog = false }) {
                        Text("Cancel", color = LexaColors.TextMuted)
                    }
                }
            )
        }
    }
}

@Composable private fun SettingsRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    rowAlpha: Float = 1f
) {
    Row(
        Modifier
            .fillMaxWidth()
            .alpha(rowAlpha)
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

private fun formatHabitHourLabel(hour: Int): String {
    return HabitStackOptions.firstOrNull { it.hour == hour }?.label ?: "${hour}:00"
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { SettingsScreen({}, {}) } }
