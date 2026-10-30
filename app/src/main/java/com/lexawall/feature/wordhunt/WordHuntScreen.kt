package com.lexawall.feature.wordhunt

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.lexawall.core.components.GhostButton
import com.lexawall.core.components.PrimaryButton
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.PlayfairDisplay
import com.lexawall.data.local.DatabaseModule
import com.lexawall.data.local.PrefsManager
import com.lexawall.data.local.entities.LevelEntity
import com.lexawall.data.local.entities.WordEntity
import com.lexawall.data.local.entities.WordHuntSession
import com.lexawall.domain.engine.XpEngine
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed interface WordHuntUiState {
    data object CameraReady : WordHuntUiState
    data object Scanning : WordHuntUiState
    data class Success(val detectedTexts: List<String>, val awardedXp: Boolean) : WordHuntUiState
    data class NotFound(val detectedTexts: List<String>) : WordHuntUiState
    data class Error(val message: String) : WordHuntUiState
    data class AlreadyFoundToday(val word: String) : WordHuntUiState
}

@Composable
fun WordHuntScreen(
    onClose: () -> Unit,
    onSuccess: () -> Unit = onClose
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val db = remember { DatabaseModule.getInstance(context) }
    val ocrEngine = remember { WordHuntOCREngine(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    var hasCameraPermission by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var currentWord by remember { mutableStateOf<WordEntity?>(null) }
    var uiState by remember { mutableStateOf<WordHuntUiState>(WordHuntUiState.CameraReady) }

    val requestPermissionLauncher = rememberLauncherForActivityResultCompat { granted ->
        hasCameraPermission = granted
        if (!granted) {
            uiState = WordHuntUiState.Error("Camera permission is required for Word Hunt.")
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        currentWord = withContext(Dispatchers.IO) {
            val wordId = PrefsManager.getCurrentWordId(context)?.takeIf { it.isNotBlank() }
            if (wordId.isNullOrBlank()) null else db.wordDao().getWordById(wordId)
        }
    }

    DisposableEffect(hasCameraPermission, previewView) {
        if (!hasCameraPermission || previewView == null) {
            onDispose { }
        } else {
            val listener = Runnable {
                runCatching {
                    val provider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView?.surfaceProvider)
                    }
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview)
                }
            }
            cameraProviderFuture.addListener(listener, ContextCompat.getMainExecutor(context))

            onDispose {
                runCatching { cameraProviderFuture.get().unbindAll() }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            ocrEngine.release()
        }
    }

    fun processBitmap(snapshot: Bitmap) {
        val targetWord = currentWord?.word.orEmpty()
        if (targetWord.isBlank()) {
            uiState = WordHuntUiState.Error("No active word available for today.")
            return
        }

        uiState = WordHuntUiState.Scanning
        ocrEngine.scanImageForWord(
            imageBitmap = snapshot,
            targetWord = targetWord,
            onSuccess = { found, detectedTexts ->
                scope.launch {
                    val word = currentWord
                    if (word == null) {
                        uiState = WordHuntUiState.Error("No active word available for today.")
                        return@launch
                    }

                    val today = LocalDate.now().toString()
                    val alreadyFound = withContext(Dispatchers.IO) {
                        db.wordHuntDao().getSessionForWordAndDate(word.id, today)?.found == true
                    }

                    if (!WordHuntRewardPolicy.shouldAwardXp(alreadyFoundToday = alreadyFound)) {
                        uiState = WordHuntUiState.AlreadyFoundToday(word.word)
                        return@launch
                    }

                    if (found) {
                        withContext(Dispatchers.IO) {
                            db.wordHuntDao().insertSession(
                                WordHuntSession(
                                    date = today,
                                    wordId = word.id,
                                    found = true,
                                    xpEarned = 20
                                )
                            )
                            applyWordHuntXp(db, 20)
                        }
                        Toast.makeText(context, "+20 XP earned from Word Hunt", Toast.LENGTH_SHORT).show()
                        uiState = WordHuntUiState.Success(detectedTexts = detectedTexts, awardedXp = true)
                    } else {
                        withContext(Dispatchers.IO) {
                            db.wordHuntDao().insertSession(
                                WordHuntSession(
                                    date = today,
                                    wordId = word.id,
                                    found = false,
                                    xpEarned = 0
                                )
                            )
                        }
                        uiState = WordHuntUiState.NotFound(detectedTexts = detectedTexts)
                    }
                }
            },
            onError = { message ->
                uiState = WordHuntUiState.Error(message)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(LexaColors.BackgroundDark)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = LexaColors.TextSecondary
                )
            }
            Text(
                text = "Find Today's Word",
                style = LexaType.headlineLarge.copy(fontFamily = PlayfairDisplay),
                color = LexaColors.TextPrimary
            )
            Spacer(Modifier.size(48.dp))
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = currentWord?.word ?: "Loading...",
            style = LexaType.wordDisplay.copy(fontSize = 32.sp, fontFamily = PlayfairDisplay),
            color = LexaColors.Primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Found in photos? Spot it in real life!",
            style = LexaType.bodySmall,
            color = LexaColors.TextMuted,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.Black)
                .border(1.dp, LexaColors.Primary.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
        ) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            previewView = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                WordHuntReticleOverlay()
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Point camera at text",
                        style = LexaType.bodySmall,
                        color = LexaColors.TextPrimary
                    )
                }
                Text(
                    text = "Hold steady for 2 seconds",
                    style = LexaType.bodySmall,
                    color = LexaColors.TextPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Camera permission is required.",
                        style = LexaType.bodyLarge,
                        color = LexaColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton(
                        text = "Allow camera",
                        onClick = { requestPermissionLauncher.launch(Manifest.permission.CAMERA) }
                    )
                }
            }

            if (uiState is WordHuntUiState.Scanning) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = LexaColors.Primary, modifier = Modifier.size(26.dp))
                        Spacer(Modifier.height(10.dp))
                        Text("Scanning text...", style = LexaType.bodyMedium, color = LexaColors.TextPrimary)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        when (val state = uiState) {
            WordHuntUiState.CameraReady, WordHuntUiState.Scanning -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GhostButton(text = "Close", onClick = onClose)
                    IconButton(
                        onClick = {
                            val snapshot = previewView?.bitmap
                            if (snapshot == null) {
                                uiState = WordHuntUiState.Error("Unable to capture frame. Try again.")
                            } else {
                                processBitmap(snapshot)
                            }
                        },
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(LexaColors.Primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Scan",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            is WordHuntUiState.Success -> {
                ResultCard(
                    title = "Found it! 🎉 +20 XP",
                    body = "You can find ${currentWord?.word ?: "this word"} once per day",
                    details = state.detectedTexts,
                    primaryLabel = "Great! Continue",
                    onPrimary = onSuccess,
                    secondaryLabel = null,
                    onSecondary = null
                )
            }

            is WordHuntUiState.AlreadyFoundToday -> {
                ResultCard(
                    title = "You already found this word today!",
                    body = "Come back tomorrow for another +20 XP chance.",
                    details = emptyList(),
                    primaryLabel = "Back to home",
                    onPrimary = onClose,
                    secondaryLabel = null,
                    onSecondary = null
                )
            }

            is WordHuntUiState.NotFound -> {
                ResultCard(
                    title = "Hmm, ${currentWord?.word ?: "word"} not found in this image",
                    body = "Try a clearer photo or text",
                    details = state.detectedTexts,
                    primaryLabel = "Try Again",
                    onPrimary = { uiState = WordHuntUiState.CameraReady },
                    secondaryLabel = "Skip",
                    onSecondary = onClose
                )
            }

            is WordHuntUiState.Error -> {
                ResultCard(
                    title = "Scan error",
                    body = state.message,
                    details = emptyList(),
                    primaryLabel = "Try Again",
                    onPrimary = { uiState = WordHuntUiState.CameraReady },
                    secondaryLabel = "Close",
                    onSecondary = onClose
                )
            }
        }
    }
}

