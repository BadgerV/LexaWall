package com.lexawall.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.ui.*
import com.lexawall.domain.model.QuizOptionState

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, showArrow: Boolean = false, enabled: Boolean = true) {
    Button(onClick = onClick, modifier = modifier.fillMaxWidth().height(56.dp), enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = LexaColors.Primary, contentColor = LexaColors.BackgroundDark,
            disabledContainerColor = LexaColors.Primary.copy(alpha = 0.4f), disabledContentColor = LexaColors.BackgroundDark.copy(alpha = 0.5f)),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text = text, style = LexaType.labelLarge)
        if (showArrow) { Spacer(Modifier.width(8.dp)); Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(20.dp)) }
    }
}

@Composable
fun GhostButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, textColor: Color = LexaColors.TextMuted) {
    TextButton(onClick = onClick, modifier = modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
        Text(text = text, style = LexaType.labelMedium, color = textColor)
    }
}

@Composable
fun OverlineTag(text: String, modifier: Modifier = Modifier, color: Color = LexaColors.Primary) {
    Text(text = text.uppercase(), style = LexaType.overline, color = color, modifier = modifier)
}

@Composable
fun SmallTag(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(4.dp)).background(LexaColors.SurfaceHighlight).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(text = text.uppercase(), style = LexaType.overlineSmall, color = LexaColors.TextSecondary)
    }
}

@Composable
fun ProgressDotsRow(total: Int, current: Int, answered: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 0 until total) {
            val color = when { i < answered -> LexaColors.Primary; i == current -> LexaColors.TextPrimary.copy(alpha = 0.9f); else -> LexaColors.SurfaceHighlight }
            Box(modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(color))
        }
    }
}

@Composable
fun QuizOptionRow(text: String, state: QuizOptionState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (bgColor, borderColor, radioColor) = when (state) {
        QuizOptionState.Default -> Triple(LexaColors.SurfaceDark, LexaColors.SurfaceHighlight, LexaColors.TextMuted)
        QuizOptionState.Selected -> Triple(LexaColors.Primary.copy(alpha = 0.1f), LexaColors.Primary, LexaColors.Primary)
        QuizOptionState.Correct -> Triple(LexaColors.Success.copy(alpha = 0.1f), LexaColors.Success, LexaColors.Success)
        QuizOptionState.Wrong -> Triple(LexaColors.ErrorRed.copy(alpha = 0.1f), LexaColors.ErrorRed, LexaColors.ErrorRed)
    }
    Row(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bgColor)
        .border(width = if (state == QuizOptionState.Default) 1.dp else 2.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
        .clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(24.dp).clip(CircleShape).then(
            when (state) { QuizOptionState.Default -> Modifier.border(2.dp, radioColor, CircleShape); else -> Modifier.background(radioColor) }
        ), contentAlignment = Alignment.Center) {
            if (state == QuizOptionState.Selected || state == QuizOptionState.Correct) Icon(Icons.Default.Check, null, Modifier.size(14.dp), tint = LexaColors.BackgroundDark)
            else if (state == QuizOptionState.Wrong) Icon(Icons.Default.Close, null, Modifier.size(14.dp), tint = LexaColors.White)
        }
        Text(text = text, style = LexaType.bodyMedium, color = LexaColors.TextPrimary, modifier = Modifier.weight(1f))
    }
}

@Composable
fun GoldDivider(modifier: Modifier = Modifier) {
    Box(modifier = modifier.width(48.dp).height(1.dp).background(LexaColors.Primary.copy(alpha = 0.5f)))
}

@Composable
fun StreakBadge(days: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.clip(RoundedCornerShape(20.dp)).background(LexaColors.Primary.copy(alpha = 0.15f)).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("🔥", fontSize = 14.sp); Text("$days day streak", style = LexaType.monoSmall, color = LexaColors.Primary)
    }
}

@Composable
fun TopBarRow(modifier: Modifier = Modifier, onBack: (() -> Unit)? = null, onClose: (() -> Unit)? = null, title: String? = null) {
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = LexaColors.TextPrimary) } else Spacer(Modifier.size(48.dp))
        if (title != null) Text(title, style = LexaType.titleMedium, color = LexaColors.TextPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center) else Spacer(Modifier.weight(1f))
        if (onClose != null) IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Close", tint = LexaColors.TextPrimary) } else Spacer(Modifier.size(48.dp))
    }
}

@Composable
fun GlowCircle(icon: @Composable () -> Unit, glowColor: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(96.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(96.dp).clip(CircleShape).background(glowColor.copy(alpha = 0.15f)))
        Box(Modifier.size(72.dp).clip(CircleShape).background(glowColor.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) { icon() }
    }
}

@Composable
fun PillBadge(text: String, textColor: Color, backgroundColor: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(20.dp)).background(backgroundColor).padding(horizontal = 14.dp, vertical = 6.dp)) {
        Text(text, style = LexaType.monoSmall, color = textColor)
    }
}
