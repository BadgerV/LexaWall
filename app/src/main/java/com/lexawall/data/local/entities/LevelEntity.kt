package com.lexawall.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_table")
data class LevelEntity(
    @PrimaryKey
    val id: Int = 1,
    val currentXp: Int = 0,
    val totalXpEarned: Int = 0,
    val currentLevel: Int = 1,
    val title: String = "The Curious"
)
