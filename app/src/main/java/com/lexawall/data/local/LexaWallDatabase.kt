package com.lexawall.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lexawall.data.local.dao.BadgeDao
import com.lexawall.data.local.dao.LevelDao
import com.lexawall.data.local.dao.QuizSessionDao
import com.lexawall.data.local.dao.StreakDao
import com.lexawall.data.local.dao.UserProgressDao
import com.lexawall.data.local.dao.WordDao
import com.lexawall.data.local.entities.BadgeEntity
import com.lexawall.data.local.entities.LevelEntity
import com.lexawall.data.local.entities.QuizSessionEntity
import com.lexawall.data.local.entities.StreakEntity
import com.lexawall.data.local.entities.UserProgressEntity
import com.lexawall.data.local.entities.WordEntity

@Database(
    entities = [
        WordEntity::class,
        UserProgressEntity::class,
        QuizSessionEntity::class,
        StreakEntity::class,
        LevelEntity::class,
        BadgeEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class LexaWallDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun quizSessionDao(): QuizSessionDao
    abstract fun streakDao(): StreakDao
    abstract fun levelDao(): LevelDao
    abstract fun badgeDao(): BadgeDao
}
