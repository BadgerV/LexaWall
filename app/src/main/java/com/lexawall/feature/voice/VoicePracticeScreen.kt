package com.lexawall.feature.voice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.lexawall.core.ai.LexaSpeaker
import com.lexawall.core.components.GhostButton
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.ui.DmMono
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.LocalLexaTheme
import com.lexawall.core.ui.PlayfairDisplay
import com.lexawall.data.local.DatabaseModule
import com.lexawall.data.local.entities.WordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private enum class VoicePracticeState {
    Idle,
    Listening,
    Processing,
    Result,
    Error,
    Unsupported
}

@Composable
fun VoicePracticeScreen(
    wordId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activeTheme = LocalLexaTheme.current
    val db = remember { DatabaseModule.getInstance(context) }
    val engine = remember { VoicePracticeEngine(context) }
    var word by remember(wordId) { mutableStateOf<WordEntity?>(null) }
    var state by remember { mutableStateOf(VoicePracticeState.Idle) }
    var partialTranscript by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<VoicePracticeResult?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            engine.release()
        }
    }

    LaunchedEffect(wordId) {
        word = withContext(Dispatchers.IO) {
            db.wordDao().getWordById(wordId)
        }
        if (!engine.isAvailable()) {
            state = VoicePracticeState.Unsupported
        }
    }

    fun startListeningFlow() {
        val targetWord = word?.word.orEmpty()
        if (targetWord.isBlank()) return

        errorText = ""
        partialTranscript = ""
        result = null
        state = VoicePracticeState.Listening
        engine.startListening(
            expectedWord = targetWord,
            onListeningChanged = { listening ->
                state = if (listening) VoicePracticeState.Listening else VoicePracticeState.Processing
            },
            onPartialTranscript = { partial ->
                partialTranscript = partial
            },
            onResult = { recognition ->
                result = recognition
                state = VoicePracticeState.Result
            },
            onError = { message ->
                errorText = message
                state = VoicePracticeState.Error
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startListeningFlow()
        } else {
            errorText = "Microphone permission is required for pronunciation practice."
            state = VoicePracticeState.Error
        }
    }

    fun requestOrStartListening() {
        val hasMicPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasMicPermission) {
            startListeningFlow()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val infinite = rememberInfiniteTransition(label = "voicePracticePulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "voicePracticePulseAnim"
    )

    val safeWord = word
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(activeTheme.backgroundTop))
            .padding(horizontal = 24.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(activeTheme.accentColorDim)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 84.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "VOICE PRACTICE",
                style = LexaType.overline.copy(fontFamily = DmMono),
                color = Color(activeTheme.accentColor)
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = safeWord?.word ?: "Loading…",
                style = LexaType.wordDisplay.copy(
                    fontFamily = PlayfairDisplay,
                    fontStyle = FontStyle.Italic,
                    fontSize = 46.sp
                ),
                color = LexaColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(132.dp)
                    .scale(if (state == VoicePracticeState.Listening) pulse else 1f)
                    .background(
                        color = Color(activeTheme.accentColor).copy(alpha = 0.12f),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = Color(activeTheme.accentColor).copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                    .clickable {
                        when (state) {
                            VoicePracticeState.Unsupported -> Unit
                            VoicePracticeState.Listening -> {
                                engine.stopListening()
                                state = VoicePracticeState.Idle
                            }
                            else -> requestOrStartListening()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color(activeTheme.accentColor),
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(18.dp))
            val status = when (state) {
                VoicePracticeState.Idle -> "Tap to speak."
                VoicePracticeState.Listening -> "Listening…"
                VoicePracticeState.Processing -> "Processing…"
                VoicePracticeState.Result -> "Done."
                VoicePracticeState.Error -> errorText.ifBlank { "Try again." }
                VoicePracticeState.Unsupported -> "Speech recognition is unavailable on this device."
            }
            Text(
                text = status,
                style = LexaType.bodyMedium,
                color = LexaColors.TextSecondary,
                textAlign = TextAlign.Center
            )

            if (partialTranscript.isNotBlank() && state == VoicePracticeState.Listening) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "\"$partialTranscript\"",
                    style = LexaType.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = LexaColors.TextMuted,
                    textAlign = TextAlign.Center
                )
            }

            result?.let { scoreResult ->
                Spacer(Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LexaColors.SurfaceDark, RoundedCornerShape(14.dp))
                        .border(
                            width = 1.dp,
                            color = Color(activeTheme.accentColor).copy(alpha = 0.35f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${scoreResult.similarityScore}%",
                        style = LexaType.wordDisplay.copy(fontSize = 36.sp),
                        color = if (scoreResult.isCorrect) LexaColors.Success else Color(activeTheme.accentColor)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (scoreResult.isCorrect) "Great pronunciation." else "Close. Try once more.",
                        style = LexaType.bodyMedium,
                        color = LexaColors.TextSecondary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Heard: ${scoreResult.transcript}",
                        style = LexaType.bodySmall,
                        color = LexaColors.TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            when (state) {
                VoicePracticeState.Unsupported -> {
                    GhostButton(
                        text = "Back",
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                VoicePracticeState.Listening -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PrimaryButton(
                            text = "Stop Listening",
                            onClick = {
                                engine.stopListening()
                                state = VoicePracticeState.Idle
                            }
                        )
                        GhostButton(
                            text = "Hear word first",
                            onClick = { safeWord?.word?.let { LexaSpeaker.speak(context, it) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PrimaryButton(
                            text = "Practice Pronunciation",
                            onClick = { requestOrStartListening() }
                        )
                        GhostButton(
                            text = "Hear word first",
                            onClick = { safeWord?.word?.let { LexaSpeaker.speak(context, it) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
