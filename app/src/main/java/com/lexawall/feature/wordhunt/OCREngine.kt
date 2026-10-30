package com.lexawall.feature.wordhunt

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Locale

class WordHuntOCREngine(context: Context) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    @Suppress("unused")
    private val appContext = context.applicationContext

    fun scanImageForWord(
        imageBitmap: Bitmap,
        targetWord: String,
        onSuccess: (Boolean, List<String>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (targetWord.isBlank()) {
            onError("Target word is empty")
            return
        }

        val image = InputImage.fromBitmap(imageBitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val matchedBlocks = visionText.textBlocks
                    .map { it.text }
                    .filter { WordHuntTextMatcher.containsTargetWord(it, targetWord) }

                onSuccess(matchedBlocks.isNotEmpty(), matchedBlocks)
            }
            .addOnFailureListener { error ->
                onError("OCR failed: ${error.message ?: "Unknown error"}")
            }
    }

    fun containsTargetWord(detectedText: String, targetWord: String): Boolean {
        return WordHuntTextMatcher.containsTargetWord(detectedText, targetWord)
    }

    fun normalizeForMatching(raw: String): String {
        return WordHuntTextMatcher.normalizeForMatching(raw)
    }

    fun release() {
        recognizer.close()
    }
}

object WordHuntTextMatcher {
    fun containsTargetWord(detectedText: String, targetWord: String): Boolean {
        val normalizedDetected = normalizeForMatching(detectedText)
        val normalizedTarget = normalizeForMatching(targetWord)
        if (normalizedTarget.isBlank()) return false

        val tokenMatches = normalizedDetected
            .split(' ')
            .filter { it.isNotBlank() }
            .any { it == normalizedTarget }

        return tokenMatches || normalizedDetected.contains(normalizedTarget)
    }

    fun normalizeForMatching(raw: String): String {
        return raw
            .lowercase(Locale.getDefault())
            .replace("[^a-z0-9\\s]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
}

object WordHuntRewardPolicy {
    fun shouldAwardXp(alreadyFoundToday: Boolean): Boolean = !alreadyFoundToday
}
