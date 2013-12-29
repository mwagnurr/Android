package at.lnu.ass3.weather;

import android.app.ActionBar;
import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import at.lnu.ass3.MainList;
import at.lnu.ass3.R;

public class CityList extends ListActivity {
	private static final String TAG = CityList.class.getSimpleName();

	private final CityEntity[] cities2 = new CityEntity[4];
	private ArrayAdapter<CityEntity> adapter;

	private boolean widgetConfiguration = false;
	int appWidgetId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// init hard coded city list (for simple solution)
		cities2[0] = new CityEntity("Sweden", "Kronoberg", "Växjö");
		cities2[1] = new CityEntity("Sweden", "Stockholm", "Stockholm");
		cities2[2] = new CityEntity("Austria", "Steiermark", "Veitsch");
		cities2[3] = new CityEntity("Denmark", "Hovedstaden", "Copenhagen");

		adapter = new ArrayAdapter<CityEntity>(this, R.layout.weather_city_list_row, cities2);

		setListAdapter(adapter);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle != null) {

			appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			Log.d(TAG, "bundle was not null, appWidgetID = " + appWidgetId);

			Log.d(TAG, "bundle size is " + bundle.size());

			if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
				Log.e(TAG, "appWidgetId was invalid");
				finish();
			}

			widgetConfiguration = true;
		} else {
			Log.d(TAG, "bundle was null, -> no configuration");
			widgetConfiguration = false;
		}

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		registerForContextMenu(getListView());

		Log.d(TAG, "onCreate finished");
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		CityEntity city = (CityEntity) getListView().getItemAtPosition(position);

		Log.d(TAG, "clicked on list an selected: " + city);

		if (widgetConfiguration) {
			Log.d(TAG, "Widget Configuration");

			Intent intent = new Intent(CityList.this, WidgetService.class);
			intent.putExtra(WidgetService.SERVICE_INTENT_COMMAND_EXTRA, WidgetService.WIDGET_INIT);
			intent.putExtra("city", city);
			intent.putExtra("appWidgetId", appWidgetId);

			this.startService(intent);

			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			setResult(RESULT_OK, resultValue);

			finish();

		} else {
			Intent intent = new Intent(CityList.this, VaxjoWeather.class);
			intent.putExtra("city", city);

			startActivity(intent);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent1 = new Intent(this, MainList.class);
			intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent1);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
