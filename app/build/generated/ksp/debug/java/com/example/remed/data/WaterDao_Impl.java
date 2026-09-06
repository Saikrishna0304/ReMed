package com.example.remed.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
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
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class WaterDao_Impl implements WaterDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WaterLog> __insertionAdapterOfWaterLog;

  private final EntityInsertionAdapter<WaterSettings> __insertionAdapterOfWaterSettings;

  private final EntityDeletionOrUpdateAdapter<WaterLog> __updateAdapterOfWaterLog;

  public WaterDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWaterLog = new EntityInsertionAdapter<WaterLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `water_logs` (`userId`,`date`,`amount`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WaterLog entity) {
        statement.bindString(1, entity.getUserId());
        statement.bindString(2, entity.getDate());
        statement.bindLong(3, entity.getAmount());
      }
    };
    this.__insertionAdapterOfWaterSettings = new EntityInsertionAdapter<WaterSettings>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `water_settings` (`userId`,`dailyGoal`,`quickAddAmount`,`reminderInterval`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WaterSettings entity) {
        statement.bindString(1, entity.getUserId());
        statement.bindLong(2, entity.getDailyGoal());
        statement.bindLong(3, entity.getQuickAddAmount());
        statement.bindLong(4, entity.getReminderInterval());
      }
    };
    this.__updateAdapterOfWaterLog = new EntityDeletionOrUpdateAdapter<WaterLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `water_logs` SET `userId` = ?,`date` = ?,`amount` = ? WHERE `userId` = ? AND `date` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WaterLog entity) {
        statement.bindString(1, entity.getUserId());
        statement.bindString(2, entity.getDate());
        statement.bindLong(3, entity.getAmount());
        statement.bindString(4, entity.getUserId());
        statement.bindString(5, entity.getDate());
      }
    };
  }

  @Override
  public Object insertLog(final WaterLog log, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWaterLog.insert(log);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSettings(final WaterSettings settings,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWaterSettings.insert(settings);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLog(final WaterLog log, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfWaterLog.handle(log);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<WaterLog> getLogForDate(final String userId, final String date) {
    final String _sql = "SELECT * FROM water_logs WHERE userId = ? AND date = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    _argIndex = 2;
    _statement.bindString(_argIndex, date);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"water_logs"}, new Callable<WaterLog>() {
      @Override
      @Nullable
      public WaterLog call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final WaterLog _result;
          if (_cursor.moveToFirst()) {
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final int _tmpAmount;
            _tmpAmount = _cursor.getInt(_cursorIndexOfAmount);
            _result = new WaterLog(_tmpUserId,_tmpDate,_tmpAmount);
          } else {
            _result = null;
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
  public Object getLog(final String userId, final String date,
      final Continuation<? super WaterLog> $completion) {
    final String _sql = "SELECT * FROM water_logs WHERE userId = ? AND date = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    _argIndex = 2;
    _statement.bindString(_argIndex, date);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<WaterLog>() {
      @Override
      @Nullable
      public WaterLog call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final WaterLog _result;
          if (_cursor.moveToFirst()) {
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final int _tmpAmount;
            _tmpAmount = _cursor.getInt(_cursorIndexOfAmount);
            _result = new WaterLog(_tmpUserId,_tmpDate,_tmpAmount);
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
  public Flow<WaterSettings> getSettings(final String userId) {
    final String _sql = "SELECT * FROM water_settings WHERE userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"water_settings"}, new Callable<WaterSettings>() {
      @Override
      @Nullable
      public WaterSettings call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDailyGoal = CursorUtil.getColumnIndexOrThrow(_cursor, "dailyGoal");
          final int _cursorIndexOfQuickAddAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "quickAddAmount");
          final int _cursorIndexOfReminderInterval = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderInterval");
          final WaterSettings _result;
          if (_cursor.moveToFirst()) {
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final int _tmpDailyGoal;
            _tmpDailyGoal = _cursor.getInt(_cursorIndexOfDailyGoal);
            final int _tmpQuickAddAmount;
            _tmpQuickAddAmount = _cursor.getInt(_cursorIndexOfQuickAddAmount);
            final long _tmpReminderInterval;
            _tmpReminderInterval = _cursor.getLong(_cursorIndexOfReminderInterval);
            _result = new WaterSettings(_tmpUserId,_tmpDailyGoal,_tmpQuickAddAmount,_tmpReminderInterval);
          } else {
            _result = null;
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
  public Object getSettingsSync(final String userId,
      final Continuation<? super WaterSettings> $completion) {
    final String _sql = "SELECT * FROM water_settings WHERE userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<WaterSettings>() {
      @Override
      @Nullable
      public WaterSettings call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDailyGoal = CursorUtil.getColumnIndexOrThrow(_cursor, "dailyGoal");
          final int _cursorIndexOfQuickAddAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "quickAddAmount");
          final int _cursorIndexOfReminderInterval = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderInterval");
          final WaterSettings _result;
          if (_cursor.moveToFirst()) {
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final int _tmpDailyGoal;
            _tmpDailyGoal = _cursor.getInt(_cursorIndexOfDailyGoal);
            final int _tmpQuickAddAmount;
            _tmpQuickAddAmount = _cursor.getInt(_cursorIndexOfQuickAddAmount);
            final long _tmpReminderInterval;
            _tmpReminderInterval = _cursor.getLong(_cursorIndexOfReminderInterval);
            _result = new WaterSettings(_tmpUserId,_tmpDailyGoal,_tmpQuickAddAmount,_tmpReminderInterval);
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
