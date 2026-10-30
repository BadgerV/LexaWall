package com.lexawall.wallpaper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import com.lexawall.utils.LexaWallLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow

class WallpaperQuizOverlay(
    context: Context,
    private val scope: CoroutineScope,
    private val isCustomBackgroundActive: () -> Boolean,
    private val onAnswered: (wordId: String, wasCorrect: Boolean) -> Unit,
    private val onDismissed: () -> Unit,
    private val onInvalidate: () -> Unit
) {
    private val appContext = context.applicationContext

    private var prompt: WallpaperQuizPrompt? = null
    private var visible = false
    private var isAnswered = false
    private var selectedIndex = -1
    private var answerReported = false
    private var overallAlpha = 0f
    private var feedbackProgress = 0f
    private var tapScale = 1f
    private var tappedButtonIndex = -1

    private var enterJob: Job? = null
    private var tapJob: Job? = null
    private var feedbackJob: Job? = null
    private var autoDismissJob: Job? = null
    private var exitJob: Job? = null

    private val buttonRects = mutableListOf<RectF>()
    private val workingRect = RectF()

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val buttonBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val questionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }
    private val buttonTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }
    private val iconPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.LEFT
    }
    private val feedbackPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }
    private val subFeedbackPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }

    fun show(prompt: WallpaperQuizPrompt) {
        cancelAllJobs()
        this.prompt = prompt
        visible = true
        isAnswered = false
        selectedIndex = -1
        answerReported = false
        overallAlpha = 0f
        feedbackProgress = 0f
        tapScale = 1f
        tappedButtonIndex = -1
        buttonRects.clear()

        enterJob = scope.launch(Dispatchers.Default) {
            animate(
                durationMs = 300,
                easing = { it }
            ) { fraction ->
                overallAlpha = fraction
            }
        }
    }

    fun isVisible(): Boolean = visible

    fun handleTap(x: Float, y: Float): Boolean {
        if (!visible) return false

        if (!isAnswered) {
            val index = buttonRects.indexOfFirst { it.contains(x, y) }
            if (index < 0) return false
            onOptionTapped(index)
            return true
        }

        dismissAnimated()
        return true
    }

    fun draw(canvas: Canvas, width: Float, height: Float): Boolean {
        val prompt = this.prompt ?: return false
        if (!visible) return false

        val baseAlpha = (overallAlpha.coerceIn(0f, 1f) * 255).toInt()
        val bgColor = if (isCustomBackgroundActive()) {
            Color.parseColor("#1a1a1a")
        } else {
            Color.argb((220 * overallAlpha).toInt().coerceIn(0, 255), 5, 5, 12)
        }
        bgPaint.color = bgColor
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        questionPaint.textSize = sp(32f)
        questionPaint.color = Color.argb(baseAlpha, 255, 255, 255)
        canvas.drawText(prompt.word, width / 2f, height * 0.24f, questionPaint)

        val sidePadding = dp(16f)
        val buttonHeight = dp(56f)
        val buttonGap = dp(12f)
        val top = height * 0.34f
        val left = sidePadding
        val right = width - sidePadding
        val radius = dp(12f)

        while (buttonRects.size < 4) {
            buttonRects.add(RectF())
        }

        for (index in 0 until 4) {
            val rectTop = top + (buttonHeight + buttonGap) * index
            val rectBottom = rectTop + buttonHeight
            val rect = buttonRects[index]
            rect.set(left, rectTop, right, rectBottom)

            val buttonAlpha = if (isAnswered) 0.5f else 1f
            val color = when {
                !isAnswered && index == selectedIndex -> 0xFFC9A74A.toInt()
                isAnswered && index == prompt.correctIndex -> 0xFF4CAF50.toInt()
                isAnswered && index == selectedIndex && index != prompt.correctIndex -> 0xFFF44336.toInt()
                else -> 0xFF12121C.toInt()
            }

            val scale = if (index == tappedButtonIndex) tapScale else 1f
            val centerX = rect.centerX()
            val centerY = rect.centerY()
            val halfWidth = rect.width() / 2f * scale
            val halfHeight = rect.height() / 2f * scale
            workingRect.set(
                centerX - halfWidth,
                centerY - halfHeight,
                centerX + halfWidth,
                centerY + halfHeight
            )

            buttonPaint.color = withAlpha(color, buttonAlpha * overallAlpha)
            canvas.drawRoundRect(workingRect, radius, radius, buttonPaint)

            buttonBorderPaint.color = withAlpha(Color.WHITE, 0.08f * buttonAlpha * overallAlpha)
            buttonBorderPaint.strokeWidth = dp(1f)
            canvas.drawRoundRect(workingRect, radius, radius, buttonBorderPaint)

            val optionText = prompt.options.getOrNull(index).orEmpty()
            drawCenteredButtonText(
                canvas = canvas,
                text = optionText,
                rect = workingRect,
                alpha = buttonAlpha * overallAlpha
            )

            if (isAnswered && (index == prompt.correctIndex || index == selectedIndex)) {
                val icon = if (index == prompt.correctIndex) "✓" else "✗"
                iconPaint.textSize = sp(20f)
                iconPaint.color = withAlpha(Color.WHITE, buttonAlpha * overallAlpha)
                val iconX = workingRect.left + dp(12f)
                val iconY = workingRect.centerY() + (iconPaint.textSize * 0.32f)
                canvas.drawText(icon, iconX, iconY, iconPaint)
            }
        }

        if (isAnswered) {
            val slideOffset = dp(24f) * (1f - feedbackProgress)
            val feedbackAlpha = (overallAlpha * feedbackProgress).coerceIn(0f, 1f)
            val feedbackY = top + (buttonHeight + buttonGap) * 4f + dp(40f) + slideOffset

            val wasCorrect = selectedIndex == prompt.correctIndex
            feedbackPaint.textSize = sp(28f)
            feedbackPaint.color = withAlpha(
                if (wasCorrect) 0xFF4CAF50.toInt() else 0xFFF44336.toInt(),
                feedbackAlpha
            )
            val feedbackText = if (wasCorrect) "✓ Correct! +15 XP" else "✗ Wrong. +1 XP"
            canvas.drawText(feedbackText, width / 2f, feedbackY, feedbackPaint)

            if (!wasCorrect) {
                subFeedbackPaint.textSize = sp(16f)
                subFeedbackPaint.color = withAlpha(0xFFE8E8E8.toInt(), feedbackAlpha)
                val line = "The correct answer was: ${prompt.options[prompt.correctIndex]}"
                val maxWidth = (width - dp(32f)).toInt().coerceAtLeast(1)
                val staticLayout = StaticLayout.Builder
                    .obtain(line, 0, line.length, subFeedbackPaint, maxWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setIncludePad(false)
                    .build()
                canvas.save()
                canvas.translate(width / 2f - (maxWidth / 2f), feedbackY + dp(20f))
                staticLayout.draw(canvas)
                canvas.restore()
            }
        }

        return true
    }

    fun release() {
        cancelAllJobs()
        visible = false
        prompt = null
        buttonRects.clear()
    }

    private fun onOptionTapped(index: Int) {
        if (isAnswered || tapJob != null || prompt == null) return

        isAnswered = true
        selectedIndex = index
        tappedButtonIndex = index
        tapScale = 1f

        tapJob = scope.launch(Dispatchers.Default) {
            animate(75) { fraction ->
                tapScale = 1f - (0.05f * fraction)
            }
            animate(75) { fraction ->
                tapScale = 0.95f + (0.05f * fraction)
            }
            tapScale = 1f
            delay(100)

            val localPrompt = prompt ?: return@launch
            if (!answerReported) {
                answerReported = true
                val wasCorrect = index == localPrompt.correctIndex
                onAnswered(localPrompt.wordId, wasCorrect)
            }

            feedbackJob = scope.launch(Dispatchers.Default) {
                animate(
                    durationMs = 200,
                    easing = ::easeOutCubic
                ) { fraction ->
                    feedbackProgress = fraction
                }
            }

            autoDismissJob = scope.launch(Dispatchers.Default) {
                delay(10_000)
                dismissAnimated()
            }
        }
    }

    private fun dismissAnimated() {
        if (!visible || exitJob != null) return

        exitJob = scope.launch(Dispatchers.Default) {
            animate(300) { fraction ->
                overallAlpha = 1f - fraction
            }
            visible = false
            prompt = null
            isAnswered = false
            selectedIndex = -1
            tappedButtonIndex = -1
            feedbackProgress = 0f
            answerReported = false
            buttonRects.clear()
            onInvalidate()
            onDismissed()
        }
    }

    private suspend fun animate(
        durationMs: Long,
        easing: (Float) -> Float = { it },
        onFrame: (Float) -> Unit
    ) {
        val safeDuration = durationMs.coerceAtLeast(1L)
        val start = System.currentTimeMillis()
        while (true) {
            val elapsed = System.currentTimeMillis() - start
            val raw = (elapsed.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
            onFrame(easing(raw))
            onInvalidate()
            if (raw >= 1f) break
            delay(16)
        }
    }

    private fun drawCenteredButtonText(
        canvas: Canvas,
        text: String,
        rect: RectF,
        alpha: Float
    ) {
        buttonTextPaint.textSize = sp(18f)
        buttonTextPaint.color = withAlpha(Color.WHITE, alpha)

        val maxWidth = (rect.width() - dp(44f)).toInt().coerceAtLeast(1)
        val staticLayout = StaticLayout.Builder
            .obtain(text, 0, text.length, buttonTextPaint, maxWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setIncludePad(false)
            .build()

        val textLeft = rect.centerX() - (maxWidth / 2f)
        val textTop = rect.centerY() - (staticLayout.height / 2f)
        canvas.save()
        canvas.translate(textLeft, textTop)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun cancelAllJobs() {
        enterJob?.cancel()
        tapJob?.cancel()
        feedbackJob?.cancel()
        autoDismissJob?.cancel()
        exitJob?.cancel()

        enterJob = null
        tapJob = null
        feedbackJob = null
        autoDismissJob = null
        exitJob = null
    }

    private fun withAlpha(color: Int, alphaFraction: Float): Int {
        val alpha = (255f * alphaFraction.coerceIn(0f, 1f)).toInt()
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun easeOutCubic(value: Float): Float {
        return 1f - (1f - value).pow(3)
    }

    private fun dp(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            appContext.resources.displayMetrics
        )
    }

    private fun sp(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            value,
            appContext.resources.displayMetrics
        )
    }
}
