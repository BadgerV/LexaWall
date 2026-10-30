package com.lexawall.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lexawall.data.local.entities.WallpaperQuizSession

@Dao
interface WallpaperQuizDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WallpaperQuizSession)

    @Query("SELECT COUNT(*) FROM wallpaper_quiz_sessions WHERE DATE(timestamp) = DATE('now')")
    suspend fun getTodayCount(): Int

    @Query("SELECT * FROM wallpaper_quiz_sessions WHERE DATE(timestamp) = :date ORDER BY timestamp DESC")
    suspend fun getSessionsForDate(date: String): List<WallpaperQuizSession>
}
