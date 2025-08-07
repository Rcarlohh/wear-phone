package com.example.sleepmonitor.database;

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
import com.example.sleepmonitor.database.dao.SleepDao;
import com.example.sleepmonitor.database.dao.SleepDao_Impl;
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
public final class SleepDatabase_Impl extends SleepDatabase {
  private volatile SleepDao _sleepDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `sleep_data` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` INTEGER NOT NULL, `duration` REAL NOT NULL, `quality` INTEGER NOT NULL, `heartRate` INTEGER NOT NULL, `stepCount` INTEGER NOT NULL, `deepSleepDuration` REAL NOT NULL, `lightSleepDuration` REAL NOT NULL, `remSleepDuration` REAL NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'f567644a154fe01ecb827fc864dcfb4c')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `sleep_data`");
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
        final HashMap<String, TableInfo.Column> _columnsSleepData = new HashMap<String, TableInfo.Column>(9);
        _columnsSleepData.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepData.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepData.put("duration", new TableInfo.Column("duration", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepData.put("quality", new TableInfo.Column("quality", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepData.put("heartRate", new TableInfo.Column("heartRate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepData.put("stepCount", new TableInfo.Column("stepCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepData.put("deepSleepDuration", new TableInfo.Column("deepSleepDuration", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepData.put("lightSleepDuration", new TableInfo.Column("lightSleepDuration", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepData.put("remSleepDuration", new TableInfo.Column("remSleepDuration", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSleepData = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSleepData = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSleepData = new TableInfo("sleep_data", _columnsSleepData, _foreignKeysSleepData, _indicesSleepData);
        final TableInfo _existingSleepData = TableInfo.read(db, "sleep_data");
        if (!_infoSleepData.equals(_existingSleepData)) {
          return new RoomOpenHelper.ValidationResult(false, "sleep_data(com.example.sleepmonitor.data.SleepData).\n"
                  + " Expected:\n" + _infoSleepData + "\n"
                  + " Found:\n" + _existingSleepData);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "f567644a154fe01ecb827fc864dcfb4c", "687eeed90aa2a4162d89eaf8050f832a");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "sleep_data");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `sleep_data`");
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
    _typeConvertersMap.put(SleepDao.class, SleepDao_Impl.getRequiredConverters());
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
  public SleepDao sleepDao() {
    if (_sleepDao != null) {
      return _sleepDao;
    } else {
      synchronized(this) {
        if(_sleepDao == null) {
          _sleepDao = new SleepDao_Impl(this);
        }
        return _sleepDao;
      }
    }
  }
}
