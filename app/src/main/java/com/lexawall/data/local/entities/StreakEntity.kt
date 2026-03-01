package com.lexawall.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streak")
data class StreakEntity(
    @PrimaryKey
    val id: Int = 1,
    val currentStreak: Int,
    val lastCompletedDate: String?,
    val freezesUsedThisWeek: Int = 0,
    val weekStartDate: String?
)
