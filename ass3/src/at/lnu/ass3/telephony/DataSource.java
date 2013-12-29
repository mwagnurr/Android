package at.lnu.ass3.telephony;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataSource {

	// Database fields
	private SQLiteDatabase database;
	private DbHelper dbHelper;
	private String[] allColumns = { DbHelper.COLUMN_ID, DbHelper.COLUMN_PHONE_NUMBER };

	private static final String TAG = DataSource.class.getSimpleName();

	public DataSource(Context context) {
		dbHelper = new DbHelper(context);
	}

	/**
	 * opens database connection
	 * 
	 * @throws SQLException
	 */
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public CallEntity createCallEntity(String phoneNumber) {
		ContentValues values = new ContentValues();
		values.put(DbHelper.COLUMN_PHONE_NUMBER, phoneNumber);

		long insertId = database.insert(DbHelper.TABLE_NAME, null, values);
		Cursor cursor = database.query(DbHelper.TABLE_NAME, allColumns, DbHelper.COLUMN_ID + " = "
				+ insertId, null, null, null, null);
		cursor.moveToFirst();

		CallEntity call = cursorToCall(cursor);
		Log.d(TAG, "created: " + call);
		cursor.close();
		return call;
	}

	public List<CallEntity> getAllCalls() {
		List<CallEntity> callEntityList = new ArrayList<CallEntity>();

		Cursor cursor = database.query(DbHelper.TABLE_NAME, allColumns, null, null, null, null,
				null);

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			callEntityList.add(cursorToCall(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		return callEntityList;
	}

	private CallEntity cursorToCall(Cursor cursor) {

		int id = cursor.getInt(0);
		String phoneNumber = cursor.getString(1);

		CallEntity call = new CallEntity(id, phoneNumber);
		return call;
	}
}
