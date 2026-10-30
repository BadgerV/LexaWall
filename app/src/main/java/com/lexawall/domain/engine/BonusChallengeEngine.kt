package com.lexawall.domain.engine

import android.content.Context
import com.lexawall.data.local.DatabaseModule
import com.lexawall.data.local.PrefsManager
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.random.Random

class BonusChallengeEngine(
    context: Context,
    private val random: Random = Random.Default,
    private val todayProvider: () -> LocalDate = { LocalDate.now() }
) {
    private val appContext = context.applicationContext
    private val db by lazy { DatabaseModule.getInstance(appContext) }

    suspend fun isEligibleForBonus(): Boolean {
        val today = todayProvider().toString()
        val completedMainQuizToday = db.quizSessionDao().getSessionsForDate(today).isNotEmpty()
        val hasCompletedBonusToday = db.bonusChallengeDao().getTodayCompletedCount() > 0
        val lastCompletedInWeek = db.bonusChallengeDao().getLastCompletedInWeek()
        val lastDate = PrefsManager.getLastBonusChallengeCompletedDate(appContext)
        return isEligibleWithFlags(
            completedMainQuizToday = completedMainQuizToday,
            hasCompletedBonusToday = hasCompletedBonusToday,
            hasCompletedInLastWeek = lastCompletedInWeek != null && lastCompletedInWeek.completed,
            lastBonusDate = lastDate,
            today = today
        )
    }

    fun selectRandomChallenge(): ChallengeType? {
        val dayOfWeek = todayProvider().dayOfWeek
        return selectRandomChallengeFor(
            dayOfWeek = dayOfWeek,
            stormRoll = random.nextDouble(),
            speedRoll = random.nextDouble()
        )
    }

    suspend fun resolveEligibleChallenge(): ChallengeType? {
        if (!isEligibleForBonus()) return null
        return selectRandomChallenge()
    }

    enum class ChallengeType(
        val title: String,
        val rewardXp: Int,
        val subtitle: String
    ) {
        WEEKEND_DOUBLE_XP(
            title = "⚡ WEEKEND DOUBLE XP",
            rewardXp = 25,
            subtitle = "+25 XP"
        ),
        STORM(
            title = "🌩️ STORM CHALLENGE",
            rewardXp = 30,
            subtitle = "+30 XP + Storm Badge"
        ),
        SPEED_RUN(
            title = "⏱️ SPEED RUN",
            rewardXp = 25,
            subtitle = "Answer in 5 seconds for +25 XP"
        );

        companion object {
            fun fromName(raw: String?): ChallengeType? {
                if (raw.isNullOrBlank()) return null
                return entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
            }
        }
    }

    companion object {
        internal fun isEligibleWithFlags(
            completedMainQuizToday: Boolean,
            hasCompletedBonusToday: Boolean,
            hasCompletedInLastWeek: Boolean,
            lastBonusDate: String?,
            today: String
        ): Boolean {
            if (!completedMainQuizToday) return false
            if (hasCompletedBonusToday) return false
            if (hasCompletedInLastWeek) return false
            if (!lastBonusDate.isNullOrBlank() && lastBonusDate == today) return false
            return true
        }

        internal fun selectRandomChallengeFor(
            dayOfWeek: DayOfWeek,
            stormRoll: Double,
            speedRoll: Double
        ): ChallengeType? {
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                return ChallengeType.WEEKEND_DOUBLE_XP
            }
            return when {
                stormRoll < 0.05 -> ChallengeType.STORM
                speedRoll < 0.10 -> ChallengeType.SPEED_RUN
                else -> null
            }
        }
    }
}
