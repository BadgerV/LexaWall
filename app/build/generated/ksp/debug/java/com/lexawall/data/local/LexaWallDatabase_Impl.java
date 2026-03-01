package com.lexawall.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.lexawall.data.local.dao.BadgeDao;
import com.lexawall.data.local.dao.BadgeDao_Impl;
import com.lexawall.data.local.dao.LevelDao;
import com.lexawall.data.local.dao.LevelDao_Impl;
import com.lexawall.data.local.dao.QuizSessionDao;
import com.lexawall.data.local.dao.QuizSessionDao_Impl;
import com.lexawall.data.local.dao.StreakDao;
import com.lexawall.data.local.dao.StreakDao_Impl;
import com.lexawall.data.local.dao.UserProgressDao;
import com.lexawall.data.local.dao.UserProgressDao_Impl;
import com.lexawall.data.local.dao.WordDao;
import com.lexawall.data.local.dao.WordDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class LexaWallDatabase_Impl extends LexaWallDatabase {
  private volatile WordDao _wordDao;

  private volatile UserProgressDao _userProgressDao;

  private volatile QuizSessionDao _quizSessionDao;

  private volatile StreakDao _streakDao;

  private volatile LevelDao _levelDao;

  private volatile BadgeDao _badgeDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `words` (`id` TEXT NOT NULL, `word` TEXT NOT NULL, `partOfSpeech` TEXT NOT NULL, `cefrLevel` TEXT NOT NULL, `definition` TEXT NOT NULL, `exampleSentence` TEXT NOT NULL, `notificationLine1` TEXT NOT NULL, `notificationLine2` TEXT NOT NULL, `pronunciationIpa` TEXT, `etymology` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_progress` (`wordId` TEXT NOT NULL, `state` TEXT NOT NULL, `intervalDays` INTEGER NOT NULL, `correctStreak` INTEGER NOT NULL, `lastAnsweredDate` TEXT, PRIMARY KEY(`wordId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `quiz_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `datestamp` TEXT NOT NULL, `score` INTEGER NOT NULL, `total` INTEGER NOT NULL, `xpEarned` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `streak` (`id` INTEGER NOT NULL, `currentStreak` INTEGER NOT NULL, `lastCompletedDate` TEXT, `freezesUsedThisWeek` INTEGER NOT NULL, `weekStartDate` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `level_table` (`id` INTEGER NOT NULL, `currentXp` INTEGER NOT NULL, `totalXpEarned` INTEGER NOT NULL, `currentLevel` INTEGER NOT NULL, `title` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `badge_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `badgeKey` TEXT NOT NULL, `earnedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '15431167038f58149e23610ef2b0733b')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `words`");
        db.execSQL("DROP TABLE IF EXISTS `user_progress`");
        db.execSQL("DROP TABLE IF EXISTS `quiz_sessions`");
        db.execSQL("DROP TABLE IF EXISTS `streak`");
        db.execSQL("DROP TABLE IF EXISTS `level_table`");
        db.execSQL("DROP TABLE IF EXISTS `badge_table`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsWords = new HashMap<String, TableInfo.Column>(10);
        _columnsWords.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("word", new TableInfo.Column("word", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("partOfSpeech", new TableInfo.Column("partOfSpeech", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("cefrLevel", new TableInfo.Column("cefrLevel", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("definition", new TableInfo.Column("definition", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("exampleSentence", new TableInfo.Column("exampleSentence", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("notificationLine1", new TableInfo.Column("notificationLine1", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("notificationLine2", new TableInfo.Column("notificationLine2", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("pronunciationIpa", new TableInfo.Column("pronunciationIpa", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("etymology", new TableInfo.Column("etymology", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWords = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWords = new TableInfo("words", _columnsWords, _foreignKeysWords, _indicesWords);
        final TableInfo _existingWords = TableInfo.read(db, "words");
        if (!_infoWords.equals(_existingWords)) {
          return new RoomOpenHelper.ValidationResult(false, "words(com.lexawall.data.local.entities.WordEntity).\n"
                  + " Expected:\n" + _infoWords + "\n"
                  + " Found:\n" + _existingWords);
        }
        final HashMap<String, TableInfo.Column> _columnsUserProgress = new HashMap<String, TableInfo.Column>(5);
        _columnsUserProgress.put("wordId", new TableInfo.Column("wordId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProgress.put("state", new TableInfo.Column("state", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProgress.put("intervalDays", new TableInfo.Column("intervalDays", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProgress.put("correctStreak", new TableInfo.Column("correctStreak", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProgress.put("lastAnsweredDate", new TableInfo.Column("lastAnsweredDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserProgress = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUserProgress = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUserProgress = new TableInfo("user_progress", _columnsUserProgress, _foreignKeysUserProgress, _indicesUserProgress);
        final TableInfo _existingUserProgress = TableInfo.read(db, "user_progress");
        if (!_infoUserProgress.equals(_existingUserProgress)) {
          return new RoomOpenHelper.ValidationResult(false, "user_progress(com.lexawall.data.local.entities.UserProgressEntity).\n"
                  + " Expected:\n" + _infoUserProgress + "\n"
                  + " Found:\n" + _existingUserProgress);
        }
        final HashMap<String, TableInfo.Column> _columnsQuizSessions = new HashMap<String, TableInfo.Column>(5);
        _columnsQuizSessions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuizSessions.put("datestamp", new TableInfo.Column("datestamp", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuizSessions.put("score", new TableInfo.Column("score", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuizSessions.put("total", new TableInfo.Column("total", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQuizSessions.put("xpEarned", new TableInfo.Column("xpEarned", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysQuizSessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesQuizSessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoQuizSessions = new TableInfo("quiz_sessions", _columnsQuizSessions, _foreignKeysQuizSessions, _indicesQuizSessions);
        final TableInfo _existingQuizSessions = TableInfo.read(db, "quiz_sessions");
        if (!_infoQuizSessions.equals(_existingQuizSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "quiz_sessions(com.lexawall.data.local.entities.QuizSessionEntity).\n"
                  + " Expected:\n" + _infoQuizSessions + "\n"
                  + " Found:\n" + _existingQuizSessions);
        }
        final HashMap<String, TableInfo.Column> _columnsStreak = new HashMap<String, TableInfo.Column>(5);
        _columnsStreak.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStreak.put("currentStreak", new TableInfo.Column("currentStreak", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStreak.put("lastCompletedDate", new TableInfo.Column("lastCompletedDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStreak.put("freezesUsedThisWeek", new TableInfo.Column("freezesUsedThisWeek", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStreak.put("weekStartDate", new TableInfo.Column("weekStartDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStreak = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesStreak = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoStreak = new TableInfo("streak", _columnsStreak, _foreignKeysStreak, _indicesStreak);
        final TableInfo _existingStreak = TableInfo.read(db, "streak");
        if (!_infoStreak.equals(_existingStreak)) {
          return new RoomOpenHelper.ValidationResult(false, "streak(com.lexawall.data.local.entities.StreakEntity).\n"
                  + " Expected:\n" + _infoStreak + "\n"
                  + " Found:\n" + _existingStreak);
        }
        final HashMap<String, TableInfo.Column> _columnsLevelTable = new HashMap<String, TableInfo.Column>(5);
        _columnsLevelTable.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLevelTable.put("currentXp", new TableInfo.Column("currentXp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLevelTable.put("totalXpEarned", new TableInfo.Column("totalXpEarned", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLevelTable.put("currentLevel", new TableInfo.Column("currentLevel", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLevelTable.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLevelTable = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLevelTable = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLevelTable = new TableInfo("level_table", _columnsLevelTable, _foreignKeysLevelTable, _indicesLevelTable);
        final TableInfo _existingLevelTable = TableInfo.read(db, "level_table");
        if (!_infoLevelTable.equals(_existingLevelTable)) {
          return new RoomOpenHelper.ValidationResult(false, "level_table(com.lexawall.data.local.entities.LevelEntity).\n"
                  + " Expected:\n" + _infoLevelTable + "\n"
                  + " Found:\n" + _existingLevelTable);
        }
        final HashMap<String, TableInfo.Column> _columnsBadgeTable = new HashMap<String, TableInfo.Column>(3);
        _columnsBadgeTable.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBadgeTable.put("badgeKey", new TableInfo.Column("badgeKey", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBadgeTable.put("earnedAt", new TableInfo.Column("earnedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBadgeTable = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBadgeTable = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBadgeTable = new TableInfo("badge_table", _columnsBadgeTable, _foreignKeysBadgeTable, _indicesBadgeTable);
        final TableInfo _existingBadgeTable = TableInfo.read(db, "badge_table");
        if (!_infoBadgeTable.equals(_existingBadgeTable)) {
          return new RoomOpenHelper.ValidationResult(false, "badge_table(com.lexawall.data.local.entities.BadgeEntity).\n"
                  + " Expected:\n" + _infoBadgeTable + "\n"
                  + " Found:\n" + _existingBadgeTable);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "15431167038f58149e23610ef2b0733b", "bdc9cd32b34f976ecf74ee6911867d58");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "words","user_progress","quiz_sessions","streak","level_table","badge_table");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `words`");
      _db.execSQL("DELETE FROM `user_progress`");
      _db.execSQL("DELETE FROM `quiz_sessions`");
      _db.execSQL("DELETE FROM `streak`");
      _db.execSQL("DELETE FROM `level_table`");
      _db.execSQL("DELETE FROM `badge_table`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(WordDao.class, WordDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UserProgressDao.class, UserProgressDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(QuizSessionDao.class, QuizSessionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(StreakDao.class, StreakDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(LevelDao.class, LevelDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(BadgeDao.class, BadgeDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public WordDao wordDao() {
    if (_wordDao != null) {
      return _wordDao;
    } else {
      synchronized(this) {
        if(_wordDao == null) {
          _wordDao = new WordDao_Impl(this);
        }
        return _wordDao;
      }
    }
  }

  @Override
  public UserProgressDao userProgressDao() {
    if (_userProgressDao != null) {
      return _userProgressDao;
    } else {
      synchronized(this) {
        if(_userProgressDao == null) {
          _userProgressDao = new UserProgressDao_Impl(this);
        }
        return _userProgressDao;
      }
    }
  }

  @Override
  public QuizSessionDao quizSessionDao() {
    if (_quizSessionDao != null) {
      return _quizSessionDao;
    } else {
      synchronized(this) {
        if(_quizSessionDao == null) {
          _quizSessionDao = new QuizSessionDao_Impl(this);
        }
        return _quizSessionDao;
      }
    }
  }

  @Override
  public StreakDao streakDao() {
    if (_streakDao != null) {
      return _streakDao;
    } else {
      synchronized(this) {
        if(_streakDao == null) {
          _streakDao = new StreakDao_Impl(this);
        }
        return _streakDao;
      }
    }
  }

  @Override
  public LevelDao levelDao() {
    if (_levelDao != null) {
      return _levelDao;
    } else {
      synchronized(this) {
        if(_levelDao == null) {
          _levelDao = new LevelDao_Impl(this);
        }
        return _levelDao;
      }
    }
  }

  @Override
  public BadgeDao badgeDao() {
    if (_badgeDao != null) {
      return _badgeDao;
    } else {
      synchronized(this) {
        if(_badgeDao == null) {
          _badgeDao = new BadgeDao_Impl(this);
        }
        return _badgeDao;
      }
    }
  }
}
