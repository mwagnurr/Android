package at.lnu.ass3.weather;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import at.lnu.ass3.MainList;
import at.lnu.ass3.R;

public class CityList extends ListActivity {
	private static final String TAG = CityList.class.getSimpleName();
	private ArrayAdapter<CityEntity> adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new ArrayAdapter<CityEntity>(this, R.layout.weather_city_list_row);
		setListAdapter(adapter);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		registerForContextMenu(getListView());

		Log.d(TAG, "onCreate finished");
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		CityEntity city = (CityEntity) getListView().getItemAtPosition(position);

		Log.d(TAG, "clicked on list an selected: " + city);
		// TODO send intent with city to start VaxjoWeather instance
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent result) {
		if (resultCode == RESULT_OK) {
			CityEntity city = new CityEntity(result.getStringExtra(CityForm.INTENT_RESULT_NAME),
					result.getStringExtra(CityForm.INTENT_RESULT_STATE),
					result.getStringExtra(CityForm.INTENT_RESULT_COUNTRY));

			adapter.add(city);
			adapter.notifyDataSetChanged();

			Log.d(TAG, "activity result received: " + city);

			// adapter.add(foo);
			// // adapter.notifyDataSetChanged();
			// listView.setAdapter(adapter);

		}
	}

	private static final int EDIT_ID = Menu.FIRST + 2;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.weather_citylist_action_menu, menu);
		menu.add(Menu.NONE, EDIT_ID, Menu.NONE, "Edit Settings").setIcon(R.drawable.ic_launcher);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_city:
			Intent intent = new Intent(this, CityForm.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivityForResult(intent, 0);
			return true;

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
			menu.setHeaderTitle(adapter.getItem((info.position)).getName());
			menu.add(0, 0, 0, "Remove");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		if (item.getItemId() == 0) { // delete task
			CityEntity ce = adapter.getItem((info.position));
			// datasource.deleteCountryVisit(ce);
			adapter.remove(ce);
		}
		return true;
	}

}