@Composable
private fun WordHuntReticleOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = size.minDimension * 0.22f
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        drawCircle(
            color = LexaColors.Primary.copy(alpha = 0.45f),
            radius = radius,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
        drawLine(
            color = LexaColors.Primary.copy(alpha = 0.5f),
            start = androidx.compose.ui.geometry.Offset(centerX - 18.dp.toPx(), centerY),
            end = androidx.compose.ui.geometry.Offset(centerX + 18.dp.toPx(), centerY),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = LexaColors.Primary.copy(alpha = 0.5f),
            start = androidx.compose.ui.geometry.Offset(centerX, centerY - 18.dp.toPx()),
            end = androidx.compose.ui.geometry.Offset(centerX, centerY + 18.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
private fun ResultCard(
    title: String,
    body: String,
    details: List<String>,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String?,
    onSecondary: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LexaColors.SurfaceDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = LexaType.titleMedium.copy(fontFamily = PlayfairDisplay, fontStyle = FontStyle.Italic),
            color = LexaColors.TextPrimary
        )
        Text(
            text = body,
            style = LexaType.bodyMedium,
            color = LexaColors.TextMuted
        )
        if (details.isNotEmpty()) {
            Text(
                text = details.joinToString("\n").take(220),
                style = LexaType.bodySmall,
                color = LexaColors.TextSecondary
            )
        }
        PrimaryButton(primaryLabel, onPrimary)
        if (secondaryLabel != null && onSecondary != null) {
            GhostButton(secondaryLabel, onSecondary)
        }
    }
}

private suspend fun applyWordHuntXp(db: com.lexawall.data.local.LexaWallDatabase, xpEarned: Int) {
    val levelDao = db.levelDao()
    val level = runCatching { levelDao.getLevel() }
        .getOrElse {
            val fallback = LevelEntity()
            levelDao.insertOrReplace(fallback)
            fallback
        }

    val newCurrentXp = level.currentXp + xpEarned
    val newTotalXp = level.totalXpEarned + xpEarned
    val newDefinition = XpEngine.getLevelForXp(newCurrentXp)

    levelDao.updateLevel(
        level.copy(
            currentXp = newCurrentXp,
            totalXpEarned = newTotalXp,
            currentLevel = newDefinition.level,
            title = newDefinition.title
        )
    )
}

@Composable
private fun rememberLauncherForActivityResultCompat(
    onResult: (Boolean) -> Unit
): androidx.activity.compose.ManagedActivityResultLauncher<String, Boolean> {
    return androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = onResult
    )
}
