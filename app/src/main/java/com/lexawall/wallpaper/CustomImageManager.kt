package com.lexawall.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.lexawall.utils.LexaWallLogger
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

object CustomImageManager {
    private const val DIRECTORY_NAME = "custom_backgrounds"
    private const val FILE_NAME = "wallpaper_custom_background.jpg"
    private const val TARGET_WIDTH_PX = 1080
    private const val TARGET_HEIGHT_PX = 1920
    private const val MAX_FILE_SIZE_BYTES = 2 * 1024 * 1024
    private const val QUALITY_START = 95
    private const val QUALITY_MIN = 5
    private const val QUALITY_STEP = 5

    suspend fun saveImageFromUri(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val source = resolver.openInputStream(uri)
                ?: error("Unable to open selected image")

            val decoded = source.use { input ->
                BitmapFactory.decodeStream(input)
            } ?: error("Unable to decode selected image")

            val resized = Bitmap.createScaledBitmap(
                decoded,
                TARGET_WIDTH_PX,
                TARGET_HEIGHT_PX,
                true
            )
            if (resized !== decoded) {
                decoded.recycle()
            }

            val outputDir = File(context.cacheDir, DIRECTORY_NAME).apply { mkdirs() }
            val outputFile = File(outputDir, FILE_NAME)

            val jpegBytes = compressUnderLimit(resized)
            check(jpegBytes != null) {
                "Unable to compress selected image under size limit"
            }

            FileOutputStream(outputFile).use { output ->
                output.write(jpegBytes)
                output.flush()
            }

            if (!resized.isRecycled) {
                resized.recycle()
            }
            outputFile.absolutePath
        }.onFailure {
            LexaWallLogger.e("CustomImage", "Failed to save custom image", it)
        }
    }

    private fun compressUnderLimit(bitmap: Bitmap): ByteArray? {
        var quality = QUALITY_START
        while (quality >= QUALITY_MIN) {
            val stream = ByteArrayOutputStream()
            val compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            if (!compressed) {
                stream.close()
                return null
            }
            val bytes = stream.toByteArray()
            stream.close()
            if (bytes.size <= MAX_FILE_SIZE_BYTES) {
                return bytes
            }
            quality -= QUALITY_STEP
        }

        val finalStream = ByteArrayOutputStream()
        val compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY_MIN, finalStream)
        if (!compressed) {
            finalStream.close()
            return null
        }
        val bytes = finalStream.toByteArray()
        finalStream.close()
        return if (bytes.size <= MAX_FILE_SIZE_BYTES) bytes else null
    }

    internal fun resizeTargetDimensions(
        sourceWidth: Int,
        sourceHeight: Int,
        maxLongEdge: Int = TARGET_HEIGHT_PX
    ): Pair<Int, Int>? {
        val longEdge = max(sourceWidth, sourceHeight)
        if (longEdge <= maxLongEdge) return null
        val scale = maxLongEdge.toFloat() / longEdge.toFloat()
        val targetWidth = (sourceWidth * scale).toInt().coerceAtLeast(1)
        val targetHeight = (sourceHeight * scale).toInt().coerceAtLeast(1)
        return targetWidth to targetHeight
    }

    internal fun calculateInSampleSize(
        sourceWidth: Int,
        sourceHeight: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        var halfWidth = sourceWidth / 2
        var halfHeight = sourceHeight / 2
        while (halfWidth / inSampleSize >= reqWidth && halfHeight / inSampleSize >= reqHeight) {
            inSampleSize *= 2
        }
        return inSampleSize.coerceAtLeast(1)
    }

    internal fun compressQualityForLimit(
        maxBytes: Int,
        estimateBytesAtQuality: (Int) -> Int
    ): Int? {
        var quality = QUALITY_START
        while (quality >= QUALITY_MIN) {
            if (estimateBytesAtQuality(quality) <= maxBytes) {
                return quality
            }
            quality -= QUALITY_STEP
        }
        return null
    }

    fun decodeForWallpaper(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val file = File(path)
        if (!file.exists()) return null

        return runCatching {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)
            if (options.outWidth <= 0 || options.outHeight <= 0) return null

            val sampled = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inSampleSize = calculateInSampleSize(
                    sourceWidth = options.outWidth,
                    sourceHeight = options.outHeight,
                    reqWidth = reqWidth.coerceAtLeast(1),
                    reqHeight = reqHeight.coerceAtLeast(1)
                )
            }
            BitmapFactory.decodeFile(path, sampled)
        }.onFailure {
            LexaWallLogger.e("CustomImage", "Failed to decode custom image for wallpaper", it)
        }.getOrNull()
    }

    fun removeCustomImage(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }.onFailure {
            LexaWallLogger.e("CustomImage", "Failed to remove custom image", it)
        }
    }
}
