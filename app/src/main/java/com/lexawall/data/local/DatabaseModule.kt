package com.lexawall.data.local

import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Room
import com.lexawall.data.local.repository.RoomQuizRepository
import com.lexawall.data.local.repository.RoomUserRepository
import com.lexawall.data.local.repository.RoomWordRepository
import com.lexawall.domain.repository.QuizRepository
import com.lexawall.domain.repository.UserRepository
import com.lexawall.domain.repository.WordRepository

object DatabaseModule {
    @Volatile
    private var instance: LexaWallDatabase? = null

    fun getInstance(context: Context): LexaWallDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                LexaWallDatabase::class.java,
                "lexawall.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                .also { db ->
                instance = db
            }
        }
    }

    fun provideWordRepository(context: Context): WordRepository {
        val db = getInstance(context)
        return RoomWordRepository(
            context = context.applicationContext,
            wordDao = db.wordDao(),
            userProgressDao = db.userProgressDao()
        )
    }

    fun provideQuizRepository(context: Context): QuizRepository {
        val db = getInstance(context)
        return RoomQuizRepository(
            context = context.applicationContext,
            wordDao = db.wordDao(),
            userProgressDao = db.userProgressDao()
        )
    }

    fun provideUserRepository(context: Context): UserRepository {
        val db = getInstance(context)
        return RoomUserRepository(
            streakDao = db.streakDao(),
            quizSessionDao = db.quizSessionDao(),
            userProgressDao = db.userProgressDao()
        )
    }

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DELETE FROM words")
            db.execSQL("DELETE FROM user_progress")
            db.execSQL("DELETE FROM quiz_sessions")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS level_table (
                    id INTEGER NOT NULL,
                    currentXp INTEGER NOT NULL,
                    totalXpEarned INTEGER NOT NULL,
                    currentLevel INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT OR IGNORE INTO level_table (id, currentXp, totalXpEarned, currentLevel, title)
                VALUES (1, 0, 0, 1, 'The Curious')
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS badge_table (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    badgeKey TEXT NOT NULL,
                    earnedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                ALTER TABLE quiz_sessions
                ADD COLUMN xpEarned INTEGER NOT NULL DEFAULT 0
                """.trimIndent()
            )
        }
    }
}
