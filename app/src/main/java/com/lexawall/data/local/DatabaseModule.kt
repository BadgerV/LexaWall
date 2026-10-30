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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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
            db.execSQL(
                """
                ALTER TABLE words
                ADD COLUMN lexaSays TEXT
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS wallpaper_quiz_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    timestamp TEXT NOT NULL,
                    wordId TEXT NOT NULL,
                    correct INTEGER NOT NULL,
                    xpEarned INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_wallpaper_quiz_sessions_timestamp
                ON wallpaper_quiz_sessions (timestamp)
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_wallpaper_quiz_sessions_wordId
                ON wallpaper_quiz_sessions (wordId)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS bonus_challenge_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    timestamp TEXT NOT NULL,
                    challengeType TEXT NOT NULL,
                    completed INTEGER NOT NULL,
                    xpEarned INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_bonus_challenge_sessions_timestamp
                ON bonus_challenge_sessions (timestamp)
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_bonus_challenge_sessions_completed
                ON bonus_challenge_sessions (completed)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS word_hunt_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    date TEXT NOT NULL,
                    wordId TEXT NOT NULL,
                    found INTEGER NOT NULL,
                    xpEarned INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_word_hunt_sessions_date_wordId
                ON word_hunt_sessions (date, wordId)
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_word_hunt_sessions_date_found
                ON word_hunt_sessions (date, found)
                """.trimIndent()
            )
        }
    }

    // v4 aligns schema versioning for existing installs that already advanced to 4.
    // No structural changes are required beyond confirming the current schema.
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS wallpaper_quiz_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    timestamp TEXT NOT NULL,
                    wordId TEXT NOT NULL,
                    correct INTEGER NOT NULL,
                    xpEarned INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS bonus_challenge_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    timestamp TEXT NOT NULL,
                    challengeType TEXT NOT NULL,
                    completed INTEGER NOT NULL,
                    xpEarned INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS word_hunt_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    date TEXT NOT NULL,
                    wordId TEXT NOT NULL,
                    found INTEGER NOT NULL,
                    xpEarned INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_wallpaper_quiz_sessions_timestamp
                ON wallpaper_quiz_sessions (timestamp)
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_wallpaper_quiz_sessions_wordId
                ON wallpaper_quiz_sessions (wordId)
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_bonus_challenge_sessions_timestamp
                ON bonus_challenge_sessions (timestamp)
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_bonus_challenge_sessions_completed
                ON bonus_challenge_sessions (completed)
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_word_hunt_sessions_date_wordId
                ON word_hunt_sessions (date, wordId)
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_word_hunt_sessions_date_found
                ON word_hunt_sessions (date, found)
                """.trimIndent()
            )
        }
    }

    // v5 reconciles installs that reached v4 with stale or partial schema for new feature tables.
    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS wallpaper_quiz_sessions")
            db.execSQL("DROP TABLE IF EXISTS bonus_challenge_sessions")
            db.execSQL("DROP TABLE IF EXISTS word_hunt_sessions")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS wallpaper_quiz_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    timestamp TEXT NOT NULL,
                    wordId TEXT NOT NULL,
                    correct INTEGER NOT NULL,
                    xpEarned INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_wallpaper_quiz_sessions_timestamp
                ON wallpaper_quiz_sessions (timestamp)
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_wallpaper_quiz_sessions_wordId
                ON wallpaper_quiz_sessions (wordId)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS bonus_challenge_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    timestamp TEXT NOT NULL,
                    challengeType TEXT NOT NULL,
                    completed INTEGER NOT NULL,
                    xpEarned INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_bonus_challenge_sessions_timestamp
                ON bonus_challenge_sessions (timestamp)
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_bonus_challenge_sessions_completed
                ON bonus_challenge_sessions (completed)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS word_hunt_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    date TEXT NOT NULL,
                    wordId TEXT NOT NULL,
                    found INTEGER NOT NULL,
                    xpEarned INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_word_hunt_sessions_date_wordId
                ON word_hunt_sessions (date, wordId)
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_word_hunt_sessions_date_found
                ON word_hunt_sessions (date, found)
                """.trimIndent()
            )
        }
    }
}
