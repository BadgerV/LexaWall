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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LexaWallTheme

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onThemeTap: () -> Unit,
    onResetConfirmed: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LexaColors.BackgroundDark)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
                    .padding(top = 40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = LexaColors.TextPrimary
                    )
                }
                Text(
                    text = "Settings",
                    style = LexaType.titleMedium,
                    color = LexaColors.TextPrimary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.size(48.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // Theme
                SettingsRow(
                    icon = Icons.Default.Palette,
                    iconColor = LexaColors.Primary,
                    title = "Theme",
                    subtitle = "Midnight",
                    onClick = onThemeTap
                )

                // Rotation Speed
                SettingsRow(
                    icon = Icons.Default.Schedule,
                    iconColor = Color(0xFF9C27B0),
                    title = "Rotation Speed",
                    subtitle = "10s",
                    onClick = {}
                )

                // My Level
                SettingsRow(
                    icon = Icons.Default.School,
                    iconColor = LexaColors.Success,
                    title = "My Level",
                    subtitle = "B2",
                    onClick = {}
                )

                // Notifications toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LexaColors.SurfaceDark)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFF9800).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "Notifications",
                        style = LexaType.bodyLarge,
                        color = LexaColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = LexaColors.White,
                            checkedTrackColor = LexaColors.Primary,
                            uncheckedThumbColor = LexaColors.White,
                            uncheckedTrackColor = LexaColors.SurfaceHighlight
                        )
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Reset progress danger button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LexaColors.ErrorRed.copy(alpha = 0.08f))
                        .border(
                            1.dp,
                            LexaColors.ErrorRed.copy(alpha = 0.3f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { showResetDialog = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.RestartAlt,
                        contentDescription = null,
                        tint = LexaColors.ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Reset progress",
                        style = LexaType.labelLarge,
                        color = LexaColors.ErrorRed
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Version
                Text(
                    text = "LexaWall v2.4.0",
                    style = LexaType.bodySmall,
                    color = LexaColors.TextMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(32.dp))
            }
        }

        // Reset Modal
        if (showResetDialog) {
            ResetProgressModal(
                onReset = {
                    showResetDialog = false
                    onResetConfirmed()
                },
                onCancel = { showResetDialog = false }
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LexaColors.SurfaceDark)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = LexaType.bodyLarge,
                color = LexaColors.TextPrimary
            )
            Text(
                text = subtitle,
                style = LexaType.bodySmall,
                color = LexaColors.TextMuted
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = LexaColors.TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ResetProgressModal(
    onReset: () -> Unit,
    onCancel: () -> Unit
) {
    // Dark scrim overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onCancel),
        contentAlignment = Alignment.Center
    ) {
        // Modal card
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(LexaColors.SurfaceDark)
                .border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(20.dp))
                .clickable(enabled = false, onClick = {})
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Warning icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(LexaColors.ErrorRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = LexaColors.ErrorRed,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Reset progress?",
                style = LexaType.titleLarge,
                color = LexaColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Are you sure you want to reset your learning progress? This action cannot be undone and you will lose all statistics.",
                style = LexaType.bodyMedium,
                color = LexaColors.TextMuted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            // Reset button (red outline)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, LexaColors.ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onReset),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Reset Progress",
                    style = LexaType.labelMedium,
                    color = LexaColors.ErrorRed
                )
            }
            Spacer(Modifier.height(12.dp))

            // Cancel button (grey)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LexaColors.SurfaceHighlight)
                    .clickable(onClick = onCancel),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    style = LexaType.labelMedium,
                    color = LexaColors.TextSecondary
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SettingsScreenPreview() {
    LexaWallTheme {
        SettingsScreen(onBack = {}, onThemeTap = {}, onResetConfirmed = {})
    }
}

