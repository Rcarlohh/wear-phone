package com.example.sleepmonitor.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.sleepmonitor.data.SleepData;
import com.example.sleepmonitor.database.converter.DateConverter;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalStateException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SleepDao_Impl implements SleepDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SleepData> __insertionAdapterOfSleepData;

  private final DateConverter __dateConverter = new DateConverter();

  private final EntityDeletionOrUpdateAdapter<SleepData> __deletionAdapterOfSleepData;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public SleepDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSleepData = new EntityInsertionAdapter<SleepData>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sleep_data` (`id`,`date`,`duration`,`quality`,`heartRate`,`stepCount`,`deepSleepDuration`,`lightSleepDuration`,`remSleepDuration`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SleepData entity) {
        statement.bindLong(1, entity.getId());
        final Long _tmp = __dateConverter.dateToTimestamp(entity.getDate());
        if (_tmp == null) {
          statement.bindNull(2);
        } else {
          statement.bindLong(2, _tmp);
        }
        statement.bindDouble(3, entity.getDuration());
        statement.bindLong(4, entity.getQuality());
        statement.bindLong(5, entity.getHeartRate());
        statement.bindLong(6, entity.getStepCount());
        statement.bindDouble(7, entity.getDeepSleepDuration());
        statement.bindDouble(8, entity.getLightSleepDuration());
        statement.bindDouble(9, entity.getRemSleepDuration());
      }
    };
    this.__deletionAdapterOfSleepData = new EntityDeletionOrUpdateAdapter<SleepData>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `sleep_data` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SleepData entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sleep_data";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final SleepData sleepData, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSleepData.insert(sleepData);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final SleepData sleepData, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSleepData.handle(sleepData);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
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
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<SleepData>> getAllSleepData() {
    final String _sql = "SELECT * FROM sleep_data ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"sleep_data"}, false, new Callable<List<SleepData>>() {
      @Override
      @Nullable
      public List<SleepData> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfQuality = CursorUtil.getColumnIndexOrThrow(_cursor, "quality");
          final int _cursorIndexOfHeartRate = CursorUtil.getColumnIndexOrThrow(_cursor, "heartRate");
          final int _cursorIndexOfStepCount = CursorUtil.getColumnIndexOrThrow(_cursor, "stepCount");
          final int _cursorIndexOfDeepSleepDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "deepSleepDuration");
          final int _cursorIndexOfLightSleepDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "lightSleepDuration");
          final int _cursorIndexOfRemSleepDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "remSleepDuration");
          final List<SleepData> _result = new ArrayList<SleepData>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SleepData _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Date _tmpDate;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfDate)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfDate);
            }
            final Date _tmp_1 = __dateConverter.fromTimestamp(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpDate = _tmp_1;
            }
            final float _tmpDuration;
            _tmpDuration = _cursor.getFloat(_cursorIndexOfDuration);
            final int _tmpQuality;
            _tmpQuality = _cursor.getInt(_cursorIndexOfQuality);
            final int _tmpHeartRate;
            _tmpHeartRate = _cursor.getInt(_cursorIndexOfHeartRate);
            final int _tmpStepCount;
            _tmpStepCount = _cursor.getInt(_cursorIndexOfStepCount);
            final float _tmpDeepSleepDuration;
            _tmpDeepSleepDuration = _cursor.getFloat(_cursorIndexOfDeepSleepDuration);
            final float _tmpLightSleepDuration;
            _tmpLightSleepDuration = _cursor.getFloat(_cursorIndexOfLightSleepDuration);
            final float _tmpRemSleepDuration;
            _tmpRemSleepDuration = _cursor.getFloat(_cursorIndexOfRemSleepDuration);
            _item = new SleepData(_tmpId,_tmpDate,_tmpDuration,_tmpQuality,_tmpHeartRate,_tmpStepCount,_tmpDeepSleepDuration,_tmpLightSleepDuration,_tmpRemSleepDuration);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getLatestSleepData(final Continuation<? super SleepData> $completion) {
    final String _sql = "SELECT * FROM sleep_data ORDER BY date DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SleepData>() {
      @Override
      @Nullable
      public SleepData call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfQuality = CursorUtil.getColumnIndexOrThrow(_cursor, "quality");
          final int _cursorIndexOfHeartRate = CursorUtil.getColumnIndexOrThrow(_cursor, "heartRate");
          final int _cursorIndexOfStepCount = CursorUtil.getColumnIndexOrThrow(_cursor, "stepCount");
          final int _cursorIndexOfDeepSleepDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "deepSleepDuration");
          final int _cursorIndexOfLightSleepDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "lightSleepDuration");
          final int _cursorIndexOfRemSleepDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "remSleepDuration");
          final SleepData _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Date _tmpDate;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfDate)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfDate);
            }
            final Date _tmp_1 = __dateConverter.fromTimestamp(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpDate = _tmp_1;
            }
            final float _tmpDuration;
            _tmpDuration = _cursor.getFloat(_cursorIndexOfDuration);
            final int _tmpQuality;
            _tmpQuality = _cursor.getInt(_cursorIndexOfQuality);
            final int _tmpHeartRate;
            _tmpHeartRate = _cursor.getInt(_cursorIndexOfHeartRate);
            final int _tmpStepCount;
            _tmpStepCount = _cursor.getInt(_cursorIndexOfStepCount);
            final float _tmpDeepSleepDuration;
            _tmpDeepSleepDuration = _cursor.getFloat(_cursorIndexOfDeepSleepDuration);
            final float _tmpLightSleepDuration;
            _tmpLightSleepDuration = _cursor.getFloat(_cursorIndexOfLightSleepDuration);
            final float _tmpRemSleepDuration;
            _tmpRemSleepDuration = _cursor.getFloat(_cursorIndexOfRemSleepDuration);
            _result = new SleepData(_tmpId,_tmpDate,_tmpDuration,_tmpQuality,_tmpHeartRate,_tmpStepCount,_tmpDeepSleepDuration,_tmpLightSleepDuration,_tmpRemSleepDuration);
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
  public LiveData<List<SleepData>> getSleepDataFromDate(final Date startDate) {
    final String _sql = "SELECT * FROM sleep_data WHERE date >= ? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final Long _tmp = __dateConverter.dateToTimestamp(startDate);
    if (_tmp == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, _tmp);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"sleep_data"}, false, new Callable<List<SleepData>>() {
      @Override
      @Nullable
      public List<SleepData> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfQuality = CursorUtil.getColumnIndexOrThrow(_cursor, "quality");
          final int _cursorIndexOfHeartRate = CursorUtil.getColumnIndexOrThrow(_cursor, "heartRate");
          final int _cursorIndexOfStepCount = CursorUtil.getColumnIndexOrThrow(_cursor, "stepCount");
          final int _cursorIndexOfDeepSleepDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "deepSleepDuration");
          final int _cursorIndexOfLightSleepDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "lightSleepDuration");
          final int _cursorIndexOfRemSleepDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "remSleepDuration");
          final List<SleepData> _result = new ArrayList<SleepData>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SleepData _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Date _tmpDate;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfDate)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfDate);
            }
            final Date _tmp_2 = __dateConverter.fromTimestamp(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpDate = _tmp_2;
            }
            final float _tmpDuration;
            _tmpDuration = _cursor.getFloat(_cursorIndexOfDuration);
            final int _tmpQuality;
            _tmpQuality = _cursor.getInt(_cursorIndexOfQuality);
            final int _tmpHeartRate;
            _tmpHeartRate = _cursor.getInt(_cursorIndexOfHeartRate);
            final int _tmpStepCount;
            _tmpStepCount = _cursor.getInt(_cursorIndexOfStepCount);
            final float _tmpDeepSleepDuration;
            _tmpDeepSleepDuration = _cursor.getFloat(_cursorIndexOfDeepSleepDuration);
            final float _tmpLightSleepDuration;
            _tmpLightSleepDuration = _cursor.getFloat(_cursorIndexOfLightSleepDuration);
            final float _tmpRemSleepDuration;
            _tmpRemSleepDuration = _cursor.getFloat(_cursorIndexOfRemSleepDuration);
            _item = new SleepData(_tmpId,_tmpDate,_tmpDuration,_tmpQuality,_tmpHeartRate,_tmpStepCount,_tmpDeepSleepDuration,_tmpLightSleepDuration,_tmpRemSleepDuration);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
