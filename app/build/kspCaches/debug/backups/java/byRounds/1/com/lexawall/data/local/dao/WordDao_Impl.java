package com.lexawall.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.lexawall.data.local.entities.WordEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
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
public final class WordDao_Impl implements WordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WordEntity> __insertionAdapterOfWordEntity;

  public WordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWordEntity = new EntityInsertionAdapter<WordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `words` (`id`,`word`,`partOfSpeech`,`cefrLevel`,`definition`,`exampleSentence`,`notificationLine1`,`notificationLine2`,`pronunciationIpa`,`etymology`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WordEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getWord());
        statement.bindString(3, entity.getPartOfSpeech());
        statement.bindString(4, entity.getCefrLevel());
        statement.bindString(5, entity.getDefinition());
        statement.bindString(6, entity.getExampleSentence());
        statement.bindString(7, entity.getNotificationLine1());
        statement.bindString(8, entity.getNotificationLine2());
        if (entity.getPronunciationIpa() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getPronunciationIpa());
        }
        if (entity.getEtymology() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getEtymology());
        }
      }
    };
  }

  @Override
  public Object insertAll(final List<WordEntity> words,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWordEntity.insert(words);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getWordById(final String id, final Continuation<? super WordEntity> $completion) {
    final String _sql = "SELECT * FROM words WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<WordEntity>() {
      @Override
      @Nullable
      public WordEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfPartOfSpeech = CursorUtil.getColumnIndexOrThrow(_cursor, "partOfSpeech");
          final int _cursorIndexOfCefrLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "cefrLevel");
          final int _cursorIndexOfDefinition = CursorUtil.getColumnIndexOrThrow(_cursor, "definition");
          final int _cursorIndexOfExampleSentence = CursorUtil.getColumnIndexOrThrow(_cursor, "exampleSentence");
          final int _cursorIndexOfNotificationLine1 = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationLine1");
          final int _cursorIndexOfNotificationLine2 = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationLine2");
          final int _cursorIndexOfPronunciationIpa = CursorUtil.getColumnIndexOrThrow(_cursor, "pronunciationIpa");
          final int _cursorIndexOfEtymology = CursorUtil.getColumnIndexOrThrow(_cursor, "etymology");
          final WordEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpPartOfSpeech;
            _tmpPartOfSpeech = _cursor.getString(_cursorIndexOfPartOfSpeech);
            final String _tmpCefrLevel;
            _tmpCefrLevel = _cursor.getString(_cursorIndexOfCefrLevel);
            final String _tmpDefinition;
            _tmpDefinition = _cursor.getString(_cursorIndexOfDefinition);
            final String _tmpExampleSentence;
            _tmpExampleSentence = _cursor.getString(_cursorIndexOfExampleSentence);
            final String _tmpNotificationLine1;
            _tmpNotificationLine1 = _cursor.getString(_cursorIndexOfNotificationLine1);
            final String _tmpNotificationLine2;
            _tmpNotificationLine2 = _cursor.getString(_cursorIndexOfNotificationLine2);
            final String _tmpPronunciationIpa;
            if (_cursor.isNull(_cursorIndexOfPronunciationIpa)) {
              _tmpPronunciationIpa = null;
            } else {
              _tmpPronunciationIpa = _cursor.getString(_cursorIndexOfPronunciationIpa);
            }
            final String _tmpEtymology;
            if (_cursor.isNull(_cursorIndexOfEtymology)) {
              _tmpEtymology = null;
            } else {
              _tmpEtymology = _cursor.getString(_cursorIndexOfEtymology);
            }
            _result = new WordEntity(_tmpId,_tmpWord,_tmpPartOfSpeech,_tmpCefrLevel,_tmpDefinition,_tmpExampleSentence,_tmpNotificationLine1,_tmpNotificationLine2,_tmpPronunciationIpa,_tmpEtymology);
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
  public Object getWordsByLevel(final String cefrLevel,
      final Continuation<? super List<WordEntity>> $completion) {
    final String _sql = "SELECT * FROM words WHERE cefrLevel = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, cefrLevel);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<WordEntity>>() {
      @Override
      @NonNull
      public List<WordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfPartOfSpeech = CursorUtil.getColumnIndexOrThrow(_cursor, "partOfSpeech");
          final int _cursorIndexOfCefrLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "cefrLevel");
          final int _cursorIndexOfDefinition = CursorUtil.getColumnIndexOrThrow(_cursor, "definition");
          final int _cursorIndexOfExampleSentence = CursorUtil.getColumnIndexOrThrow(_cursor, "exampleSentence");
          final int _cursorIndexOfNotificationLine1 = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationLine1");
          final int _cursorIndexOfNotificationLine2 = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationLine2");
          final int _cursorIndexOfPronunciationIpa = CursorUtil.getColumnIndexOrThrow(_cursor, "pronunciationIpa");
          final int _cursorIndexOfEtymology = CursorUtil.getColumnIndexOrThrow(_cursor, "etymology");
          final List<WordEntity> _result = new ArrayList<WordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WordEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpPartOfSpeech;
            _tmpPartOfSpeech = _cursor.getString(_cursorIndexOfPartOfSpeech);
            final String _tmpCefrLevel;
            _tmpCefrLevel = _cursor.getString(_cursorIndexOfCefrLevel);
            final String _tmpDefinition;
            _tmpDefinition = _cursor.getString(_cursorIndexOfDefinition);
            final String _tmpExampleSentence;
            _tmpExampleSentence = _cursor.getString(_cursorIndexOfExampleSentence);
            final String _tmpNotificationLine1;
            _tmpNotificationLine1 = _cursor.getString(_cursorIndexOfNotificationLine1);
            final String _tmpNotificationLine2;
            _tmpNotificationLine2 = _cursor.getString(_cursorIndexOfNotificationLine2);
            final String _tmpPronunciationIpa;
            if (_cursor.isNull(_cursorIndexOfPronunciationIpa)) {
              _tmpPronunciationIpa = null;
            } else {
              _tmpPronunciationIpa = _cursor.getString(_cursorIndexOfPronunciationIpa);
            }
            final String _tmpEtymology;
            if (_cursor.isNull(_cursorIndexOfEtymology)) {
              _tmpEtymology = null;
            } else {
              _tmpEtymology = _cursor.getString(_cursorIndexOfEtymology);
            }
            _item = new WordEntity(_tmpId,_tmpWord,_tmpPartOfSpeech,_tmpCefrLevel,_tmpDefinition,_tmpExampleSentence,_tmpNotificationLine1,_tmpNotificationLine2,_tmpPronunciationIpa,_tmpEtymology);
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
  public Object getWordCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM words";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
  public Object getAllWords(final Continuation<? super List<WordEntity>> $completion) {
    final String _sql = "SELECT * FROM words";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<WordEntity>>() {
      @Override
      @NonNull
      public List<WordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfPartOfSpeech = CursorUtil.getColumnIndexOrThrow(_cursor, "partOfSpeech");
          final int _cursorIndexOfCefrLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "cefrLevel");
          final int _cursorIndexOfDefinition = CursorUtil.getColumnIndexOrThrow(_cursor, "definition");
          final int _cursorIndexOfExampleSentence = CursorUtil.getColumnIndexOrThrow(_cursor, "exampleSentence");
          final int _cursorIndexOfNotificationLine1 = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationLine1");
          final int _cursorIndexOfNotificationLine2 = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationLine2");
          final int _cursorIndexOfPronunciationIpa = CursorUtil.getColumnIndexOrThrow(_cursor, "pronunciationIpa");
          final int _cursorIndexOfEtymology = CursorUtil.getColumnIndexOrThrow(_cursor, "etymology");
          final List<WordEntity> _result = new ArrayList<WordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WordEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpPartOfSpeech;
            _tmpPartOfSpeech = _cursor.getString(_cursorIndexOfPartOfSpeech);
            final String _tmpCefrLevel;
            _tmpCefrLevel = _cursor.getString(_cursorIndexOfCefrLevel);
            final String _tmpDefinition;
            _tmpDefinition = _cursor.getString(_cursorIndexOfDefinition);
            final String _tmpExampleSentence;
            _tmpExampleSentence = _cursor.getString(_cursorIndexOfExampleSentence);
            final String _tmpNotificationLine1;
            _tmpNotificationLine1 = _cursor.getString(_cursorIndexOfNotificationLine1);
            final String _tmpNotificationLine2;
            _tmpNotificationLine2 = _cursor.getString(_cursorIndexOfNotificationLine2);
            final String _tmpPronunciationIpa;
            if (_cursor.isNull(_cursorIndexOfPronunciationIpa)) {
              _tmpPronunciationIpa = null;
            } else {
              _tmpPronunciationIpa = _cursor.getString(_cursorIndexOfPronunciationIpa);
            }
            final String _tmpEtymology;
            if (_cursor.isNull(_cursorIndexOfEtymology)) {
              _tmpEtymology = null;
            } else {
              _tmpEtymology = _cursor.getString(_cursorIndexOfEtymology);
            }
            _item = new WordEntity(_tmpId,_tmpWord,_tmpPartOfSpeech,_tmpCefrLevel,_tmpDefinition,_tmpExampleSentence,_tmpNotificationLine1,_tmpNotificationLine2,_tmpPronunciationIpa,_tmpEtymology);
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
  public Object getRandomWords(final int count,
      final Continuation<? super List<WordEntity>> $completion) {
    final String _sql = "SELECT * FROM words ORDER BY RANDOM() LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, count);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<WordEntity>>() {
      @Override
      @NonNull
      public List<WordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfPartOfSpeech = CursorUtil.getColumnIndexOrThrow(_cursor, "partOfSpeech");
          final int _cursorIndexOfCefrLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "cefrLevel");
          final int _cursorIndexOfDefinition = CursorUtil.getColumnIndexOrThrow(_cursor, "definition");
          final int _cursorIndexOfExampleSentence = CursorUtil.getColumnIndexOrThrow(_cursor, "exampleSentence");
          final int _cursorIndexOfNotificationLine1 = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationLine1");
          final int _cursorIndexOfNotificationLine2 = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationLine2");
          final int _cursorIndexOfPronunciationIpa = CursorUtil.getColumnIndexOrThrow(_cursor, "pronunciationIpa");
          final int _cursorIndexOfEtymology = CursorUtil.getColumnIndexOrThrow(_cursor, "etymology");
          final List<WordEntity> _result = new ArrayList<WordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WordEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpPartOfSpeech;
            _tmpPartOfSpeech = _cursor.getString(_cursorIndexOfPartOfSpeech);
            final String _tmpCefrLevel;
            _tmpCefrLevel = _cursor.getString(_cursorIndexOfCefrLevel);
            final String _tmpDefinition;
            _tmpDefinition = _cursor.getString(_cursorIndexOfDefinition);
            final String _tmpExampleSentence;
            _tmpExampleSentence = _cursor.getString(_cursorIndexOfExampleSentence);
            final String _tmpNotificationLine1;
            _tmpNotificationLine1 = _cursor.getString(_cursorIndexOfNotificationLine1);
            final String _tmpNotificationLine2;
            _tmpNotificationLine2 = _cursor.getString(_cursorIndexOfNotificationLine2);
            final String _tmpPronunciationIpa;
            if (_cursor.isNull(_cursorIndexOfPronunciationIpa)) {
              _tmpPronunciationIpa = null;
            } else {
              _tmpPronunciationIpa = _cursor.getString(_cursorIndexOfPronunciationIpa);
            }
            final String _tmpEtymology;
            if (_cursor.isNull(_cursorIndexOfEtymology)) {
              _tmpEtymology = null;
            } else {
              _tmpEtymology = _cursor.getString(_cursorIndexOfEtymology);
            }
            _item = new WordEntity(_tmpId,_tmpWord,_tmpPartOfSpeech,_tmpCefrLevel,_tmpDefinition,_tmpExampleSentence,_tmpNotificationLine1,_tmpNotificationLine2,_tmpPronunciationIpa,_tmpEtymology);
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
  public Object getWordsByIds(final List<String> ids,
      final Continuation<? super List<WordEntity>> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM words WHERE id IN (");
    final int _inputSize = ids.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : ids) {
      _statement.bindString(_argIndex, _item);
      _argIndex++;
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<WordEntity>>() {
      @Override
      @NonNull
      public List<WordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfPartOfSpeech = CursorUtil.getColumnIndexOrThrow(_cursor, "partOfSpeech");
          final int _cursorIndexOfCefrLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "cefrLevel");
          final int _cursorIndexOfDefinition = CursorUtil.getColumnIndexOrThrow(_cursor, "definition");
          final int _cursorIndexOfExampleSentence = CursorUtil.getColumnIndexOrThrow(_cursor, "exampleSentence");
          final int _cursorIndexOfNotificationLine1 = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationLine1");
          final int _cursorIndexOfNotificationLine2 = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationLine2");
          final int _cursorIndexOfPronunciationIpa = CursorUtil.getColumnIndexOrThrow(_cursor, "pronunciationIpa");
          final int _cursorIndexOfEtymology = CursorUtil.getColumnIndexOrThrow(_cursor, "etymology");
          final List<WordEntity> _result = new ArrayList<WordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WordEntity _item_1;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpPartOfSpeech;
            _tmpPartOfSpeech = _cursor.getString(_cursorIndexOfPartOfSpeech);
            final String _tmpCefrLevel;
            _tmpCefrLevel = _cursor.getString(_cursorIndexOfCefrLevel);
            final String _tmpDefinition;
            _tmpDefinition = _cursor.getString(_cursorIndexOfDefinition);
            final String _tmpExampleSentence;
            _tmpExampleSentence = _cursor.getString(_cursorIndexOfExampleSentence);
            final String _tmpNotificationLine1;
            _tmpNotificationLine1 = _cursor.getString(_cursorIndexOfNotificationLine1);
            final String _tmpNotificationLine2;
            _tmpNotificationLine2 = _cursor.getString(_cursorIndexOfNotificationLine2);
            final String _tmpPronunciationIpa;
            if (_cursor.isNull(_cursorIndexOfPronunciationIpa)) {
              _tmpPronunciationIpa = null;
            } else {
              _tmpPronunciationIpa = _cursor.getString(_cursorIndexOfPronunciationIpa);
            }
            final String _tmpEtymology;
            if (_cursor.isNull(_cursorIndexOfEtymology)) {
              _tmpEtymology = null;
            } else {
              _tmpEtymology = _cursor.getString(_cursorIndexOfEtymology);
            }
            _item_1 = new WordEntity(_tmpId,_tmpWord,_tmpPartOfSpeech,_tmpCefrLevel,_tmpDefinition,_tmpExampleSentence,_tmpNotificationLine1,_tmpNotificationLine2,_tmpPronunciationIpa,_tmpEtymology);
            _result.add(_item_1);
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
  public Object getWanderWords(final List<String> excludeIds, final int count,
      final Continuation<? super List<WordEntity>> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM words WHERE id NOT IN (");
    final int _inputSize = excludeIds.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(") ORDER BY RANDOM() LIMIT ");
    _stringBuilder.append("?");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 1 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : excludeIds) {
      _statement.bindString(_argIndex, _item);
      _argIndex++;
    }
    _argIndex = 1 + _inputSize;
    _statement.bindLong(_argIndex, count);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<WordEntity>>() {
      @Override
      @NonNull
      public List<WordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfPartOfSpeech = CursorUtil.getColumnIndexOrThrow(_cursor, "partOfSpeech");
          final int _cursorIndexOfCefrLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "cefrLevel");
          final int _cursorIndexOfDefinition = CursorUtil.getColumnIndexOrThrow(_cursor, "definition");
          final int _cursorIndexOfExampleSentence = CursorUtil.getColumnIndexOrThrow(_cursor, "exampleSentence");
          final int _cursorIndexOfNotificationLine1 = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationLine1");
          final int _cursorIndexOfNotificationLine2 = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationLine2");
          final int _cursorIndexOfPronunciationIpa = CursorUtil.getColumnIndexOrThrow(_cursor, "pronunciationIpa");
          final int _cursorIndexOfEtymology = CursorUtil.getColumnIndexOrThrow(_cursor, "etymology");
          final List<WordEntity> _result = new ArrayList<WordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WordEntity _item_1;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpPartOfSpeech;
            _tmpPartOfSpeech = _cursor.getString(_cursorIndexOfPartOfSpeech);
            final String _tmpCefrLevel;
            _tmpCefrLevel = _cursor.getString(_cursorIndexOfCefrLevel);
            final String _tmpDefinition;
            _tmpDefinition = _cursor.getString(_cursorIndexOfDefinition);
            final String _tmpExampleSentence;
            _tmpExampleSentence = _cursor.getString(_cursorIndexOfExampleSentence);
            final String _tmpNotificationLine1;
            _tmpNotificationLine1 = _cursor.getString(_cursorIndexOfNotificationLine1);
            final String _tmpNotificationLine2;
            _tmpNotificationLine2 = _cursor.getString(_cursorIndexOfNotificationLine2);
            final String _tmpPronunciationIpa;
            if (_cursor.isNull(_cursorIndexOfPronunciationIpa)) {
              _tmpPronunciationIpa = null;
            } else {
              _tmpPronunciationIpa = _cursor.getString(_cursorIndexOfPronunciationIpa);
            }
            final String _tmpEtymology;
            if (_cursor.isNull(_cursorIndexOfEtymology)) {
              _tmpEtymology = null;
            } else {
              _tmpEtymology = _cursor.getString(_cursorIndexOfEtymology);
            }
            _item_1 = new WordEntity(_tmpId,_tmpWord,_tmpPartOfSpeech,_tmpCefrLevel,_tmpDefinition,_tmpExampleSentence,_tmpNotificationLine1,_tmpNotificationLine2,_tmpPronunciationIpa,_tmpEtymology);
            _result.add(_item_1);
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
