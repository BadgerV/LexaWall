package com.lexawall.core.ai

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LexaVoice {
    private const val MAX_RECENT_MESSAGES = 20

    private var lastTemplateIndex: Int = -1

    private val _recentMessages = mutableStateOf<List<LexaMessage>>(emptyList())
    val recentMessages: State<List<LexaMessage>> = _recentMessages

    suspend fun generateMessage(
        context: Context,
        trigger: LexaTrigger,
        speakAloud: Boolean = false
    ): LexaMessage {
        val message = withContext(Dispatchers.Default) {
            when (trigger) {
                is LexaTrigger.OnAppOpen -> {
                    val chosenWord = trigger.masteredWords.randomOrNull()?.trim().orEmpty()
                    val text = if (chosenWord.isNotEmpty()) {
                        renderWithWord(
                            template = pickTemplate(LexaTemplates.onAppOpenWithWord),
                            word = chosenWord
                        )
                    } else {
                        pickTemplate(LexaTemplates.onStreakBroken)
                    }
                    LexaMessage(
                        text = text,
                        timestamp = System.currentTimeMillis(),
                        type = LexaMessageType.GREETING
                    )
                }

                is LexaTrigger.OnQuizComplete -> {
                    val chosenWord = trigger.hardestWord.trim()
                    val text = if (chosenWord.isNotEmpty()) {
                        renderWithWord(
                            template = pickTemplate(LexaTemplates.onQuizCompleteWithWord),
                            word = chosenWord
                        )
                    } else {
                        "You finished today's Word Drop."
                    }
                    LexaMessage(
                        text = text,
                        timestamp = System.currentTimeMillis(),
                        type = LexaMessageType.INSIGHT
                    )
                }

                is LexaTrigger.OnStreakBroken -> {
                    LexaMessage(
                        text = pickTemplate(LexaTemplates.onStreakBroken),
                        timestamp = System.currentTimeMillis(),
                        type = LexaMessageType.NUDGE
                    )
                }

                is LexaTrigger.OnFlashcardComplete -> {
                    val chosenWord = trigger.wordsSeenToday.randomOrNull()?.trim().orEmpty()
                    val text = if (chosenWord.isNotEmpty()) {
                        renderWithWord(
                            template = pickTemplate(LexaTemplates.onFlashcardCompleteWithWord),
                            word = chosenWord
                        )
                    } else {
                        "Good review session."
                    }
                    LexaMessage(
                        text = text,
                        timestamp = System.currentTimeMillis(),
                        type = LexaMessageType.CELEBRATION
                    )
                }

                is LexaTrigger.OnProgressViewed -> {
                    val chosenWord = trigger.masteredWords.randomOrNull()?.trim().orEmpty()
                    val template = pickTemplate(LexaTemplates.onProgressViewed)
                    val text = when {
                        chosenWord.isNotEmpty() -> template
                            .replace("{title}", trigger.title)
                            .replace("{word}", chosenWord)
                        template.contains("{word}") -> "Look at what you've built."
                        else -> template.replace("{title}", trigger.title)
                    }
                    LexaMessage(
                        text = text,
                        timestamp = System.currentTimeMillis(),
                        type = LexaMessageType.INSIGHT
                    )
                }
            }
        }

        _recentMessages.value = listOf(message) + _recentMessages.value.take(MAX_RECENT_MESSAGES - 1)
        return message
    }

    private fun pickTemplate(templates: List<String>): String {
        if (templates.isEmpty()) return ""

        var index = Random.Default.nextInt(templates.size)
        if (templates.size > 1) {
            while (index == lastTemplateIndex) {
                index = Random.Default.nextInt(templates.size)
            }
        }
        lastTemplateIndex = index
        return templates[index]
    }

    private fun renderWithWord(template: String, word: String): String {
        return template.replace("{word}", word)
    }
}

data class LexaMessage(
    val text: String,
    val timestamp: Long,
    val type: LexaMessageType
)

enum class LexaMessageType {
    GREETING,
    INSIGHT,
    NUDGE,
    CELEBRATION
}

sealed class LexaTrigger {
    data class OnAppOpen(
        val streakCount: Int,
        val masteredWords: List<String>
    ) : LexaTrigger()

    data class OnQuizComplete(
        val score: Int,
        val streakCount: Int,
        val hardestWord: String
    ) : LexaTrigger()

    data class OnStreakBroken(
        val previousStreak: Int
    ) : LexaTrigger()

    data class OnFlashcardComplete(
        val wordsSeenToday: List<String>
    ) : LexaTrigger()

    data class OnProgressViewed(
        val title: String,
        val masteredWords: List<String>
    ) : LexaTrigger()
}
