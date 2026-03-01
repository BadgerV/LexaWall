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
import com.lexawall.data.local.PrefsManager
import com.lexawall.utils.LexaWallLogger
import java.util.Locale
import java.lang.ref.WeakReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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

        override fun onCreate(surfaceHolder: android.view.SurfaceHolder) {
            super.onCreate(surfaceHolder)
            initPaints()
            readCurrentWallpaperData()
            PrefsManager.saveWallpaperSet(applicationContext, true)
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(screenStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(screenStateReceiver, filter)
            }
            activeEngine = WeakReference(this)
        }

        override fun onDestroy() {
            super.onDestroy()
            rotationJob?.cancel()
            engineScope.cancel()
            runCatching { unregisterReceiver(screenStateReceiver) }
            activeEngine?.clear()
            activeEngine = null
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
            if (width <= 0 || height <= 0) return
            drawWallpaper()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                startRotationTimer()
            } else {
                rotationJob?.cancel()
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
            readCurrentWallpaperData()
            if (isVisible) {
                startRotationTimer()
            }
            drawWallpaper()
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
        }

        private fun startRotationTimer() {
            rotationJob?.cancel()
            LexaWallLogger.d("Rotation", "Timer started")
            rotationJob = engineScope.launch {
                while (isActive) {
                    val intervalMs = when (PrefsManager.getRotationSpeed(applicationContext)) {
                        "1min" -> 60_000L
                        "5min" -> 300_000L
                        else -> 600_000L
                    }
                    LexaWallLogger.d("Rotation", "Waiting ${intervalMs}ms before next rotation")
                    delay(intervalMs)
                    LexaWallLogger.d("Rotation", "Timer fired, updating word")
                    WallpaperWordManager.updateCurrentWord(applicationContext)
                    refreshWallpaper()
                }
            }
        }

        private fun drawWallpaper() {
            if (surfaceWidth <= 0 || surfaceHeight <= 0) return

            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas == null) return

                val width = canvas.width.toFloat()
                val height = canvas.height.toFloat()
                if (width <= 0f || height <= 0f) return

                if (isScreenOff) {
                    drawAmbientWallpaper(canvas, width, height)
                    return
                }

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

                wordPaint.color = 0xFFF2F0E9.toInt()
                posPaint.color = activeTheme.accentColor.toInt()
                linePaint.color = withAlpha(activeTheme.accentColor.toInt(), 0.5f)
                definitionPaint.color = withAlpha(0xFFE8E8E8.toInt(), 0.9f)
                examplePaint.color = 0xFFCCCCCC.toInt()

                val topInset = getTopInsetPx()
                val wordY = (height * 0.40f) + topInset
                val posY = (height * 0.50f) + topInset
                val lineY = (height * 0.55f) + topInset
                val defY = (height * 0.62f) + topInset

                canvas.drawText(wordText, width / 2f, wordY, wordPaint)
                canvas.drawText(wordPos.uppercase(Locale.getDefault()), width / 2f, posY, posPaint)

                val halfLineWidth = dpToPx(48f) / 2f
                canvas.drawLine(width / 2f - halfLineWidth, lineY, width / 2f + halfLineWidth, lineY, linePaint)

                val horizontalPadding = dpToPx(48f)
                val maxTextWidth = (width - (horizontalPadding * 2f)).toInt().coerceAtLeast(1)
                val definitionLayout = StaticLayout.Builder
                    .obtain(wordDefinition, 0, wordDefinition.length, definitionPaint, maxTextWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setIncludePad(false)
                    .build()
                canvas.save()
                canvas.translate(horizontalPadding, defY - definitionPaint.textSize)
                definitionLayout.draw(canvas)
                canvas.restore()

                val exampleTop = defY - definitionPaint.textSize + definitionLayout.height + dpToPx(16f)
                val exampleLayout = StaticLayout.Builder
                    .obtain(wordExample, 0, wordExample.length, examplePaint, maxTextWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setIncludePad(false)
                    .build()
                canvas.save()
                canvas.translate(horizontalPadding, exampleTop)
                exampleLayout.draw(canvas)
                canvas.restore()
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

        private inner class ScreenStateReceiver : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        isScreenOff = true
                        drawWallpaper()
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        isScreenOff = false
                        drawWallpaper()
                    }
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

        private const val DEFAULT_WORD_TEXT = "ephemeral"
        private const val DEFAULT_WORD_DEFINITION = "lasting only a short time"
        private const val DEFAULT_WORD_EXAMPLE = "Fashions are ephemeral, changing with every season."
        private const val DEFAULT_WORD_POS = "adjective"
        private const val IDENTITY_GOLD_ACCENT = 0xFFC9A74AL
    }
}
