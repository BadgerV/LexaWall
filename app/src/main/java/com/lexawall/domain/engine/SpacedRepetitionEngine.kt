package com.lexawall.domain.engine

import com.lexawall.data.local.entities.UserProgressEntity
import kotlin.random.Random

class SpacedRepetitionEngine {
    fun advanceWord(progress: UserProgressEntity, cefrLevel: String? = null): UserProgressEntity {
        val newCorrectStreak = progress.correctStreak + 1
        val graduationThreshold = getGraduationThreshold(cefrLevel.orEmpty())
        val newState = if (newCorrectStreak >= graduationThreshold) STATE_MASTERED else progress.state
        val newIntervalDays = when (progress.intervalDays) {
            1 -> 2
            2 -> 3
            else -> progress.intervalDays
        }

        return progress.copy(
            correctStreak = newCorrectStreak,
            intervalDays = newIntervalDays,
            state = newState
        )
    }

    fun resetWord(progress: UserProgressEntity): UserProgressEntity {
        return progress.copy(
            correctStreak = 0,
            intervalDays = 1,
            state = STATE_TESTED
        )
    }

    fun advanceToSeen(progress: UserProgressEntity): UserProgressEntity {
        return if (progress.state == STATE_NEW) {
            progress.copy(state = STATE_SEEN)
        } else {
            progress
        }
    }

    fun getGraduationThreshold(cefrLevel: String): Int {
        return when (cefrLevel.uppercase()) {
            "A1", "A2" -> 2
            else -> 3
        }
    }

    fun isGraduated(progress: UserProgressEntity): Boolean {
        return progress.state == STATE_MASTERED
    }

    fun buildQuizPool(allProgress: List<UserProgressEntity>): List<UserProgressEntity> {
        return allProgress
            .asSequence()
            .filter { it.state == STATE_NEW || it.state == STATE_SEEN || it.state == STATE_TESTED }
            .sortedBy { it.intervalDays }
            .chunked(5)
            .flatMap { it.shuffled(Random.Default) }
            .take(MAX_QUIZ_POOL_SIZE)
            .toList()
    }

    companion object {
        private const val STATE_NEW = "NEW"
        private const val STATE_SEEN = "SEEN"
        private const val STATE_TESTED = "TESTED"
        private const val STATE_MASTERED = "MASTERED"
        private const val MAX_QUIZ_POOL_SIZE = 15
    }
}
