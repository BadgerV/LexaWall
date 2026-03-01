package com.lexawall.data.local.repository

import com.lexawall.data.local.dao.QuizSessionDao
import com.lexawall.data.local.dao.StreakDao
import com.lexawall.data.local.dao.UserProgressDao
import com.lexawall.data.local.entities.StreakEntity
import com.lexawall.domain.model.UserStats
import com.lexawall.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Calendar

class RoomUserRepository(
    private val streakDao: StreakDao,
    private val quizSessionDao: QuizSessionDao,
    private val userProgressDao: UserProgressDao
) : UserRepository {
    fun getStreakEntity(): StreakEntity? = runBlocking {
        withContext(Dispatchers.IO) {
            streakDao.getStreak()
        }
    }

    override fun getUserStats(): UserStats = runBlocking {
        withContext(Dispatchers.IO) {
            val streak = streakDao.getStreak()
            val recent = quizSessionDao.getRecentSessions(1).firstOrNull()
            val mastered = userProgressDao.getWordsByState("MASTERED")

            UserStats(
                streakDays = streak?.currentStreak ?: 0,
                wordsMastered = mastered.size,
                quizScore = recent?.score ?: 0,
                quizTotal = recent?.total ?: 0
            )
        }
    }

    override fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "GOOD MORNING"
            hour < 17 -> "GOOD AFTERNOON"
            else -> "GOOD EVENING"
        }
    }
}
