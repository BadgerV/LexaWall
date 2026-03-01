package com.lexawall.core.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

object LexaSpeaker {
    private var tts: TextToSpeech? = null
    private var isReady: Boolean = false

    fun init(context: Context) {
        if (tts != null) return

        val appContext = context.applicationContext
        tts = TextToSpeech(appContext) { status ->
            isReady = status == TextToSpeech.SUCCESS
            if (isReady) {
                tts?.language = Locale.getDefault()
                tts?.setSpeechRate(0.88f)
                tts?.setPitch(0.92f)
            }
        }
    }

    fun speak(context: Context, text: String) {
        if (text.isBlank()) return
        if (tts == null) init(context)
        if (!isReady) return

        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "lexa_${System.currentTimeMillis()}"
        )
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
