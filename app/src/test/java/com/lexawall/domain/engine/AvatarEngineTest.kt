package com.lexawall.domain.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AvatarEngineTest {

    @Test
    fun baseOutfit_isAlwaysUnlocked() {
        val unlocked = AvatarEngine.getUnlockedOutfits(streakDays = 0, xp = 0, level = 1)
        assertTrue(unlocked.any { it.id == "base" })
    }

    @Test
    fun scholarOutfit_lockedAtSixDayStreak_unlockedAtSeven() {
        val atSixDays = AvatarEngine.getUnlockedOutfits(streakDays = 6, xp = 0, level = 1)
        assertFalse(atSixDays.any { it.id == "scholar" })

        val atSevenDays = AvatarEngine.getUnlockedOutfits(streakDays = 7, xp = 0, level = 1)
        assertTrue(atSevenDays.any { it.id == "scholar" })
    }

    @Test
    fun astronautOutfit_lockedAt4999Xp_unlockedAt5000() {
        val belowThreshold = AvatarEngine.getUnlockedOutfits(streakDays = 0, xp = 4999, level = 1)
        assertFalse(belowThreshold.any { it.id == "astronaut" })

        val atThreshold = AvatarEngine.getUnlockedOutfits(streakDays = 0, xp = 5000, level = 1)
        assertTrue(atThreshold.any { it.id == "astronaut" })
    }
}
