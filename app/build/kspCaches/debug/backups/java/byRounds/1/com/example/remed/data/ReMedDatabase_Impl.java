package com.example.remed.data;

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
public final class ReMedDatabase_Impl extends ReMedDatabase {
  private volatile MedicationDao _medicationDao;

  private volatile WaterDao _waterDao;

  private volatile StepDao _stepDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(4) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `medications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` TEXT NOT NULL, `name` TEXT NOT NULL, `dosage` TEXT NOT NULL, `frequency` TEXT NOT NULL, `quantity` INTEGER NOT NULL, `startDate` INTEGER NOT NULL, `durationMonths` INTEGER NOT NULL, `scheduledTime` INTEGER NOT NULL, `isTaken` INTEGER NOT NULL, `lastTakenTimestamp` INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `water_logs` (`userId` TEXT NOT NULL, `date` TEXT NOT NULL, `amount` INTEGER NOT NULL, PRIMARY KEY(`userId`, `date`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `water_settings` (`userId` TEXT NOT NULL, `dailyGoal` INTEGER NOT NULL, `quickAddAmount` INTEGER NOT NULL, `reminderInterval` INTEGER NOT NULL, PRIMARY KEY(`userId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `step_logs` (`userId` TEXT NOT NULL, `date` TEXT NOT NULL, `count` INTEGER NOT NULL, PRIMARY KEY(`userId`, `date`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `step_settings` (`userId` TEXT NOT NULL, `dailyGoal` INTEGER NOT NULL, PRIMARY KEY(`userId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '7887ef46ccd6cc497159932a2de59bbb')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `medications`");
        db.execSQL("DROP TABLE IF EXISTS `water_logs`");
        db.execSQL("DROP TABLE IF EXISTS `water_settings`");
        db.execSQL("DROP TABLE IF EXISTS `step_logs`");
        db.execSQL("DROP TABLE IF EXISTS `step_settings`");
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
        final HashMap<String, TableInfo.Column> _columnsMedications = new HashMap<String, TableInfo.Column>(11);
        _columnsMedications.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("dosage", new TableInfo.Column("dosage", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("frequency", new TableInfo.Column("frequency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("quantity", new TableInfo.Column("quantity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("startDate", new TableInfo.Column("startDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("durationMonths", new TableInfo.Column("durationMonths", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("scheduledTime", new TableInfo.Column("scheduledTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("isTaken", new TableInfo.Column("isTaken", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedications.put("lastTakenTimestamp", new TableInfo.Column("lastTakenTimestamp", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMedications = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMedications = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMedications = new TableInfo("medications", _columnsMedications, _foreignKeysMedications, _indicesMedications);
        final TableInfo _existingMedications = TableInfo.read(db, "medications");
        if (!_infoMedications.equals(_existingMedications)) {
          return new RoomOpenHelper.ValidationResult(false, "medications(com.example.remed.data.Medication).\n"
                  + " Expected:\n" + _infoMedications + "\n"
                  + " Found:\n" + _existingMedications);
        }
        final HashMap<String, TableInfo.Column> _columnsWaterLogs = new HashMap<String, TableInfo.Column>(3);
        _columnsWaterLogs.put("userId", new TableInfo.Column("userId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWaterLogs.put("date", new TableInfo.Column("date", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWaterLogs.put("amount", new TableInfo.Column("amount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWaterLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWaterLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWaterLogs = new TableInfo("water_logs", _columnsWaterLogs, _foreignKeysWaterLogs, _indicesWaterLogs);
        final TableInfo _existingWaterLogs = TableInfo.read(db, "water_logs");
        if (!_infoWaterLogs.equals(_existingWaterLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "water_logs(com.example.remed.data.WaterLog).\n"
                  + " Expected:\n" + _infoWaterLogs + "\n"
                  + " Found:\n" + _existingWaterLogs);
        }
        final HashMap<String, TableInfo.Column> _columnsWaterSettings = new HashMap<String, TableInfo.Column>(4);
        _columnsWaterSettings.put("userId", new TableInfo.Column("userId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWaterSettings.put("dailyGoal", new TableInfo.Column("dailyGoal", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWaterSettings.put("quickAddAmount", new TableInfo.Column("quickAddAmount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWaterSettings.put("reminderInterval", new TableInfo.Column("reminderInterval", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWaterSettings = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWaterSettings = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWaterSettings = new TableInfo("water_settings", _columnsWaterSettings, _foreignKeysWaterSettings, _indicesWaterSettings);
        final TableInfo _existingWaterSettings = TableInfo.read(db, "water_settings");
        if (!_infoWaterSettings.equals(_existingWaterSettings)) {
          return new RoomOpenHelper.ValidationResult(false, "water_settings(com.example.remed.data.WaterSettings).\n"
                  + " Expected:\n" + _infoWaterSettings + "\n"
                  + " Found:\n" + _existingWaterSettings);
        }
        final HashMap<String, TableInfo.Column> _columnsStepLogs = new HashMap<String, TableInfo.Column>(3);
        _columnsStepLogs.put("userId", new TableInfo.Column("userId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStepLogs.put("date", new TableInfo.Column("date", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStepLogs.put("count", new TableInfo.Column("count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStepLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesStepLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoStepLogs = new TableInfo("step_logs", _columnsStepLogs, _foreignKeysStepLogs, _indicesStepLogs);
        final TableInfo _existingStepLogs = TableInfo.read(db, "step_logs");
        if (!_infoStepLogs.equals(_existingStepLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "step_logs(com.example.remed.data.StepLog).\n"
                  + " Expected:\n" + _infoStepLogs + "\n"
                  + " Found:\n" + _existingStepLogs);
        }
        final HashMap<String, TableInfo.Column> _columnsStepSettings = new HashMap<String, TableInfo.Column>(2);
        _columnsStepSettings.put("userId", new TableInfo.Column("userId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStepSettings.put("dailyGoal", new TableInfo.Column("dailyGoal", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStepSettings = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesStepSettings = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoStepSettings = new TableInfo("step_settings", _columnsStepSettings, _foreignKeysStepSettings, _indicesStepSettings);
        final TableInfo _existingStepSettings = TableInfo.read(db, "step_settings");
        if (!_infoStepSettings.equals(_existingStepSettings)) {
          return new RoomOpenHelper.ValidationResult(false, "step_settings(com.example.remed.data.StepSettings).\n"
                  + " Expected:\n" + _infoStepSettings + "\n"
                  + " Found:\n" + _existingStepSettings);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "7887ef46ccd6cc497159932a2de59bbb", "d1d9beb7167d3065a852d14f7da34cf5");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "medications","water_logs","water_settings","step_logs","step_settings");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `medications`");
      _db.execSQL("DELETE FROM `water_logs`");
      _db.execSQL("DELETE FROM `water_settings`");
      _db.execSQL("DELETE FROM `step_logs`");
      _db.execSQL("DELETE FROM `step_settings`");
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
    _typeConvertersMap.put(MedicationDao.class, MedicationDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(WaterDao.class, WaterDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(StepDao.class, StepDao_Impl.getRequiredConverters());
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
  public MedicationDao medicationDao() {
    if (_medicationDao != null) {
      return _medicationDao;
    } else {
      synchronized(this) {
        if(_medicationDao == null) {
          _medicationDao = new MedicationDao_Impl(this);
        }
        return _medicationDao;
      }
    }
  }

  @Override
  public WaterDao waterDao() {
    if (_waterDao != null) {
      return _waterDao;
    } else {
      synchronized(this) {
        if(_waterDao == null) {
          _waterDao = new WaterDao_Impl(this);
        }
        return _waterDao;
      }
    }
  }

  @Override
  public StepDao stepDao() {
    if (_stepDao != null) {
      return _stepDao;
    } else {
      synchronized(this) {
        if(_stepDao == null) {
          _stepDao = new StepDao_Impl(this);
        }
        return _stepDao;
      }
    }
  }
}
