package com.lexawall.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lexawall.data.local.entities.UserProgressEntity

@Dao
interface UserProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(progress: UserProgressEntity)

    @Query("SELECT * FROM user_progress WHERE wordId = :wordId LIMIT 1")
    suspend fun getProgressByWordId(wordId: String): UserProgressEntity?

    @Query("SELECT * FROM user_progress WHERE state = :state")
    suspend fun getWordsByState(state: String): List<UserProgressEntity>

    @Update
    suspend fun updateProgress(progress: UserProgressEntity)

    @Query("SELECT * FROM user_progress")
    suspend fun getAllProgress(): List<UserProgressEntity>

    @Query("DELETE FROM user_progress")
    suspend fun clearAll()
}
