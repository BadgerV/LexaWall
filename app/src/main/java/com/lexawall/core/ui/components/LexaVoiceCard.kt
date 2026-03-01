package com.lexawall.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.ai.LexaMessage
import com.lexawall.core.ui.DmMono
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.PlayfairDisplay

@Composable
fun LexaVoiceCard(message: LexaMessage?) {
    if (message == null) return

    var visible by remember(message.timestamp) { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "lexaVoiceCardAlpha"
    )

    LaunchedEffect(message.timestamp) {
        visible = true
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .background(Color(0xFF101012), RoundedCornerShape(14.dp))
            .border(1.dp, LexaColors.WhiteAlpha10, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "LEXA",
                style = LexaType.overlineSmall.copy(fontFamily = DmMono, fontSize = 9.sp),
                color = LexaColors.Primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = message.text,
                style = LexaType.bodyLarge.copy(fontFamily = PlayfairDisplay, fontStyle = FontStyle.Italic, fontSize = 16.sp),
                color = Color.White
            )
        }
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Speak",
                tint = LexaColors.Primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
