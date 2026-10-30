package com.lexawall.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lexawall.data.local.entities.BonusChallengeSession

@Dao
interface BonusChallengeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: BonusChallengeSession)

    @Query("SELECT * FROM bonus_challenge_sessions WHERE DATE(timestamp) >= DATE('now', '-7 days') AND completed = 1 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastCompletedInWeek(): BonusChallengeSession?

    @Query("SELECT COUNT(*) FROM bonus_challenge_sessions WHERE DATE(timestamp) = DATE('now') AND completed = 1")
    suspend fun getTodayCompletedCount(): Int
}
