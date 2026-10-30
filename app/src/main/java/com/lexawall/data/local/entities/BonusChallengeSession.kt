package com.lexawall.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bonus_challenge_sessions",
    indices = [
        Index("timestamp"),
        Index("completed")
    ]
)
data class BonusChallengeSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: String,
    val challengeType: String,
    val completed: Boolean,
    val xpEarned: Int
)
