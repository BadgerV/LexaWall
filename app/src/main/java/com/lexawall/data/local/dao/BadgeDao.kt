package com.lexawall.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lexawall.data.local.entities.BadgeEntity

@Dao
interface BadgeDao {
    @Query("SELECT * FROM badge_table ORDER BY earnedAt DESC")
    suspend fun getAllBadges(): List<BadgeEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBadge(badge: BadgeEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM badge_table WHERE badgeKey = :key LIMIT 1)")
    suspend fun hasBadge(key: String): Boolean
}
