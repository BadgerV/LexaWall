package com.lexawall.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.lexawall.data.local.entities.StreakEntity

@Dao
interface StreakDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(streak: StreakEntity)

    @Update
    suspend fun updateStreak(streak: StreakEntity)

    @Query("SELECT * FROM streak LIMIT 1")
    suspend fun getStreakOrNull(): StreakEntity?

    @Transaction
    suspend fun getStreak(): StreakEntity {
        val existing = getStreakOrNull()
        if (existing != null) return existing
        val default = StreakEntity(
            id = 1,
            currentStreak = 0,
            lastCompletedDate = null,
            freezesUsedThisWeek = 0,
            weekStartDate = null
        )
        insertOrReplace(default)
        return default
    }
}
