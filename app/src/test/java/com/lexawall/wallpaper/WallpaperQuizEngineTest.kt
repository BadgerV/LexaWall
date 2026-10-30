package com.lexawall.wallpaper

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WallpaperQuizEngineTest {

    @Test
    fun quizNotTriggered_ifQuizzesTodayAtLeastTwo() {
        assertFalse(WallpaperQuizEngine.canTriggerWithDailyLimit(todayCount = 2))
        assertFalse(WallpaperQuizEngine.canTriggerWithDailyLimit(todayCount = 3))
        assertTrue(WallpaperQuizEngine.canTriggerWithDailyLimit(todayCount = 1))
    }

    @Test
    fun quizNotTriggered_ifLastQuizWithinFiveHours() {
        val now = 30_000_000L
        val justInside = now - (5 * 60 * 60 * 1000L) + 1L
        val boundary = now - (5 * 60 * 60 * 1000L)

        assertTrue(WallpaperQuizEngine.isWithinQuizCooldown(now, justInside))
        assertFalse(WallpaperQuizEngine.isWithinQuizCooldown(now, boundary))
    }

    @Test
    fun dailyCountResetsAtMidnight() {
        assertTrue(
            WallpaperQuizEngine.resolveTodayCount(
                storedDate = "2026-03-02",
                today = "2026-03-02",
                storedCount = 2
            ) == 2
        )
        assertTrue(
            WallpaperQuizEngine.resolveTodayCount(
                storedDate = "2026-03-01",
                today = "2026-03-02",
                storedCount = 2
            ) == 0
        )
    }
}
