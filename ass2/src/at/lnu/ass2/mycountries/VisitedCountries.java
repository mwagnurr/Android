package at.lnu.ass2.mycountries;

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
	private boolean orderAsc;

	private static final String TAG = VisitedCountries.class.getSimpleName();
	private static final String orderColumnPrefName = "orderColumn";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mycountries);

		View layout = findViewById(R.id.mycountries_layout);
		changeBackgroundColor(layout);
		datasource = new DataSource(this);
		datasource.open();

		listView = (ListView) findViewById(R.id.listView);
		adapter = new MyAdapter(this, R.layout.mycountries_list_row);

		//get settings
		orderColumn = getColumnOrderSettings();
		orderAsc = getAscOrderSettings();

		refreshAdapter();

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		registerForContextMenu(listView);
	}

	private static final int EDIT_ID = Menu.FIRST + 2;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mycountries_action_menu, menu);
		menu.add(Menu.NONE, EDIT_ID, Menu.NONE, "Edit Settings").setIcon(R.drawable.misc);

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
			// switching orderColumn and storing to preferences
			this.orderColumn = DbHelper.COLUMN_COUNTRY;
			Log.d(TAG, "switched sorting to by country name");
			storeColumnOrderSettings(DbHelper.COLUMN_COUNTRY);

			// refresh adapter to instantly apply changes
			refreshAdapter();

			return true;
		case R.id.sort_year:
			// switching orderColumn and storing to preferences
			this.orderColumn = DbHelper.COLUMN_YEAR;
			Log.d(TAG, "switched sorting to by year");
			storeColumnOrderSettings(DbHelper.COLUMN_YEAR);

			// refresh adapter to instantly apply changes
			refreshAdapter();

			return true;
		case R.id.settings:
			startActivity(new Intent(this, MyCountriesPreferenceActivity.class));
			return (true);

		case EDIT_ID:
			startActivity(new Intent(this, MyCountriesPreferenceActivity.class));
			return (true);

		case android.R.id.home:
			Intent intent1 = new Intent(this, MainList.class);
			intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent1);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.listView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle(adapter.getItem((info.position)).toPresentableString());
			menu.add(0, 0, 0, "Edit");
			menu.add(0, 1, 1, "Delete");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();

		if (item.getItemId() == 0) { // edit task
			CountryVisit cv = adapter.getItem((info.position));
			Log.d(TAG, "context menu selected edit for " + cv.getName());

			// send CountryVisit to edit to form
			Intent intent = new Intent(this, MyCountriesForm.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(MyCountriesForm.INTENT_START_CV, cv);
			startActivityForResult(intent, 1); // start with requestcode 1 for editing

		} else if (item.getItemId() == 1) { // delete task
			CountryVisit cv = adapter.getItem((info.position));
			Log.d(TAG, "context menu selected delete for " + cv.getName());
			datasource.deleteCountryVisit(cv);
			adapter.remove(cv);
		}
		return true;
	}

	/**
	 * Called when the activity receives a results. Expects two types of request codes: 0 for new
	 * created CVs and 1 for edited CVs
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent result) {
		if (resultCode == RESULT_OK) {
			Log.d(TAG, "received requestcode: " + requestCode);
			if (requestCode == 0) {

				datasource.open();
				CountryVisit foo = datasource.createCountryVisit(
						result.getStringExtra(MyCountriesForm.INTENT_RESULT_NAME),
						result.getIntExtra(MyCountriesForm.INTENT_RESULT_YEAR, -1));

				if (foo == null)
					Log.e(TAG, "couldnt create countryvisit");

				refreshAdapter();
			} else if (requestCode == 1) {

				CountryVisit editedCV = (CountryVisit) result
						.getSerializableExtra(MyCountriesForm.INTENT_RESULT_EDITCV);
				datasource.open();

				if (!datasource.updateGoal(editedCV))
					Log.e(TAG, "couldnt update countryvisit: " + editedCV);

				refreshAdapter();
			}

		}
	}

	@Override
	protected void onResume() {
		datasource.open();
		super.onResume();

		orderColumn = getColumnOrderSettings();
		orderAsc = getAscOrderSettings();

		// update the background color of the views
		View layout = findViewById(R.id.mycountries_layout);
		changeBackgroundColor(layout);
		refreshAdapter();
	}

	@Override
	protected void onPause() {
		datasource.close();
		super.onPause();
	}

	/**
	 * refreshes the ListAdapter (querying all data from database with the currently used sorting)
	 * Attention: orderColumn has to be set correctly before calling this method to get a right
	 * sorting
	 */
	private void refreshAdapter() {
		// refresh adapter to instantly apply changes
		adapter.clear();
		adapter.addAll(datasource.getAllCountryVisits(orderColumn, orderAsc));
		listView.setAdapter(adapter);
	}

	/**
	 * stores the columnName parameter as the current columnOrder for sorting in preferences
	 * 
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
	 * 
	 * @return
	 */
	private String getColumnOrderSettings() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs.getString("orderColumn", DbHelper.COLUMN_YEAR);
	}

	/**
	 * get the current boolean for ascending order for sorting from the shared preferences
	 * 
	 * @return
	 */
	private boolean getAscOrderSettings() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean bla = prefs.getBoolean(
				getResources().getString(R.string.pref_mycountries_sort_key), true);
		return bla;
	}

	/**
	 * changes background color of the view depending on the current settings
	 * 
	 * @param view
	 */
	private void changeBackgroundColor(View view) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		String backColorStr = prefs.getString(
				getResources().getString(R.string.pref_mycountries_background_key), "0");

		int backColor = Integer.parseInt(backColorStr);

		if (backColor == 1) {
			view.setBackgroundResource(R.color.dark_blue);
		} else if (backColor == 2) {
			view.setBackgroundResource(R.color.dark_green);
		} else {
			view.setBackgroundResource(R.color.solid_black);
		}
	}

	/**
	 * custom adapter to format correct display output TextView in the list
	 * 
	 */
	private class MyAdapter extends ArrayAdapter<CountryVisit> {

		public MyAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			/* Reuse super handling ==> A TextView from R.layout.list_item */
			TextView tv = (TextView) super.getView(position, convertView, parent);

			/* Find corresponding entry */
			CountryVisit cv = getItem(position);

			String str = String.valueOf(cv.getYear()) + " " + cv.getName();
			// Log.d(TAG, "Adapter getView: " + str);
			tv.setText(str);

			float textSize = getFontSizePreference();
			tv.setTextSize(textSize);

			changeBackgroundColor(tv);

			return tv;
		}

		/**
		 * returns the font size from shared preferences and parses it to float (scaled-pixel)
		 * 
		 * @return
		 */
		private float getFontSizePreference() {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(VisitedCountries.this);

			String textSizeStr = prefs.getString(
					getResources().getString(R.string.pref_mycountries_fontsize_key), "16.0");

			float textSize = Float.parseFloat(textSizeStr);
			return textSize;
		}

	}
}
