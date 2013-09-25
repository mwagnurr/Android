package at.lnu.ass2.mycountries;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.SyncStateContract.Columns;
import android.util.Log;

public class DataSource {

	  // Database fields
	  private SQLiteDatabase database;
	  private DbHelper dbHelper;
	  private String[] allColumns = { DbHelper.COLUMN_ID,
			  DbHelper.COLUMN_COUNTRY, DbHelper.COLUMN_YEAR };
	  
	  private static final String TAG = DataSource.class.getSimpleName();

	  public DataSource(Context context) {
	    dbHelper = new DbHelper(context);
	  }

	  /**
	   * opens database connection
	   * @throws SQLException
	   */
	  public void open() throws SQLException {
		Log.d(TAG, "opened Databaseconnection");
	    database = dbHelper.getWritableDatabase();
	  }

	  public void close() {
	    dbHelper.close();
	  }
	  
	  public CountryVisit createCountryVisit(String country, int year){
		  ContentValues values = new ContentValues();
		  values.put(DbHelper.COLUMN_COUNTRY, country);
		  values.put(DbHelper.COLUMN_YEAR, year);
		  
		  long insertId = database.insert(DbHelper.TABLE_NAME, null, values);
		  Cursor cursor = database.query(DbHelper.TABLE_NAME,
			        allColumns, DbHelper.COLUMN_ID + " = " + insertId, null,
			        null, null, null);
		  cursor.moveToFirst();
		  CountryVisit cv = new CountryVisit(cursor.getLong(0), cursor.getString(1), cursor.getInt(2));
		  Log.d(TAG, "created: " + cv);
		  cursor.close();
		  return cv;
	  }

	public CountryVisit getCountryVisit(long id) {
		String restrict = DbHelper.COLUMN_ID + "=" + id;
		Cursor cursor = database.query(true, DbHelper.TABLE_NAME, allColumns,
				restrict, null, null, null, null, null);

		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();

			CountryVisit cv = new CountryVisit(cursor.getLong(0),
					cursor.getString(1), cursor.getInt(2));

			cursor.close();
			return cv;
		} else {
			return null;
		}
	}
	
	/**
	 * unsorted result of all country visits
	 * @return
	 */
	public List<CountryVisit> getAllCountryVisits() {
		List<CountryVisit> visits = new ArrayList<CountryVisit>();

		Cursor cursor = database.query(DbHelper.TABLE_NAME, allColumns, null,
				null, null, null, null);

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			CountryVisit cv = new CountryVisit(cursor.getLong(0),
					cursor.getString(1), cursor.getInt(2));
			Log.d(TAG, "getting: " + cv);
			visits.add(cv);
			cursor.moveToNext();
		}
		cursor.close();
		return visits;
	}
	
	/**
	 * sorted results of all country visits
	 * @param orderColumn column by which it should be ordered
	 * @param orderTypeAsc true for ascending, false for descending
	 * @return
	 */
	public List<CountryVisit> getAllCountryVisits(String orderColumn, boolean orderTypeAsc) {
		List<CountryVisit> visits = new ArrayList<CountryVisit>();
		
		String orderType =" ";
		if(orderTypeAsc==true)orderType+="ASC";
		else orderType+="DESC";

		Cursor cursor = database.query(DbHelper.TABLE_NAME, allColumns, null,
				null, null, null, orderColumn + orderType);

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			CountryVisit cv = new CountryVisit(cursor.getLong(0),
					cursor.getString(1), cursor.getInt(2));
			Log.d(TAG, "getting: " + cv);
			visits.add(cv);
			cursor.moveToNext();
		}
		cursor.close();
		return visits;
	}
	
	public void deleteCountryVisit(CountryVisit cv){
		long id = cv.getId();
		Log.d(TAG, "Country visit deleted with id: " + id);
		database.delete(DbHelper.TABLE_NAME, DbHelper.COLUMN_ID + " = " +id, null);
	}


/*
 * 
 *   public void deleteTask(TodoTask task) {
	    long id = task.getId();
	    System.out.println("Task deleted with id: " + id);
	    database.delete(DbHelper.TABLE_NAME, DbHelper.COLUMN_ID
	    		+ " = " + id, null);
	  }
 * 	  public List<TodoTask> getAllTasks() {
	    List<TodoTask> tasks = new ArrayList<TodoTask>();

	    Cursor cursor = database.query(DbHelper.TABLE_NAME,
	        allColumns, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      TodoTask task = cursorToTask(cursor);
	      tasks.add(task);
	      cursor.moveToNext();
	    }
	    // Make sure to close the cursor
	    cursor.close();
	    return tasks;
	  }
	  
	  public void deleteTask(TodoTask task) {
	    long id = task.getId();
	    System.out.println("Task deleted with id: " + id);
	    database.delete(DbHelper.TABLE_NAME, DbHelper.COLUMN_ID
	    		+ " = " + id, null);
	  }
	  
	  public TodoTask getTask(long taskId) {
		  String restrict = DbHelper.COLUMN_ID + "=" + taskId;
		  Cursor cursor = database.query(true, DbHelper.TABLE_NAME, allColumns, restrict, 
		    		                        null, null, null, null, null);
		  if (cursor != null && cursor.getCount() > 0) {
			  cursor.moveToFirst();
			  TodoTask task = cursorToTask(cursor);
			  return task;
		  }
		  // Make sure to close the cursor
		  cursor.close();
		  return null;
	  }
	  
	  public boolean updateTask(long taskId, String task) {
		  ContentValues args = new ContentValues();
		  args.put(DbHelper.COLUMN_TASK, task);

		  String restrict = DbHelper.COLUMN_ID + "=" + taskId;
		  return database.update(DbHelper.TABLE_NAME, args, restrict , null) > 0;
	  } 



	  private TodoTask cursorToTask(Cursor cursor) {
		  TodoTask task = new TodoTask();
		  task.setId(cursor.getLong(0));
		  task.setTask(cursor.getString(1));
		  return task;
	  }
	  
	  */
	} 
