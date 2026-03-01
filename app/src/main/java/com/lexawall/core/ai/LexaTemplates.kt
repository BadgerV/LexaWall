package com.lexawall.core.ai

object LexaTemplates {
    val onAppOpenWithWord = listOf(
        "You carry {word} now.",
        "Someone used {word} yesterday. You already knew it.",
        "The kind of person who knows {word} reads carefully.",
        "There are people who will never know {word}. You do."
    )

    val onQuizCompleteWithWord = listOf(
        "Four from five. {word} was always going to stick.",
        "You answered {word} correctly. That one matters.",
        "Not everyone gets {word} right the first time."
    )

    val onStreakBroken = listOf(
        "That was a good run.",
        "Streaks end. Words stay.",
        "Start again. You know more than you did before."
    )

    val onFlashcardCompleteWithWord = listOf(
        "You saw {word} twice today.",
        "{word} is settling in."
    )

    val onProgressViewed = listOf(
        "The {title} knows {word} now.",
        "{word}. That one was not easy.",
        "Someone once struggled with {word}. Not you.",
        "Look at what you've built."
    )
}
