package com.lexawall.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.lexawall.data.local.entities.StreakEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class StreakDao_Impl implements StreakDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<StreakEntity> __insertionAdapterOfStreakEntity;

  private final EntityDeletionOrUpdateAdapter<StreakEntity> __updateAdapterOfStreakEntity;

  public StreakDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfStreakEntity = new EntityInsertionAdapter<StreakEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `streak` (`id`,`currentStreak`,`lastCompletedDate`,`freezesUsedThisWeek`,`weekStartDate`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StreakEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getCurrentStreak());
        if (entity.getLastCompletedDate() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getLastCompletedDate());
        }
        statement.bindLong(4, entity.getFreezesUsedThisWeek());
        if (entity.getWeekStartDate() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getWeekStartDate());
        }
      }
    };
    this.__updateAdapterOfStreakEntity = new EntityDeletionOrUpdateAdapter<StreakEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `streak` SET `id` = ?,`currentStreak` = ?,`lastCompletedDate` = ?,`freezesUsedThisWeek` = ?,`weekStartDate` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StreakEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getCurrentStreak());
        if (entity.getLastCompletedDate() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getLastCompletedDate());
        }
        statement.bindLong(4, entity.getFreezesUsedThisWeek());
        if (entity.getWeekStartDate() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getWeekStartDate());
        }
        statement.bindLong(6, entity.getId());
      }
    };
  }

  @Override
  public Object insertOrReplace(final StreakEntity streak,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStreakEntity.insert(streak);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStreak(final StreakEntity streak,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfStreakEntity.handle(streak);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getStreak(final Continuation<? super StreakEntity> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> StreakDao.DefaultImpls.getStreak(StreakDao_Impl.this, __cont), $completion);
  }

  @Override
  public Object getStreakOrNull(final Continuation<? super StreakEntity> $completion) {
    final String _sql = "SELECT * FROM streak LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<StreakEntity>() {
      @Override
      @Nullable
      public StreakEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCurrentStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStreak");
          final int _cursorIndexOfLastCompletedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastCompletedDate");
          final int _cursorIndexOfFreezesUsedThisWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "freezesUsedThisWeek");
          final int _cursorIndexOfWeekStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "weekStartDate");
          final StreakEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpCurrentStreak;
            _tmpCurrentStreak = _cursor.getInt(_cursorIndexOfCurrentStreak);
            final String _tmpLastCompletedDate;
            if (_cursor.isNull(_cursorIndexOfLastCompletedDate)) {
              _tmpLastCompletedDate = null;
            } else {
              _tmpLastCompletedDate = _cursor.getString(_cursorIndexOfLastCompletedDate);
            }
            final int _tmpFreezesUsedThisWeek;
            _tmpFreezesUsedThisWeek = _cursor.getInt(_cursorIndexOfFreezesUsedThisWeek);
            final String _tmpWeekStartDate;
            if (_cursor.isNull(_cursorIndexOfWeekStartDate)) {
              _tmpWeekStartDate = null;
            } else {
              _tmpWeekStartDate = _cursor.getString(_cursorIndexOfWeekStartDate);
            }
            _result = new StreakEntity(_tmpId,_tmpCurrentStreak,_tmpLastCompletedDate,_tmpFreezesUsedThisWeek,_tmpWeekStartDate);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
