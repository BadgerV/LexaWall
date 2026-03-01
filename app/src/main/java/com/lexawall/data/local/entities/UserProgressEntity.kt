package com.lexawall.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey
    val wordId: String,
    val state: String,
    val intervalDays: Int = 1,
    val correctStreak: Int = 0,
    val lastAnsweredDate: String?
)
