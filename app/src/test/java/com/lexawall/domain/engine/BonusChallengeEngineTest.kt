package com.lexawall.domain.engine

import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BonusChallengeEngineTest {

    @Test
    fun notEligible_ifMainQuizNotCompletedToday() {
        val today = "2026-03-02"
        assertFalse(
            BonusChallengeEngine.isEligibleWithFlags(
                completedMainQuizToday = false,
                hasCompletedBonusToday = false,
                hasCompletedInLastWeek = false,
                lastBonusDate = null,
                today = today
            )
        )
    }

    @Test
    fun notEligible_ifLastBonusWasWithinSevenDays() {
        val today = "2026-03-02"
        assertFalse(
            BonusChallengeEngine.isEligibleWithFlags(
                completedMainQuizToday = true,
                hasCompletedBonusToday = false,
                hasCompletedInLastWeek = true,
                lastBonusDate = null,
                today = today
            )
        )
    }

    @Test
    fun eligibility_requiresMainQuizAndNotRateLimited() {
        val today = "2026-03-02"
        assertFalse(
            BonusChallengeEngine.isEligibleWithFlags(
                completedMainQuizToday = true,
                hasCompletedBonusToday = false,
                hasCompletedInLastWeek = false,
                lastBonusDate = today,
                today = today
            )
        )

        assertTrue(
            BonusChallengeEngine.isEligibleWithFlags(
                completedMainQuizToday = true,
                hasCompletedBonusToday = false,
                hasCompletedInLastWeek = false,
                lastBonusDate = "2026-02-20",
                today = today
            )
        )
    }

    @Test
    fun weekendDoubleXp_selectedOnSaturdayAndSunday() {
        assertEquals(
            BonusChallengeEngine.ChallengeType.WEEKEND_DOUBLE_XP,
            BonusChallengeEngine.selectRandomChallengeFor(
                dayOfWeek = DayOfWeek.SATURDAY,
                stormRoll = 0.99,
                speedRoll = 0.99
            )
        )
        assertEquals(
            BonusChallengeEngine.ChallengeType.WEEKEND_DOUBLE_XP,
            BonusChallengeEngine.selectRandomChallengeFor(
                dayOfWeek = DayOfWeek.SUNDAY,
                stormRoll = 0.99,
                speedRoll = 0.99
            )
        )
    }

    @Test
    fun randomSelection_respectsStormAndSpeedRules() {
        assertEquals(
            BonusChallengeEngine.ChallengeType.STORM,
            BonusChallengeEngine.selectRandomChallengeFor(
                dayOfWeek = DayOfWeek.MONDAY,
                stormRoll = 0.01,
                speedRoll = 0.99
            )
        )

        assertEquals(
            BonusChallengeEngine.ChallengeType.SPEED_RUN,
            BonusChallengeEngine.selectRandomChallengeFor(
                dayOfWeek = DayOfWeek.MONDAY,
                stormRoll = 0.80,
                speedRoll = 0.05
            )
        )

        assertNull(
            BonusChallengeEngine.selectRandomChallengeFor(
                dayOfWeek = DayOfWeek.WEDNESDAY,
                stormRoll = 0.8,
                speedRoll = 0.9
            )
        )
    }
}
