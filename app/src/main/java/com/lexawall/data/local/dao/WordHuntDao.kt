package com.lexawall.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lexawall.data.local.entities.WordHuntSession

@Dao
interface WordHuntDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WordHuntSession)

    @Query("SELECT * FROM word_hunt_sessions WHERE date = :date AND wordId = :wordId LIMIT 1")
    suspend fun getSessionForWordAndDate(wordId: String, date: String): WordHuntSession?

    @Query("SELECT * FROM word_hunt_sessions WHERE DATE(date) = DATE('now') AND found = 1")
    suspend fun getTodayFoundWords(): List<WordHuntSession>
}
