package com.lexawall.wallpaper

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.service.wallpaper.WallpaperService
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.net.Uri
import android.util.TypedValue
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.lexawall.data.local.DatabaseModule
import com.lexawall.data.local.PrefsManager
import com.lexawall.data.local.entities.LevelEntity
import com.lexawall.data.local.entities.WallpaperQuizSession
import com.lexawall.domain.engine.AvatarEngine
import com.lexawall.domain.engine.XpEngine
import com.lexawall.utils.LexaWallLogger
import java.util.Locale
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.lang.ref.WeakReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

class LexaWallWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return LexaWallEngine()
    }

    inner class LexaWallEngine : Engine() {
        private var wordText: String = DEFAULT_WORD_TEXT
        private var wordDefinition: String = DEFAULT_WORD_DEFINITION
        private var wordExample: String = DEFAULT_WORD_EXAMPLE
        private var wordPos: String = DEFAULT_WORD_POS
        private var wordId: String? = null
        private var activeTheme: LexaTheme = LexaThemes.Midnight
        private var surfaceWidth: Int = 0
        private var surfaceHeight: Int = 0
        private var isScreenOff: Boolean = false
        private var isEngineVisible: Boolean = false
        private val engineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        private var rotationJob: Job? = null
        private val screenStateReceiver = ScreenStateReceiver()
        private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val wordPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val posPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val definitionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        private val examplePaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        private val ambientWordPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        private val ambientMetaPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        private val customBitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            isDither = true
        }
        private val customBitmapSrcRect = Rect()
        private val customBitmapDstRect = RectF()
        private val readabilityOverlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb((255f * 0.30f).toInt(), 0, 0, 0)
        }
        private val glassPanelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val glassPanelStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val avatarShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            alpha = 50
        }
        private val avatarBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var customBackgroundBitmap: Bitmap? = null
        private var customBackgroundPath: String? = null
        private lateinit var wallpaperQuizEngine: WallpaperQuizEngine
        private lateinit var wallpaperQuizOverlay: WallpaperQuizOverlay

        override fun onCreate(surfaceHolder: android.view.SurfaceHolder) {
            super.onCreate(surfaceHolder)
            LexaWallLogger.d("LexaWallWallpaper", "Engine onCreate")
            initPaints()
            readCurrentWallpaperData()
            wallpaperQuizOverlay = WallpaperQuizOverlay(
                context = applicationContext,
                scope = engineScope,
                isCustomBackgroundActive = { PrefsManager.isCustomImageEnabled(applicationContext) },
                onAnswered = { answeredWordId, wasCorrect ->
                    wallpaperQuizEngine.onQuizAnswered(answeredWordId, wasCorrect)
                    recordWallpaperQuizAnswer(answeredWordId, wasCorrect)
                },
                onDismissed = {
                    wallpaperQuizEngine.onQuizOverlayDismissed()
                },
                onInvalidate = {
                    drawWallpaper()
                }
            )
            wallpaperQuizEngine = WallpaperQuizEngine(
                context = applicationContext,
                scope = engineScope,
                onQuizReady = { prompt ->
                    wallpaperQuizOverlay.show(prompt)
                    drawWallpaper()
                }
            )
            // don't mark wallpaper set here; the picker preview triggers onCreate
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(screenStateReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                registerReceiver(screenStateReceiver, filter)
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            rotationJob?.cancel()
            engineScope.cancel()
            wallpaperQuizOverlay.release()
            runCatching { unregisterReceiver(screenStateReceiver) }
            clearCustomBackgroundCache()
            if (activeEngine?.get() === this) {
                activeEngine?.clear()
                activeEngine = null
            }
        }

        override fun onSurfaceChanged(
            holder: android.view.SurfaceHolder,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            surfaceWidth = width
            surfaceHeight = height
            LexaWallLogger.d("LexaWallWallpaper", "onSurfaceChanged width=$width height=$height")
            if (width <= 0 || height <= 0) return
            drawWallpaper()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            LexaWallLogger.d("LexaWallWallpaper", "onVisibilityChanged visible=$visible")
            isEngineVisible = visible
            if (visible) {
                activeEngine = WeakReference(this)
                startRotationTimer()
                WallpaperWordManager.rotateIfDue(applicationContext)
                wallpaperQuizEngine.onScreenOn()
                readCurrentWallpaperData()
                drawWallpaper()
            } else {
                rotationJob?.cancel()
                wallpaperQuizEngine.onScreenOff()
            }
        }

        override fun onSurfaceRedrawNeeded(holder: android.view.SurfaceHolder) {
            super.onSurfaceRedrawNeeded(holder)
            drawWallpaper()
        }

        override fun onCommand(
            action: String?,
            x: Int,
            y: Int,
            z: Int,
            extras: Bundle?,
            resultRequested: Boolean
        ): Bundle? {
            if (action == WallpaperManager.COMMAND_TAP) {
                onTapCommand(
                    tapType = 0,
                    x = x,
                    y = y,
                    eventTime = System.currentTimeMillis()
                )
                return null
            }
            return super.onCommand(action, x, y, z, extras, resultRequested)
        }

        private fun onTapCommand(tapType: Int, x: Int, y: Int, eventTime: Long) {
            if (wallpaperQuizOverlay.isVisible() && wallpaperQuizOverlay.handleTap(x.toFloat(), y.toFloat())) {
                return
            }

            if (!isTapOnWord(x, y)) return

            val currentWordId = PrefsManager.getCurrentWordId(applicationContext)
            if (currentWordId.isNullOrBlank() || currentWordId == "0") return

            val uri = Uri.parse("lexawall://word/$currentWordId")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            try {
                applicationContext.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                LexaWallLogger.e("LexaWall", "Deep link activity not found for wallpaper tap", e)
            }
        }

        private fun isTapOnWord(x: Int, y: Int): Boolean {
            if (surfaceWidth <= 0 || surfaceHeight <= 0) return false
            if (wordText.isBlank()) return false
            if (isScreenOff) return false

            val width = surfaceWidth.toFloat()
            val height = surfaceHeight.toFloat()
            val topInset = getTopInsetPx()
            val wordY = (height * 0.40f) + topInset
            val wordHalfWidth = wordPaint.measureText(wordText) / 2f
            val fontMetrics = wordPaint.fontMetrics
            val horizontalPadding = dpToPx(6f)
            val verticalPadding = dpToPx(6f)

            val left = (width / 2f) - wordHalfWidth - horizontalPadding
            val right = (width / 2f) + wordHalfWidth + horizontalPadding
            val top = wordY + fontMetrics.top - verticalPadding
            val bottom = wordY + fontMetrics.bottom + verticalPadding

            return x.toFloat() in left..right && y.toFloat() in top..bottom
        }

        fun refreshWallpaper() {
            LexaWallLogger.d("LexaWallWallpaper", "refreshWallpaper invoked")
            readCurrentWallpaperData()
            drawWallpaper()
        }

        fun restartRotationTimer() {
            LexaWallLogger.d("LexaWallWallpaper", "restartRotationTimer invoked")
            startRotationTimer()
        }

        fun onWallpaperQuizPreferenceChanged() {
            val enabled = PrefsManager.isWallpaperQuizOptedIn(applicationContext)
            if (!enabled) {
                wallpaperQuizEngine.onScreenOff()
                wallpaperQuizOverlay.release()
                drawWallpaper()
                return
            }
            if (isEngineVisible && !isScreenOff) {
                wallpaperQuizEngine.onScreenOn()
            }
        }

        private fun initPaints() {
            wordPaint.textAlign = Paint.Align.CENTER
            wordPaint.textSize = spToPx(72f)
            wordPaint.isFakeBoldText = true

            posPaint.textAlign = Paint.Align.CENTER
            posPaint.textSize = spToPx(18f)
            posPaint.isFakeBoldText = false

            linePaint.strokeWidth = dpToPx(1f)
            linePaint.style = Paint.Style.STROKE

            definitionPaint.textSize = spToPx(18f)
            definitionPaint.isFakeBoldText = false

            examplePaint.textSize = spToPx(13f)
            examplePaint.isFakeBoldText = false
            examplePaint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)

            ambientWordPaint.textSize = spToPx(28f)
            ambientWordPaint.color = 0x59FFFFFF
            ambientWordPaint.textAlign = Paint.Align.LEFT
            ambientWordPaint.typeface = Typeface.create("serif", Typeface.ITALIC)

            ambientMetaPaint.textSize = spToPx(12f)
            ambientMetaPaint.color = 0x33FFFFFF
            ambientMetaPaint.textAlign = Paint.Align.LEFT

            glassPanelStrokePaint.style = Paint.Style.STROKE
            glassPanelStrokePaint.strokeWidth = dpToPx(1f)
        }

        private fun startRotationTimer() {
            rotationJob?.cancel()
            LexaWallLogger.d("Rotation", "Timer started")
            rotationJob = engineScope.launch {
                while (isActive) {
                    val intervalMs = resolveRotationIntervalMs(PrefsManager.getRotationSpeed(applicationContext))
                    LexaWallLogger.d("Rotation", "Waiting ${intervalMs}ms before next rotation")
                    try {
                        delay(intervalMs)
                        LexaWallLogger.d("Rotation", "Timer fired, updating word")
                        val rotated = WallpaperWordManager.rotateIfDue(applicationContext, force = true)
                        if (rotated) {
                            wallpaperQuizEngine.onWallpaperRotated()
                        }
                        readCurrentWallpaperData()
                        drawWallpaper()
                    } catch (t: Throwable) {
                        LexaWallLogger.e("Rotation", "Rotation tick failed; continuing timer loop", t)
                    }
                }
            }
        }

        private fun resolveRotationIntervalMs(raw: String?): Long {
            return when (raw?.trim()?.lowercase(Locale.US)) {
                "1min", "1 min", "1minute", "1 minutes", "1 minute" -> 60_000L
                "5min", "5 min", "5minute", "5 minutes", "5 minute" -> 300_000L
                "10min", "10 min", "10minutes", "10 minutes", "10minute", "10 minute" -> 600_000L
                else -> 600_000L
            }
        }

        private fun drawWallpaper() {
            if (surfaceWidth <= 0 || surfaceHeight <= 0) return

            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                readCurrentWallpaperData()
                LexaWallLogger.d("LexaWallWallpaper", "drawWallpaper start isScreenOff=$isScreenOff width=$surfaceWidth height=$surfaceHeight")
                canvas = holder.lockCanvas()
                if (canvas == null) return

                val width = canvas.width.toFloat()
                val height = canvas.height.toFloat()
                if (width <= 0f || height <= 0f) return

                if (isScreenOff) {
                    drawAmbientWallpaper(canvas, width, height)
                    return
                }

                if (wallpaperQuizOverlay.draw(canvas, width, height)) {
                    return
                }

                val drewCustomBackground = drawCustomBackground(canvas, width, height)
                if (!drewCustomBackground) {
                    val gradient = LexaThemes.gradientForWordId(wordId, activeTheme)
                    gradientPaint.shader = LinearGradient(
                        0f,
                        0f,
                        0f,
                        height,
                        gradient.topColor.toInt(),
                        gradient.bottomColor.toInt(),
                        Shader.TileMode.CLAMP
                    )
                    canvas.drawRect(0f, 0f, width, height, gradientPaint)
                }

                if (drewCustomBackground) {
                    canvas.drawRect(0f, 0f, width, height, readabilityOverlayPaint)
                }

                glowPaint.shader = RadialGradient(
                    width / 2f,
                    height * 1.04f,
                    height * 0.58f,
                    intArrayOf(
                        withAlpha(IDENTITY_GOLD_ACCENT.toInt(), 0.30f),
                        withAlpha(IDENTITY_GOLD_ACCENT.toInt(), 0.12f),
                        Color.TRANSPARENT
                    ),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width, height, glowPaint)

                val baseTextColor = if (drewCustomBackground) {
                    detectOptimalTextColor(customBackgroundBitmap)
                } else {
                    0xFFF2F0E9.toInt()
                }

                wordPaint.color = baseTextColor
                posPaint.color = activeTheme.accentColor.toInt()
                linePaint.color = withAlpha(activeTheme.accentColor.toInt(), 0.5f)
                definitionPaint.color = withAlpha(baseTextColor, 0.9f)
                examplePaint.color = withAlpha(baseTextColor, 0.78f)

                val topInset = getTopInsetPx()
                val wordY = (height * 0.40f) + topInset
                val posY = (height * 0.50f) + topInset
                val lineY = (height * 0.55f) + topInset
                val defY = (height * 0.62f) + topInset

                val horizontalPadding = dpToPx(48f)
                val maxTextWidth = (width - (horizontalPadding * 2f)).toInt().coerceAtLeast(1)
                val definitionLayout = StaticLayout.Builder
                    .obtain(wordDefinition, 0, wordDefinition.length, definitionPaint, maxTextWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setIncludePad(false)
                    .build()
                val exampleTop = defY - definitionPaint.textSize + definitionLayout.height + dpToPx(16f)
                val exampleLayout = StaticLayout.Builder
                    .obtain(wordExample, 0, wordExample.length, examplePaint, maxTextWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setIncludePad(false)
                    .build()

                if (drewCustomBackground) {
                    val isLightText = isLightColor(baseTextColor)
                    val panelHorizontalInset = dpToPx(14f)
                    val panelVerticalInset = dpToPx(18f)
                    val panelLeft = (horizontalPadding - panelHorizontalInset).coerceAtLeast(dpToPx(10f))
                    val panelRight = (width - horizontalPadding + panelHorizontalInset).coerceAtMost(width - dpToPx(10f))
                    val wordTop = wordY + wordPaint.fontMetrics.top
                    val panelTop = (wordTop - panelVerticalInset).coerceAtLeast(dpToPx(20f))
                    val panelBottom = (exampleTop + exampleLayout.height + panelVerticalInset).coerceAtMost(height - dpToPx(20f))
                    val panelRadius = dpToPx(24f)
                    val panelTopColor = if (isLightText) {
                        Color.argb((255f * 0.52f).toInt(), 0, 0, 0)
                    } else {
                        Color.argb((255f * 0.42f).toInt(), 255, 255, 255)
                    }
                    val panelBottomColor = if (isLightText) {
                        Color.argb((255f * 0.34f).toInt(), 0, 0, 0)
                    } else {
                        Color.argb((255f * 0.30f).toInt(), 255, 255, 255)
                    }
                    glassPanelPaint.shader = LinearGradient(
                        0f,
                        panelTop,
                        0f,
                        panelBottom,
                        panelTopColor,
                        panelBottomColor,
                        Shader.TileMode.CLAMP
                    )
                    glassPanelStrokePaint.color = if (isLightText) {
                        Color.argb((255f * 0.28f).toInt(), 255, 255, 255)
                    } else {
                        Color.argb((255f * 0.24f).toInt(), 0, 0, 0)
                    }
                    canvas.drawRoundRect(panelLeft, panelTop, panelRight, panelBottom, panelRadius, panelRadius, glassPanelPaint)
                    canvas.drawRoundRect(panelLeft, panelTop, panelRight, panelBottom, panelRadius, panelRadius, glassPanelStrokePaint)
                    glassPanelPaint.shader = null
                }

                canvas.drawText(wordText, width / 2f, wordY, wordPaint)
                canvas.drawText(wordPos.uppercase(Locale.getDefault()), width / 2f, posY, posPaint)

                val halfLineWidth = dpToPx(48f) / 2f
                canvas.drawLine(width / 2f - halfLineWidth, lineY, width / 2f + halfLineWidth, lineY, linePaint)

                canvas.save()
                canvas.translate(horizontalPadding, defY - definitionPaint.textSize)
                definitionLayout.draw(canvas)
                canvas.restore()

                canvas.save()
                canvas.translate(horizontalPadding, exampleTop)
                exampleLayout.draw(canvas)
                canvas.restore()
            } catch (t: Throwable) {
                LexaWallLogger.e("LexaWallWallpaper", "drawWallpaper failed", t)
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }

        private fun drawAmbientWallpaper(canvas: Canvas, width: Float, height: Float) {
            canvas.drawColor(Color.BLACK)
            val horizontalPadding = dpToPx(48f)
            val baseY = height * 0.72f
            val condensed = "${wordPos.lowercase(Locale.getDefault())} · $wordDefinition"
                .replace("\n", " ")
                .trim()
            val truncated = if (condensed.length > 40) {
                condensed.take(37).trimEnd() + "..."
            } else {
                condensed
            }

            canvas.drawText(wordText, horizontalPadding, baseY, ambientWordPaint)
            canvas.drawText(
                truncated,
                horizontalPadding,
                baseY + spToPx(12f) + dpToPx(8f),
                ambientMetaPaint
            )
        }

        private fun drawCustomBackground(canvas: Canvas, width: Float, height: Float): Boolean {
            if (!PrefsManager.isCustomImageEnabled(applicationContext)) {
                clearCustomBackgroundCache()
                return false
            }

            val path = PrefsManager.getCustomImagePath(applicationContext)
                ?.takeIf { it.isNotBlank() }
                ?: return false

            val bitmap = getOrLoadCustomBackgroundBitmap(path, width.toInt(), height.toInt()) ?: return false
            if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return false

            val bitmapWidth = bitmap.width.toFloat()
            val bitmapHeight = bitmap.height.toFloat()
            val scale = max(width / bitmapWidth, height / bitmapHeight)
            val drawnWidth = bitmapWidth * scale
            val drawnHeight = bitmapHeight * scale
            val left = (width - drawnWidth) / 2f
            val top = (height - drawnHeight) / 2f

            customBitmapSrcRect.set(0, 0, bitmap.width, bitmap.height)
            customBitmapDstRect.set(left, top, left + drawnWidth, top + drawnHeight)
            canvas.drawBitmap(bitmap, customBitmapSrcRect, customBitmapDstRect, customBitmapPaint)
            return true
        }

        private fun getOrLoadCustomBackgroundBitmap(path: String, width: Int, height: Int): Bitmap? {
            if (customBackgroundPath == path && customBackgroundBitmap?.isRecycled == false) {
                return customBackgroundBitmap
            }

            clearCustomBackgroundCache()
            customBackgroundPath = path
            customBackgroundBitmap = CustomImageManager.decodeForWallpaper(path, width, height)
            return customBackgroundBitmap
        }

        private fun clearCustomBackgroundCache() {
            if (customBackgroundBitmap?.isRecycled == false) {
                customBackgroundBitmap?.recycle()
            }
            customBackgroundBitmap = null
            customBackgroundPath = null
        }

        private fun detectOptimalTextColor(bitmap: Bitmap?): Int {
            if (bitmap == null || bitmap.isRecycled) {
                return 0xFFF2F0E9.toInt()
            }
            val centerX = bitmap.width / 2
            val centerY = bitmap.height / 2
            val radius = 2 // 5x5 grid around center

            var luminanceTotal = 0.0
            var sampleCount = 0

            for (dy in -radius..radius) {
                for (dx in -radius..radius) {
                    val x = (centerX + dx).coerceIn(0, bitmap.width - 1)
                    val y = (centerY + dy).coerceIn(0, bitmap.height - 1)
                    val color = bitmap.getPixel(x, y)
                    val r = Color.red(color).toDouble()
                    val g = Color.green(color).toDouble()
                    val b = Color.blue(color).toDouble()
                    luminanceTotal += (0.299 * r + 0.587 * g + 0.114 * b)
                    sampleCount += 1
                }
            }

            if (sampleCount == 0) return 0xFFF2F0E9.toInt()
            val averageLuminance = luminanceTotal / sampleCount
            return if (averageLuminance >= 128.0) 0xFF1F1F24.toInt() else 0xFFF2F0E9.toInt()
        }

        private inner class ScreenStateReceiver : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                LexaWallLogger.d("LexaWallWallpaper", "ScreenStateReceiver action=${intent?.action}")
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        isScreenOff = true
                        wallpaperQuizEngine.onScreenOff()
                        drawWallpaper()
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        isScreenOff = false
                        wallpaperQuizEngine.onScreenOn()
                        drawWallpaper()
                    }
                }
            }
        }

        private fun recordWallpaperQuizAnswer(wordId: String, wasCorrect: Boolean) {
            engineScope.launch(Dispatchers.IO) {
                try {
                    val xpEarned = if (wasCorrect) 15 else 1
                    val db = DatabaseModule.getInstance(applicationContext)
                    val levelDao = db.levelDao()

                    val level = runCatching { levelDao.getLevel() }
                        .getOrElse {
                            val fallback = LevelEntity()
                            levelDao.insertOrReplace(fallback)
                            fallback
                        }

                    val newCurrentXp = level.currentXp + xpEarned
                    val newTotalXp = level.totalXpEarned + xpEarned
                    val newLevelDef = XpEngine.getLevelForXp(newCurrentXp)

                    levelDao.updateLevel(
                        level.copy(
                            currentXp = newCurrentXp,
                            totalXpEarned = newTotalXp,
                            currentLevel = newLevelDef.level,
                            title = newLevelDef.title
                        )
                    )

                    db.wallpaperQuizDao().insertSession(
                        WallpaperQuizSession(
                            timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                            wordId = wordId,
                            correct = wasCorrect,
                            xpEarned = xpEarned
                        )
                    )
                } catch (t: Throwable) {
                    LexaWallLogger.e("WallpaperQuiz", "Failed to persist wallpaper quiz answer", t)
                }
            }
        }

        private fun readCurrentWallpaperData() {
            val (text, definition, pos) = WallpaperWordManager.getCurrentWord(applicationContext)
            wordText = text
            wordDefinition = definition
            wordExample = WallpaperWordManager.getCurrentWordExample(applicationContext)
            wordPos = pos
            wordId = PrefsManager.getCurrentWordId(applicationContext)
            activeTheme = WallpaperWordManager.getActiveTheme(applicationContext)
            LexaWallLogger.d("LexaWallWallpaper", "readCurrentWallpaperData id=$wordId word=$wordText")
        }

        private fun spToPx(sp: Float): Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
        }

        private fun dpToPx(dp: Float): Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
        }

        private fun withAlpha(color: Int, alphaFraction: Float): Int {
            val alpha = (255 * alphaFraction).toInt().coerceIn(0, 255)
            return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
        }

        private fun isLightColor(color: Int): Boolean {
            val luminance =
                (0.299 * Color.red(color)) + (0.587 * Color.green(color)) + (0.114 * Color.blue(color))
            return luminance > 160.0
        }

        private fun drawAvatarIfEnabled(canvas: Canvas, width: Float, height: Float) {
            if (!PrefsManager.showAvatarOnWallpaper(applicationContext)) return

            val outfitId = PrefsManager.getSelectedAvatarOutfit(applicationContext)
            val outfit = AvatarEngine.getOutfit(outfitId)
            val avatarDrawable = ContextCompat.getDrawable(
                applicationContext,
                outfit.iconResourceId
            )
                ?: return

            val size = dpToPx(60f)
            val padding = dpToPx(16f)
            val left = (width - size - padding)
            val top = (height - size - padding)
            val right = left + size
            val bottom = top + size
            val centerX = left + (size / 2f)
            val centerY = top + (size / 2f)
            val radius = size / 2f

            avatarBackgroundPaint.color = avatarBackgroundForOutfit(outfitId)

            canvas.drawCircle(centerX, centerY + dpToPx(2f), radius + dpToPx(2f), avatarShadowPaint)
            canvas.drawCircle(centerX, centerY, radius, avatarBackgroundPaint)

            val inset = dpToPx(12f).toInt()
            avatarDrawable.setTintList(null)
            avatarDrawable.setBounds(
                left.toInt() + inset,
                top.toInt() + inset,
                right.toInt() - inset,
                bottom.toInt() - inset
            )
            avatarDrawable.draw(canvas)
        }

        private fun avatarBackgroundForOutfit(outfitId: String): Int {
            return when (outfitId) {
                "scholar" -> 0xFF1E1A10.toInt()
                "gold_robe" -> 0xFF241A08.toInt()
                "crown" -> 0xFF2A1808.toInt()
                "astronaut" -> 0xFF101A24.toInt()
                "emerald" -> 0xFF0F1E14.toInt()
                "storm" -> 0xFF111826.toInt()
                "arctic" -> 0xFF0E1B24.toInt()
                "nightfall" -> 0xFF141120.toInt()
                "mythic" -> 0xFF24150C.toInt()
                else -> 0xFF161622.toInt()
            }
        }

        private fun getTopInsetPx(): Float {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowManager = applicationContext.getSystemService(WindowManager::class.java)
                val insets = windowManager?.currentWindowMetrics?.windowInsets
                val top = insets?.getInsetsIgnoringVisibility(WindowInsets.Type.statusBars())?.top ?: 0
                if (top > 0) return top.toFloat()
            }

            val statusBarRes = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (statusBarRes > 0) {
                return resources.getDimensionPixelSize(statusBarRes).toFloat()
            }
            return 0f
        }
    }

    companion object {
        private var activeEngine: WeakReference<LexaWallEngine>? = null

        fun refresh() {
            activeEngine?.get()?.refreshWallpaper()
        }

        fun restartRotationTimer() {
            activeEngine?.get()?.restartRotationTimer()
        }

        fun onWallpaperQuizPreferenceChanged() {
            activeEngine?.get()?.onWallpaperQuizPreferenceChanged()
        }

        private const val DEFAULT_WORD_TEXT = "ephemeral"
        private const val DEFAULT_WORD_DEFINITION = "lasting only a short time"
        private const val DEFAULT_WORD_EXAMPLE = "Fashions are ephemeral, changing with every season."
        private const val DEFAULT_WORD_POS = "adjective"
        private const val IDENTITY_GOLD_ACCENT = 0xFFC9A74AL
        private val TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
}
