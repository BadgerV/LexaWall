package com.lexawall.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareCardGenerator {
    fun generateAndShareWord(
        context: Context,
        word: String,
        definition: String,
        partOfSpeech: String
    ) {
        try {
            val sizePx = 1080
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            canvas.drawColor(Color.parseColor("#1F1C13"))

            val stripPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f,
                    0f,
                    0f,
                    sizePx.toFloat(),
                    Color.argb((0.6f * 255).toInt(), 0xC9, 0xA7, 0x4A),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, 8f, sizePx.toFloat(), stripPaint)

            val partPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#C9A74A")
                textAlign = Paint.Align.CENTER
                textSize = 32f
                typeface = Typeface.MONOSPACE
                isFakeBoldText = false
            }
            val partBaseline = 92f
            canvas.drawText(partOfSpeech.uppercase(), sizePx / 2f, partBaseline, partPaint)

            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb((0.8f * 255).toInt(), 0xC9, 0xA7, 0x4A)
                strokeWidth = 1f
            }
            val firstLineY = partBaseline + 20f
            canvas.drawLine(
                (sizePx / 2f) - 60f,
                firstLineY,
                (sizePx / 2f) + 60f,
                firstLineY,
                linePaint
            )

            val wordPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#F0D080")
                textAlign = Paint.Align.CENTER
                textSize = 120f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
            }
            val wordY = sizePx * 0.38f
            canvas.drawText(word, sizePx / 2f, wordY, wordPaint)

            val definitionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb((0.9f * 255).toInt(), 0xF2, 0xF0, 0xE9)
                textSize = 36f
                textAlign = Paint.Align.LEFT
                typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            }
            val definitionWidth = 900
            val definitionX = ((sizePx - definitionWidth) / 2f)
            val definitionY = wordY + 40f
            val definitionLayout = StaticLayout.Builder
                .obtain(definition, 0, definition.length, definitionPaint, definitionWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setIncludePad(false)
                .build()

            canvas.save()
            canvas.translate(definitionX, definitionY)
            definitionLayout.draw(canvas)
            canvas.restore()

            val secondLineY = definitionY + definitionLayout.height + 60f
            canvas.drawLine(
                (sizePx / 2f) - 60f,
                secondLineY,
                (sizePx / 2f) + 60f,
                secondLineY,
                linePaint
            )

            val lexaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb((0.6f * 255).toInt(), 0xC9, 0xA7, 0x4A)
                textAlign = Paint.Align.RIGHT
                textSize = 28f
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            }
            canvas.drawText("Lexa", sizePx - 60f, sizePx - 60f, lexaPaint)

            val shareDir = File(context.cacheDir, "share_cards").apply { mkdirs() }
            val imageFile = File(shareDir, "share_card.png")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(sendIntent, "Share with").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            LexaWallLogger.e("ShareCardGenerator", "Failed to generate/share word card", e)
        }
    }
}
