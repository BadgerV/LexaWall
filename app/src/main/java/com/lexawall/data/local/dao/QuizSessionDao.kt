package com.lexawall.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lexawall.data.local.entities.QuizSessionEntity

@Dao
interface QuizSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: QuizSessionEntity): Long

    @Query("SELECT * FROM quiz_sessions ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentSessions(limit: Int): List<QuizSessionEntity>

    @Query("SELECT * FROM quiz_sessions ORDER BY id DESC")
    suspend fun getAllSessions(): List<QuizSessionEntity>

    @Query("DELETE FROM quiz_sessions")
    suspend fun clearAll()
}
