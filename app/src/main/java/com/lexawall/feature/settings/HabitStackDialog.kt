package com.lexawall.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType

data class HabitStackOption(
    val label: String,
    val emoji: String,
    val hour: Int
)

val HabitStackOptions = listOf(
    HabitStackOption(label = "Morning (7:00 AM)", emoji = "🌅", hour = 7),
    HabitStackOption(label = "Breakfast (8:00 AM)", emoji = "🍽️", hour = 8),
    HabitStackOption(label = "Commute (9:00 AM)", emoji = "🚗", hour = 9),
    HabitStackOption(label = "Lunch (12:00 PM)", emoji = "🍴", hour = 12),
    HabitStackOption(label = "Afternoon Break (3:00 PM)", emoji = "☕", hour = 15),
    HabitStackOption(label = "Evening (6:00 PM)", emoji = "🌆", hour = 18),
    HabitStackOption(label = "Bedtime (9:00 PM)", emoji = "🌙", hour = 21)
)

@Composable
fun HabitStackDialog(
    selectedHour: Int?,
    onSelect: (Int) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "When do you learn best?",
                style = LexaType.titleMedium,
                color = LexaColors.TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "We'll remind you at this time daily",
                    style = LexaType.bodySmall,
                    color = LexaColors.TextMuted
                )
                HabitStackOptions.forEach { option ->
                    val isSelected = selectedHour == option.hour
                    val background = if (isSelected) {
                        LexaColors.Primary.copy(alpha = 0.16f)
                    } else {
                        LexaColors.SurfaceDark
                    }
                    val border = if (isSelected) {
                        LexaColors.Primary.copy(alpha = 0.5f)
                    } else {
                        LexaColors.WhiteAlpha10
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(background, RoundedCornerShape(12.dp))
                            .border(1.dp, border, RoundedCornerShape(12.dp))
                            .clickable { onSelect(option.hour) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(option.emoji, style = LexaType.bodyLarge)
                        Spacer(Modifier.size(10.dp))
                        Text(
                            option.label,
                            style = LexaType.bodyMedium,
                            color = if (isSelected) LexaColors.Primary else LexaColors.TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            androidx.compose.material3.Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = LexaColors.Primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = LexaColors.TextMuted)
                }
                TextButton(onClick = onClear) {
                    Text("Clear", color = Color(0xFFB0B0B0))
                }
                TextButton(
                    onClick = onSave,
                    enabled = selectedHour != null
                ) {
                    Text(
                        "Save",
                        color = if (selectedHour != null) LexaColors.Primary else LexaColors.TextMuted
                    )
                }
            }
        }
    )
}
