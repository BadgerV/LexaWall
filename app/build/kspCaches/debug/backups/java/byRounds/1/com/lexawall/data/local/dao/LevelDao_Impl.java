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
import com.lexawall.data.local.entities.LevelEntity;
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
public final class LevelDao_Impl implements LevelDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<LevelEntity> __insertionAdapterOfLevelEntity;

  private final EntityDeletionOrUpdateAdapter<LevelEntity> __updateAdapterOfLevelEntity;

  public LevelDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfLevelEntity = new EntityInsertionAdapter<LevelEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `level_table` (`id`,`currentXp`,`totalXpEarned`,`currentLevel`,`title`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LevelEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getCurrentXp());
        statement.bindLong(3, entity.getTotalXpEarned());
        statement.bindLong(4, entity.getCurrentLevel());
        statement.bindString(5, entity.getTitle());
      }
    };
    this.__updateAdapterOfLevelEntity = new EntityDeletionOrUpdateAdapter<LevelEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `level_table` SET `id` = ?,`currentXp` = ?,`totalXpEarned` = ?,`currentLevel` = ?,`title` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LevelEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getCurrentXp());
        statement.bindLong(3, entity.getTotalXpEarned());
        statement.bindLong(4, entity.getCurrentLevel());
        statement.bindString(5, entity.getTitle());
        statement.bindLong(6, entity.getId());
      }
    };
  }

  @Override
  public Object insertOrReplace(final LevelEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfLevelEntity.insert(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLevel(final LevelEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfLevelEntity.handle(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getLevel(final Continuation<? super LevelEntity> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> LevelDao.DefaultImpls.getLevel(LevelDao_Impl.this, __cont), $completion);
  }

  @Override
  public Object getLevelOrNull(final Continuation<? super LevelEntity> $completion) {
    final String _sql = "SELECT * FROM level_table WHERE id = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<LevelEntity>() {
      @Override
      @Nullable
      public LevelEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCurrentXp = CursorUtil.getColumnIndexOrThrow(_cursor, "currentXp");
          final int _cursorIndexOfTotalXpEarned = CursorUtil.getColumnIndexOrThrow(_cursor, "totalXpEarned");
          final int _cursorIndexOfCurrentLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "currentLevel");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final LevelEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpCurrentXp;
            _tmpCurrentXp = _cursor.getInt(_cursorIndexOfCurrentXp);
            final int _tmpTotalXpEarned;
            _tmpTotalXpEarned = _cursor.getInt(_cursorIndexOfTotalXpEarned);
            final int _tmpCurrentLevel;
            _tmpCurrentLevel = _cursor.getInt(_cursorIndexOfCurrentLevel);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            _result = new LevelEntity(_tmpId,_tmpCurrentXp,_tmpTotalXpEarned,_tmpCurrentLevel,_tmpTitle);
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
