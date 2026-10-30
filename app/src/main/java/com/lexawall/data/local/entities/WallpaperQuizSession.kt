package com.lexawall.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wallpaper_quiz_sessions",
    indices = [
        Index("timestamp"),
        Index("wordId")
    ]
)
data class WallpaperQuizSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: String,
    val wordId: String,
    val correct: Boolean,
    val xpEarned: Int
)
