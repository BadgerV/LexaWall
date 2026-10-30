package com.lexawall.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "word_hunt_sessions",
    indices = [
        Index(value = ["date", "wordId"]),
        Index(value = ["date", "found"])
    ]
)
data class WordHuntSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String,
    val wordId: String,
    val found: Boolean,
    val xpEarned: Int
)
