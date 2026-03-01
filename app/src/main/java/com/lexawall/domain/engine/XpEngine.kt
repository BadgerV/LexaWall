package com.lexawall.domain.engine

object XpEngine {
    data class LevelDefinition(
        val level: Int,
        val title: String,
        val xpRequired: Int,
        val unlocksTheme: String?,
        val unlocksWordTier: String?
    )

    val LEVELS: List<LevelDefinition> = listOf(
        LevelDefinition(1, "The Curious", 0, null, null),
        LevelDefinition(2, "The Apprentice", 250, "arctic", null),
        LevelDefinition(3, "The Reader", 600, null, "b2_standard"),
        LevelDefinition(4, "The Thinker", 1200, "forest", null),
        LevelDefinition(5, "The Orator", 2200, null, "b2_deep"),
        LevelDefinition(6, "The Scholar", 3800, "savanna", "c1_standard"),
        LevelDefinition(7, "The Intellectual", 6000, null, "c1_deep"),
        LevelDefinition(8, "The Wordsmith", 9000, "all", null),
        LevelDefinition(9, "The Rhetorician", 13000, null, null),
        LevelDefinition(10, "The Lexicon", 18000, null, null)
    )

    fun getLevelForXp(xp: Int): LevelDefinition {
        return LEVELS.lastOrNull { xp >= it.xpRequired } ?: LEVELS.first()
    }

    fun getNextLevel(xp: Int): LevelDefinition? {
        val current = getLevelForXp(xp)
        return LEVELS.firstOrNull { it.level > current.level }
    }

    fun getXpToNextLevel(xp: Int): Int {
        val next = getNextLevel(xp) ?: return 0
        return (next.xpRequired - xp).coerceAtLeast(0)
    }

    fun getProgressFraction(xp: Int): Float {
        val current = getLevelForXp(xp)
        val next = getNextLevel(xp) ?: return 1f
        val span = (next.xpRequired - current.xpRequired).coerceAtLeast(1)
        val intoLevel = (xp - current.xpRequired).coerceAtLeast(0)
        return (intoLevel.toFloat() / span.toFloat()).coerceIn(0f, 1f)
    }

    fun calculateQuizXp(score: Int, totalQuestions: Int): Int {
        val perfectBonus = if (totalQuestions > 0 && score == totalQuestions) 50 else 0
        return 25 + perfectBonus
    }

    fun calculateMasteryXp(): Int = 100

    fun calculateStreakMilestoneXp(): Int = 75

    fun getAllUnlockedThemes(xp: Int): List<String> {
        val currentLevel = getLevelForXp(xp).level
        val unlockMap = listOf(
            "midnight" to 1,
            "arctic" to 2,
            "crimson" to 3,
            "forest" to 4,
            "jade" to 5,
            "dusk" to 7,
            "savanna" to 6,
            "obsidian" to 6,
            "storm" to 7,
            "rust" to 8,
            "ember" to 9,
            "void" to 10
        )
        return unlockMap
            .filter { (_, level) -> currentLevel >= level }
            .map { (key, _) -> key }
    }
}

object XpBadges {
    const val FIRST_WORD = "first_word"
    const val STREAK_7 = "streak_7"
    const val STREAK_30 = "streak_30"
    const val WORDS_10 = "words_10"
    const val WORDS_50 = "words_50"
    const val WORDS_100 = "words_100"
    const val PERFECT_QUIZ = "perfect_quiz"
    const val LEVEL_5 = "level_5"
    const val LEVEL_10 = "level_10"
}
