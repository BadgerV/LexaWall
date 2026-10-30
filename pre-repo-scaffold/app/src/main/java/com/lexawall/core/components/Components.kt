package com.lexawall.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.DmMono
import com.lexawall.core.ui.PlayfairDisplay
import com.lexawall.domain.model.QuizOptionState

// ─── Primary Button (Gold fill, dark text, full width, 56dp, 12dp radius) ───

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showArrow: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = LexaColors.Primary,
            contentColor = LexaColors.BackgroundDark,
            disabledContainerColor = LexaColors.Primary.copy(alpha = 0.4f),
            disabledContentColor = LexaColors.BackgroundDark.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            style = LexaType.labelLarge,
        )
        if (showArrow) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─── Ghost Button (Transparent, muted text, full width) ───

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = LexaColors.TextMuted
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            style = LexaType.labelMedium,
            color = textColor
        )
    }
}

// ─── Overline Tag (Mono uppercase with gold tint background) ───

@Composable
fun OverlineTag(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LexaColors.Primary
) {
    Text(
        text = text.uppercase(),
        style = LexaType.overline,
        color = color,
        modifier = modifier
    )
}

// ─── Small Tag (Grey chip for pos/level) ───

@Composable
fun SmallTag(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(LexaColors.SurfaceHighlight)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = LexaType.overlineSmall,
            color = LexaColors.TextSecondary
        )
    }
}

// ─── Progress Dots Row ───

@Composable
fun ProgressDotsRow(
    total: Int,
    current: Int,
    answered: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0 until total) {
            val color = when {
                i < answered -> LexaColors.Primary
                i == current -> LexaColors.TextPrimary.copy(alpha = 0.9f)
                else -> LexaColors.SurfaceHighlight
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

// ─── Quiz Option Row ───

@Composable
fun QuizOptionRow(
    text: String,
    state: QuizOptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, radioColor) = when (state) {
        QuizOptionState.Default -> Triple(
            LexaColors.SurfaceDark,
            LexaColors.SurfaceHighlight,
            LexaColors.TextMuted
        )
        QuizOptionState.Selected -> Triple(
            LexaColors.Primary.copy(alpha = 0.1f),
            LexaColors.Primary,
            LexaColors.Primary
        )
        QuizOptionState.Correct -> Triple(
            LexaColors.Success.copy(alpha = 0.1f),
            LexaColors.Success,
            LexaColors.Success
        )
        QuizOptionState.Wrong -> Triple(
            LexaColors.ErrorRed.copy(alpha = 0.1f),
            LexaColors.ErrorRed,
            LexaColors.ErrorRed
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (state == QuizOptionState.Default) 1.dp else 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Radio circle
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .then(
                    when (state) {
                        QuizOptionState.Default -> Modifier.border(2.dp, radioColor, CircleShape)
                        else -> Modifier.background(radioColor)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (state == QuizOptionState.Selected || state == QuizOptionState.Correct) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = LexaColors.BackgroundDark
                )
            } else if (state == QuizOptionState.Wrong) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = LexaColors.White
                )
            }
        }
        Text(
            text = text,
            style = LexaType.bodyMedium,
            color = LexaColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

// ─── Gold Divider (48dp wide, 1dp, 50% gold opacity) ───

@Composable
fun GoldDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(48.dp)
            .height(1.dp)
            .background(LexaColors.Primary.copy(alpha = 0.5f))
    )
}

// ─── Streak Badge ───

@Composable
fun StreakBadge(
    days: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(LexaColors.Primary.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("🔥", fontSize = 14.sp)
        Text(
            text = "$days day streak",
            style = LexaType.monoSmall,
            color = LexaColors.Primary
        )
    }
}

// ─── Top Bar Row ───

@Composable
fun TopBarRow(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    title: String? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = LexaColors.TextPrimary
                )
            }
        } else {
            Spacer(Modifier.size(48.dp))
        }

        if (title != null) {
            Text(
                text = title,
                style = LexaType.titleMedium,
                color = LexaColors.TextPrimary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        } else {
            Spacer(Modifier.weight(1f))
        }

        if (onClose != null) {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = LexaColors.TextPrimary
                )
            }
        } else if (trailingContent != null) {
            trailingContent()
        } else {
            Spacer(Modifier.size(48.dp))
        }
    }
}

// ─── Word Display Composable (reused across many screens) ───

@Composable
fun WordDisplayLarge(
    word: String,
    modifier: Modifier = Modifier,
    color: Color = LexaColors.TextPrimary
) {
    Text(
        text = word,
        style = LexaType.wordDisplay,
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

// ─── Glow Circle (for quiz feedback) ───

@Composable
fun GlowCircle(
    icon: @Composable () -> Unit,
    glowColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(96.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(glowColor.copy(alpha = 0.15f))
        )
        // Inner circle
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(glowColor.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}

// ─── Pill Badge ───

@Composable
fun PillBadge(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = LexaType.monoSmall,
            color = textColor
        )
    }
}

