package com.lexawall.feature.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lexawall.core.components.*
import com.lexawall.core.ui.*
import com.lexawall.data.local.PrefsManager

@Composable
fun OnboardingNotificationsScreen(onAllow: () -> Unit, onNoThanks: () -> Unit) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        PrefsManager.saveOnboardingComplete(context, true)
        onAllow()
    }

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(LexaColors.BackgroundDark),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).clip(RoundedCornerShape(24.dp)).background(LexaColors.SurfaceDark)) {
            Box(Modifier.fillMaxWidth().height(180.dp).background(Brush.verticalGradient(listOf(LexaColors.SurfaceHighlight, LexaColors.SurfaceDark)))) {
                Box(Modifier.align(Alignment.TopEnd).padding(16.dp).size(40.dp).clip(CircleShape).background(LexaColors.Primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Notifications, null, tint = LexaColors.Primary, modifier = Modifier.size(20.dp))
                }
            }
            Column(Modifier.fillMaxWidth().padding(24.dp)) {
                Text("Can I check in on you?", style = LexaType.headlineSmall, color = LexaColors.TextPrimary); Spacer(Modifier.height(8.dp))
                Text("I'll send one message a day. No noise. Just your word.", style = LexaType.bodyMedium, color = LexaColors.TextSecondary); Spacer(Modifier.height(28.dp))
                PrimaryButton(
                    "Yes, check in on me",
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            PrefsManager.saveOnboardingComplete(context, true)
                            onAllow()
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))
                GhostButton("Maybe later", onClick = {
                    PrefsManager.saveOnboardingComplete(context, true)
                    onNoThanks()
                })
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable private fun Preview() { LexaWallTheme { OnboardingNotificationsScreen({}, {}) } }
