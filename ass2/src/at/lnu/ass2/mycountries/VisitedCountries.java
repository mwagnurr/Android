package at.lnu.ass2.mycountries;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import at.lnu.ass2.MainList;
import at.lnu.ass2.R;

public class VisitedCountries extends Activity {
	private ListView listView;
	private MyAdapter adapter;
	private DataSource datasource;
	private String orderColumn = DbHelper.COLUMN_YEAR;
	private List<CountryVisit> visits;

	private static final String TAG = VisitedCountries.class.getSimpleName();
	private static final String orderColumnPrefName = "orderColumn";
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mycountries);

		datasource = new DataSource(this);
		datasource.open();

		listView = (ListView) findViewById(R.id.listView);
		adapter = new MyAdapter(this, R.layout.mycountries_list_row);
		
		orderColumn = getColumnOrderSettings();
		
		// refilling list with all datasource values
//		visits = datasource.getAllCountryVisits(orderColumn, true);
//		adapter.addAll(visits);
//		listView.setAdapter(adapter);
		
		refreshAdapter();

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		registerForContextMenu(listView);
	}

	private static final int EDIT_ID = Menu.FIRST+2;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mycountries_action_menu, menu);
		menu.add(Menu.NONE, EDIT_ID, Menu.NONE, "Edit Settings")
		.setIcon(R.drawable.misc);
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_country:
			Intent intent = new Intent(this, MyCountriesForm.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivityForResult(intent, 0);
			return true;
		case R.id.sort_country:
			//switching orderColumn and storing to preferences
			this.orderColumn = DbHelper.COLUMN_COUNTRY;
			Log.d(TAG, "switched sorting to by country name");
			storeColumnOrderSettings(DbHelper.COLUMN_COUNTRY);
			
			//refresh adapter to instantly apply changes
			refreshAdapter();
			
			return true;
		case R.id.sort_year:
			//switching orderColumn and storing to preferences
			this.orderColumn = DbHelper.COLUMN_YEAR;
			Log.d(TAG, "switched sorting to by year");	
			storeColumnOrderSettings(DbHelper.COLUMN_YEAR);
			
			//refresh adapter to instantly apply changes
			refreshAdapter();
				
			return true;
			
		case EDIT_ID:
			startActivity(new Intent(this, MyCountriesPreferenceActivity.class));
			return(true);
			
		case android.R.id.home:
			Intent intent1 = new Intent(this, MainList.class);
			intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent1);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	


	/**
	 * refreshes the ListAdapter 
	 * (querying all data from database with the currently used sorting)
	 * Attention: orderColumn has to be set correctly before calling this method to get a right sorting
	 */
	private void refreshAdapter() {
		//refresh adapter to instantly apply changes
		adapter.clear();
		adapter.addAll(datasource.getAllCountryVisits(orderColumn, true));
		listView.setAdapter(adapter);
	}

	/**
	 * stores the columnName parameter as the current columnOrder for sorting
	 * in preferences
	 * @param columnName
	 */
	private void storeColumnOrderSettings(String columnName) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putString(VisitedCountries.orderColumnPrefName, columnName);
		editor.apply();
	}
	
	/**
	 * get the current columnOrder for sorting from the shared preferences
	 * @return
	 */
	private String getColumnOrderSettings() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);	
		return prefs.getString("orderColumn", DbHelper.COLUMN_YEAR);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.listView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle(visits.get(info.position).toString());
			menu.add(0, 0, 0, "Delete");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		if (item.getItemId() == 0) { // delete task
			CountryVisit cv = visits.get(info.position);
			datasource.deleteCountryVisit(cv);
			adapter.remove(cv);
		}
		return true;
	}

	@Override
	protected void onResume() {
		datasource.open();
		super.onResume();
		
		orderColumn = getColumnOrderSettings();	
	}

	@Override
	protected void onPause() {
		datasource.close();
		super.onPause();
	}

	/** Called when the activity receives a results. */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent result) {
		if (resultCode == RESULT_OK) {

			datasource.open();
			CountryVisit foo = datasource.createCountryVisit(
					result.getStringExtra(MyCountriesForm.INTENT_RESULT_NAME),
					result.getIntExtra(MyCountriesForm.INTENT_RESULT_YEAR, -1));

//			adapter.add(foo);
//			// adapter.notifyDataSetChanged();
//			listView.setAdapter(adapter);
			refreshAdapter();

		}
	}

	private class MyAdapter extends ArrayAdapter<CountryVisit> {

		public MyAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			/* Reuse super handling ==> A TextView from R.layout.list_item */
			TextView tv = (TextView) super.getView(position, convertView,
					parent);

			/* Find corresponding entry */
			CountryVisit cv = getItem(position);

			String str = String.valueOf(cv.getYear()) + " " + cv.getName();
			Log.d(TAG, "Adapter getView: " + str);
			tv.setText(str);
			return tv;
		}

	}
}
