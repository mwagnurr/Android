package at.lnu.ass2.alarm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataSourceAlarm {

	// Database fields
	private SQLiteDatabase database;
	private DbHelper dbHelper;
	private String[] allColumns = { DbHelper.COLUMN_ID, DbHelper.COLUMN_ALARM_ID,
			DbHelper.COLUMN_TIME };

	private static final String TAG = DataSourceAlarm.class.getSimpleName();

	public DataSourceAlarm(Context context) {
		dbHelper = new DbHelper(context);
	}

	/**
	 * opens database connection
	 * 
	 * @throws SQLException
	 */
	public void open() throws SQLException {
		Log.d(TAG, "opened Databaseconnection");
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public Alarm createAlarmEntry(int alarmId, Calendar time) {

		ContentValues values = new ContentValues();
		values.put(DbHelper.COLUMN_ALARM_ID, alarmId);
		values.put(DbHelper.COLUMN_TIME, time.getTimeInMillis());

		long insertId = database.insert(DbHelper.TABLE_NAME, null, values);
		Cursor cursor = database.query(DbHelper.TABLE_NAME, allColumns, DbHelper.COLUMN_ID + " = "
				+ insertId, null, null, null, null);
		cursor.moveToFirst();

		Alarm alarm = cursorToAlarm(cursor);
		Log.d(TAG, "created: " + alarm);
		cursor.close();
		return alarm;
	}

	/**
	 * converts the cursor to a Alarm entity
	 * @param cursor
	 * @return
	 */
	private Alarm cursorToAlarm(Cursor cursor) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(cursor.getLong(2));
		int id = cursor.getInt(0);
		Alarm alarm = new Alarm(id, cursor.getInt(1), cal);
		return alarm;
	}

	/**
	 * retrieves all (active (if inactive got correctly deleted as they should)) alarms in database
	 * 
	 * @return
	 */
	public List<Alarm> getAllAlarms() {
		List<Alarm> alarmList = new ArrayList<Alarm>();

		Cursor cursor = database.query(DbHelper.TABLE_NAME, allColumns, null, null, null, null,
				null);

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			alarmList.add(cursorToAlarm(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		return alarmList;
	}

	public Alarm getAlarm(long id) {
		String restrict = DbHelper.COLUMN_ID + "=" + id;
		Cursor cursor = database.query(true, DbHelper.TABLE_NAME, allColumns, restrict, null, null,
				null, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			Alarm a = cursorToAlarm(cursor);
			cursor.close();
			return a;
		} else {
			return null;
		}
	}

	/**
	 * deleting alarms (especially for deleting inactive/cancelled ones)
	 * 
	 * @param alarm
	 */
	public void deleteAlarm(Alarm alarm) {
		long id = alarm.getId();
		Log.d(TAG, "Alarm deleted with id: " + id);
		int effectedRows = database.delete(DbHelper.TABLE_NAME, DbHelper.COLUMN_ID + " = " + id,
				null);

		if (effectedRows == 0) {
			Log.e(TAG, "no rows have been affected! nothing got deleted");
		}
	}

	public boolean updateAlarm(Alarm alarm) {
		ContentValues args = new ContentValues();
		args.put(DbHelper.COLUMN_ALARM_ID, alarm.getAlarmID());
		args.put(DbHelper.COLUMN_TIME, alarm.getCalendar().getTimeInMillis());
		return database.update(DbHelper.TABLE_NAME, args, DbHelper.COLUMN_ID + "=" + alarm.getId(),
				null) > 0;
	}

}
