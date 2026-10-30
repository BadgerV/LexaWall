package com.lexawall.feature.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

data class VoicePracticeResult(
    val transcript: String,
    val similarityScore: Int,
    val isCorrect: Boolean
)

class VoicePracticeEngine(context: Context) {
    private val appContext = context.applicationContext
    private var recognizer: android.speech.SpeechRecognizer? = null
    private var activeListener: RecognitionListener? = null

    fun isAvailable(): Boolean {
        return android.speech.SpeechRecognizer.isRecognitionAvailable(appContext)
    }

    fun startListening(
        expectedWord: String,
        onListeningChanged: (Boolean) -> Unit,
        onPartialTranscript: (String) -> Unit,
        onResult: (VoicePracticeResult) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isAvailable()) {
            onError("Speech recognition is not available on this device.")
            return
        }

        stopListening()
        recognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(appContext).also { sr ->
            val listener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    onListeningChanged(true)
                }

                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit

                override fun onError(error: Int) {
                    onListeningChanged(false)
                    onError(mapError(error))
                }

                override fun onResults(results: Bundle?) {
                    onListeningChanged(false)
                    val transcript = results
                        ?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?.trim()
                        .orEmpty()

                    if (transcript.isBlank()) {
                        onError("Could not hear you clearly. Try again.")
                        return
                    }

                    val score = similarity(expectedWord, transcript)
                    onResult(
                        VoicePracticeResult(
                            transcript = transcript,
                            similarityScore = score,
                            isCorrect = score >= 80
                        )
                    )
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults
                        ?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?.trim()
                        .orEmpty()
                    if (partial.isNotBlank()) {
                        onPartialTranscript(partial)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            }

            activeListener = listener
            sr.setRecognitionListener(listener)
            sr.startListening(
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }
            )
        }
    }

    fun stopListening() {
        runCatching { recognizer?.stopListening() }
        runCatching { recognizer?.cancel() }
        runCatching {
            recognizer?.setRecognitionListener(null)
            recognizer?.destroy()
        }
        recognizer = null
        activeListener = null
    }

    fun release() {
        stopListening()
    }

    private fun mapError(error: Int): String {
        return when (error) {
            android.speech.SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
            android.speech.SpeechRecognizer.ERROR_CLIENT -> "Speech recognizer client error."
            android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
            android.speech.SpeechRecognizer.ERROR_NETWORK,
            android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network issue while recognizing speech."
            android.speech.SpeechRecognizer.ERROR_NO_MATCH -> "No match found. Try saying the word again."
            android.speech.SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy. Try again."
            android.speech.SpeechRecognizer.ERROR_SERVER -> "Speech recognizer server error."
            android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timed out. Try again."
            else -> "Speech recognition failed. Try again."
        }
    }

    private fun similarity(expected: String, actual: String): Int {
        val normalizedExpected = normalize(expected)
        val normalizedActual = normalize(actual)
        if (normalizedExpected.isBlank() || normalizedActual.isBlank()) return 0

        val distance = levenshtein(normalizedExpected, normalizedActual)
        val maxLength = max(normalizedExpected.length, normalizedActual.length)
        if (maxLength <= 0) return 100
        val score = ((1f - (distance.toFloat() / maxLength.toFloat())) * 100f)
            .roundToInt()
            .coerceIn(0, 100)
        return score
    }

    private fun normalize(input: String): String {
        return input
            .lowercase(Locale.US)
            .replace(Regex("[^a-z ]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val costs = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var last = i - 1
            costs[0] = i
            for (j in 1..b.length) {
                val old = costs[j]
                val replace = last + if (a[i - 1] == b[j - 1]) 0 else 1
                val insert = costs[j] + 1
                val delete = costs[j - 1] + 1
                costs[j] = minOf(replace, insert, delete)
                last = old
            }
        }
        return costs[b.length]
    }
}
