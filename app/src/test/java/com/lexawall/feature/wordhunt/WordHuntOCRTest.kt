package com.lexawall.feature.wordhunt

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordHuntOCRTest {

    @Test
    fun normalizeForMatching_stripsPunctuationAndNormalizesSpaces() {
        val normalized = WordHuntTextMatcher.normalizeForMatching("  Ephemeral,\n  things!!  ")
        assertEquals("ephemeral things", normalized)
    }

    @Test
    fun containsTargetWord_matchesCaseInsensitively() {
        assertTrue(
            WordHuntTextMatcher.containsTargetWord(
                detectedText = "This feeling is EPHEMERAL and brief",
                targetWord = "ephemeral"
            )
        )
    }

    @Test
    fun containsTargetWord_rejectsMissingWord() {
        assertFalse(
            WordHuntTextMatcher.containsTargetWord(
                detectedText = "Language learning is fun",
                targetWord = "serendipity"
            )
        )
    }

    @Test
    fun duplicateHuntOnSameDay_doesNotAwardXp() {
        assertFalse(WordHuntRewardPolicy.shouldAwardXp(alreadyFoundToday = true))
        assertTrue(WordHuntRewardPolicy.shouldAwardXp(alreadyFoundToday = false))
    }
}
