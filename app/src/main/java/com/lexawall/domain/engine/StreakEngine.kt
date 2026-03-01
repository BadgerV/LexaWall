package com.lexawall.domain.engine

import com.lexawall.data.local.entities.StreakEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class StreakEngine {
    data class CompletionResult(
        val streak: StreakEntity,
        val streakMilestoneReached: Boolean
    )

    fun recordCompletion(streak: StreakEntity, todayDate: String): CompletionResult {
        val zone = ZoneId.systemDefault()
        val today = parseDateString(todayDate, zone) ?: LocalDate.now(zone)
        val yesterday = today.minusDays(1)
        val lastCompleted = parseDateString(streak.lastCompletedDate, zone)
        val todayString = today.toString()

        if (lastCompleted == today) {
            return CompletionResult(
                streak = streak,
                streakMilestoneReached = streak.currentStreak > 0 && streak.currentStreak % 7 == 0
            )
        }

        // First ever completion — start the streak at 1
        if (lastCompleted == null) {
            val updated = streak.copy(
                currentStreak = 1,
                lastCompletedDate = todayString
            )
            return CompletionResult(
                streak = updated,
                streakMilestoneReached = false
            )
        }

        if (lastCompleted == yesterday) {
            val updated = streak.copy(
                currentStreak = streak.currentStreak + 1,
                lastCompletedDate = todayString
            )
            return CompletionResult(
                streak = updated,
                streakMilestoneReached = updated.currentStreak > 0 && updated.currentStreak % 7 == 0
            )
        }

        val canUseFreeze = streak.freezesUsedThisWeek < MAX_FREEZES_PER_WEEK
        val updated = if (canUseFreeze) {
            streak.copy(
                freezesUsedThisWeek = streak.freezesUsedThisWeek + 1,
                lastCompletedDate = todayString
            )
        } else {
            streak.copy(
                currentStreak = 1,
                lastCompletedDate = todayString
            )
        }
        return CompletionResult(
            streak = updated,
            streakMilestoneReached = updated.currentStreak > 0 && updated.currentStreak % 7 == 0
        )
    }

    fun isStreakBroken(streak: StreakEntity, todayDate: String): Boolean {
        val zone = ZoneId.systemDefault()
        val today = parseDateString(todayDate, zone) ?: LocalDate.now(zone)
        val yesterday = today.minusDays(1)
        val last = parseDateString(streak.lastCompletedDate, zone)

        return streak.currentStreak > 0 && last != today && last != yesterday
    }

    fun canRedeem(streak: StreakEntity, todayDate: String): Boolean {
        return isStreakBroken(streak, todayDate) && streak.freezesUsedThisWeek < MAX_FREEZES_PER_WEEK
    }

    companion object {
        private const val MAX_FREEZES_PER_WEEK = 1
    }

    private fun parseDateString(value: String?, zone: ZoneId): LocalDate? {
        if (value.isNullOrBlank()) return null
        return runCatching { LocalDate.parse(value) }.getOrElse {
            runCatching {
                Instant.ofEpochMilli(value.toLong()).atZone(zone).toLocalDate()
            }.getOrNull()
        }
    }
}
