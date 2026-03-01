package com.lexawall.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.lexawall.data.local.entities.QuizSessionEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
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
public final class QuizSessionDao_Impl implements QuizSessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<QuizSessionEntity> __insertionAdapterOfQuizSessionEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public QuizSessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfQuizSessionEntity = new EntityInsertionAdapter<QuizSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `quiz_sessions` (`id`,`datestamp`,`score`,`total`,`xpEarned`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final QuizSessionEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getDatestamp());
        statement.bindLong(3, entity.getScore());
        statement.bindLong(4, entity.getTotal());
        statement.bindLong(5, entity.getXpEarned());
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM quiz_sessions";
        return _query;
      }
    };
  }

  @Override
  public Object insertSession(final QuizSessionEntity session,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfQuizSessionEntity.insertAndReturnId(session);
          __db.setTransactionSuccessful();
          return _result;
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
  public Object getRecentSessions(final int limit,
      final Continuation<? super List<QuizSessionEntity>> $completion) {
    final String _sql = "SELECT * FROM quiz_sessions ORDER BY id DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<QuizSessionEntity>>() {
      @Override
      @NonNull
      public List<QuizSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDatestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "datestamp");
          final int _cursorIndexOfScore = CursorUtil.getColumnIndexOrThrow(_cursor, "score");
          final int _cursorIndexOfTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "total");
          final int _cursorIndexOfXpEarned = CursorUtil.getColumnIndexOrThrow(_cursor, "xpEarned");
          final List<QuizSessionEntity> _result = new ArrayList<QuizSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final QuizSessionEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpDatestamp;
            _tmpDatestamp = _cursor.getString(_cursorIndexOfDatestamp);
            final int _tmpScore;
            _tmpScore = _cursor.getInt(_cursorIndexOfScore);
            final int _tmpTotal;
            _tmpTotal = _cursor.getInt(_cursorIndexOfTotal);
            final int _tmpXpEarned;
            _tmpXpEarned = _cursor.getInt(_cursorIndexOfXpEarned);
            _item = new QuizSessionEntity(_tmpId,_tmpDatestamp,_tmpScore,_tmpTotal,_tmpXpEarned);
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
  public Object getAllSessions(final Continuation<? super List<QuizSessionEntity>> $completion) {
    final String _sql = "SELECT * FROM quiz_sessions ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<QuizSessionEntity>>() {
      @Override
      @NonNull
      public List<QuizSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDatestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "datestamp");
          final int _cursorIndexOfScore = CursorUtil.getColumnIndexOrThrow(_cursor, "score");
          final int _cursorIndexOfTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "total");
          final int _cursorIndexOfXpEarned = CursorUtil.getColumnIndexOrThrow(_cursor, "xpEarned");
          final List<QuizSessionEntity> _result = new ArrayList<QuizSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final QuizSessionEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpDatestamp;
            _tmpDatestamp = _cursor.getString(_cursorIndexOfDatestamp);
            final int _tmpScore;
            _tmpScore = _cursor.getInt(_cursorIndexOfScore);
            final int _tmpTotal;
            _tmpTotal = _cursor.getInt(_cursorIndexOfTotal);
            final int _tmpXpEarned;
            _tmpXpEarned = _cursor.getInt(_cursorIndexOfXpEarned);
            _item = new QuizSessionEntity(_tmpId,_tmpDatestamp,_tmpScore,_tmpTotal,_tmpXpEarned);
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
