package com.lexawall.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_sessions")
data class QuizSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val datestamp: String,
    val score: Int,
    val total: Int,
    val xpEarned: Int = 0
)
