package at.lnu.ass2.alarm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {

	public static final String TABLE_NAME = "alarm";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_ALARM_ID = "alarm_id";
	public static final String COLUMN_TIME = "time";
	

	private static final String DATABASE_NAME = "alarm.db";
	private static final int DATABASE_VERSION = 5;

    private static final String DATABASE_CREATE = "create table " + TABLE_NAME 
    		+ " (" + COLUMN_ID + " integer primary key autoincrement, "
    		+ COLUMN_ALARM_ID + " integer not null, "
            + COLUMN_TIME + " datetime not null);";
	
	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.w(DbHelper.class.getName(), "Upgrading database from version " 
	    		+ oldVersion + " to " + newVersion 
	    		+ ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
	}

}
