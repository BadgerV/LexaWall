package com.lexawall.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "badge_table")
data class BadgeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val badgeKey: String,
    val earnedAt: Long
)
