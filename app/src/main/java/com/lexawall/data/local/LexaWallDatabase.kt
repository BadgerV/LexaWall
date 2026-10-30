package com.lexawall.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lexawall.data.local.dao.BadgeDao
import com.lexawall.data.local.dao.BonusChallengeDao
import com.lexawall.data.local.dao.LevelDao
import com.lexawall.data.local.dao.QuizSessionDao
import com.lexawall.data.local.dao.StreakDao
import com.lexawall.data.local.dao.UserProgressDao
import com.lexawall.data.local.dao.WallpaperQuizDao
import com.lexawall.data.local.dao.WordHuntDao
import com.lexawall.data.local.dao.WordDao
import com.lexawall.data.local.entities.BadgeEntity
import com.lexawall.data.local.entities.BonusChallengeSession
import com.lexawall.data.local.entities.LevelEntity
import com.lexawall.data.local.entities.QuizSessionEntity
import com.lexawall.data.local.entities.StreakEntity
import com.lexawall.data.local.entities.UserProgressEntity
import com.lexawall.data.local.entities.WallpaperQuizSession
import com.lexawall.data.local.entities.WordHuntSession
import com.lexawall.data.local.entities.WordEntity

@Database(
    entities = [
        WordEntity::class,
        UserProgressEntity::class,
        QuizSessionEntity::class,
        StreakEntity::class,
        LevelEntity::class,
        BadgeEntity::class,
        WallpaperQuizSession::class,
        BonusChallengeSession::class,
        WordHuntSession::class
    ],
    version = 5,
    exportSchema = false
)
abstract class LexaWallDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun quizSessionDao(): QuizSessionDao
    abstract fun streakDao(): StreakDao
    abstract fun levelDao(): LevelDao
    abstract fun badgeDao(): BadgeDao
    abstract fun wallpaperQuizDao(): WallpaperQuizDao
    abstract fun bonusChallengeDao(): BonusChallengeDao
    abstract fun wordHuntDao(): WordHuntDao
}
