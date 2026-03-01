package com.lexawall.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.lexawall.data.local.entities.UserProgressEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UserProgressDao_Impl implements UserProgressDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UserProgressEntity> __insertionAdapterOfUserProgressEntity;

  private final EntityDeletionOrUpdateAdapter<UserProgressEntity> __updateAdapterOfUserProgressEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public UserProgressDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUserProgressEntity = new EntityInsertionAdapter<UserProgressEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `user_progress` (`wordId`,`state`,`intervalDays`,`correctStreak`,`lastAnsweredDate`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserProgressEntity entity) {
        statement.bindString(1, entity.getWordId());
        statement.bindString(2, entity.getState());
        statement.bindLong(3, entity.getIntervalDays());
        statement.bindLong(4, entity.getCorrectStreak());
        if (entity.getLastAnsweredDate() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLastAnsweredDate());
        }
      }
    };
    this.__updateAdapterOfUserProgressEntity = new EntityDeletionOrUpdateAdapter<UserProgressEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `user_progress` SET `wordId` = ?,`state` = ?,`intervalDays` = ?,`correctStreak` = ?,`lastAnsweredDate` = ? WHERE `wordId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserProgressEntity entity) {
        statement.bindString(1, entity.getWordId());
        statement.bindString(2, entity.getState());
        statement.bindLong(3, entity.getIntervalDays());
        statement.bindLong(4, entity.getCorrectStreak());
        if (entity.getLastAnsweredDate() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLastAnsweredDate());
        }
        statement.bindString(6, entity.getWordId());
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM user_progress";
        return _query;
      }
    };
  }

  @Override
  public Object insertOrReplace(final UserProgressEntity progress,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUserProgressEntity.insert(progress);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateProgress(final UserProgressEntity progress,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfUserProgressEntity.handle(progress);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getProgressByWordId(final String wordId,
      final Continuation<? super UserProgressEntity> $completion) {
    final String _sql = "SELECT * FROM user_progress WHERE wordId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, wordId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<UserProgressEntity>() {
      @Override
      @Nullable
      public UserProgressEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfWordId = CursorUtil.getColumnIndexOrThrow(_cursor, "wordId");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfIntervalDays = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalDays");
          final int _cursorIndexOfCorrectStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "correctStreak");
          final int _cursorIndexOfLastAnsweredDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastAnsweredDate");
          final UserProgressEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpWordId;
            _tmpWordId = _cursor.getString(_cursorIndexOfWordId);
            final String _tmpState;
            _tmpState = _cursor.getString(_cursorIndexOfState);
            final int _tmpIntervalDays;
            _tmpIntervalDays = _cursor.getInt(_cursorIndexOfIntervalDays);
            final int _tmpCorrectStreak;
            _tmpCorrectStreak = _cursor.getInt(_cursorIndexOfCorrectStreak);
            final String _tmpLastAnsweredDate;
            if (_cursor.isNull(_cursorIndexOfLastAnsweredDate)) {
              _tmpLastAnsweredDate = null;
            } else {
              _tmpLastAnsweredDate = _cursor.getString(_cursorIndexOfLastAnsweredDate);
            }
            _result = new UserProgressEntity(_tmpWordId,_tmpState,_tmpIntervalDays,_tmpCorrectStreak,_tmpLastAnsweredDate);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getWordsByState(final String state,
      final Continuation<? super List<UserProgressEntity>> $completion) {
    final String _sql = "SELECT * FROM user_progress WHERE state = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, state);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<UserProgressEntity>>() {
      @Override
      @NonNull
      public List<UserProgressEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfWordId = CursorUtil.getColumnIndexOrThrow(_cursor, "wordId");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfIntervalDays = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalDays");
          final int _cursorIndexOfCorrectStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "correctStreak");
          final int _cursorIndexOfLastAnsweredDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastAnsweredDate");
          final List<UserProgressEntity> _result = new ArrayList<UserProgressEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UserProgressEntity _item;
            final String _tmpWordId;
            _tmpWordId = _cursor.getString(_cursorIndexOfWordId);
            final String _tmpState;
            _tmpState = _cursor.getString(_cursorIndexOfState);
            final int _tmpIntervalDays;
            _tmpIntervalDays = _cursor.getInt(_cursorIndexOfIntervalDays);
            final int _tmpCorrectStreak;
            _tmpCorrectStreak = _cursor.getInt(_cursorIndexOfCorrectStreak);
            final String _tmpLastAnsweredDate;
            if (_cursor.isNull(_cursorIndexOfLastAnsweredDate)) {
              _tmpLastAnsweredDate = null;
            } else {
              _tmpLastAnsweredDate = _cursor.getString(_cursorIndexOfLastAnsweredDate);
            }
            _item = new UserProgressEntity(_tmpWordId,_tmpState,_tmpIntervalDays,_tmpCorrectStreak,_tmpLastAnsweredDate);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllProgress(final Continuation<? super List<UserProgressEntity>> $completion) {
    final String _sql = "SELECT * FROM user_progress";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<UserProgressEntity>>() {
      @Override
      @NonNull
      public List<UserProgressEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfWordId = CursorUtil.getColumnIndexOrThrow(_cursor, "wordId");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfIntervalDays = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalDays");
          final int _cursorIndexOfCorrectStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "correctStreak");
          final int _cursorIndexOfLastAnsweredDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastAnsweredDate");
          final List<UserProgressEntity> _result = new ArrayList<UserProgressEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UserProgressEntity _item;
            final String _tmpWordId;
            _tmpWordId = _cursor.getString(_cursorIndexOfWordId);
            final String _tmpState;
            _tmpState = _cursor.getString(_cursorIndexOfState);
            final int _tmpIntervalDays;
            _tmpIntervalDays = _cursor.getInt(_cursorIndexOfIntervalDays);
            final int _tmpCorrectStreak;
            _tmpCorrectStreak = _cursor.getInt(_cursorIndexOfCorrectStreak);
            final String _tmpLastAnsweredDate;
            if (_cursor.isNull(_cursorIndexOfLastAnsweredDate)) {
              _tmpLastAnsweredDate = null;
            } else {
              _tmpLastAnsweredDate = _cursor.getString(_cursorIndexOfLastAnsweredDate);
            }
            _item = new UserProgressEntity(_tmpWordId,_tmpState,_tmpIntervalDays,_tmpCorrectStreak,_tmpLastAnsweredDate);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
