/**
 * VaxjoWeather.java
 * Created: May 9, 2010
 * Jonas Lundberg, LnU
 */

package at.lnu.ass3.weather;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import at.lnu.ass3.MainList;
import at.lnu.ass3.R;

/**
 * This is a first prototype for a weather app. It is currently only downloading
 * weather data for Växjö.
 * 
 * This activity downloads weather data and constructs a WeatherReport, a data
 * structure containing weather data for a number of periods ahead.
 * 
 * The WeatherHandler is a SAX parser for the weather reports (forecast.xml)
 * produced by www.yr.no. The handler constructs a WeatherReport containing meta
 * data for a given location (e.g. city, country, last updated, next update) and
 * a sequence of WeatherForecasts. Each WeatherForecast represents a forecast
 * (weather, rain, wind, etc) for a given time period.
 * 
 * The next task is to construct a list based GUI where each row displays the
 * weather data for a single period.
 * 
 * 
 * @author jlnmsi
 * 
 */

public class VaxjoWeather extends ListActivity {
	private InputStream input;
	private WeatherReport report = null;
	private WeatherAdapter adapter;
	private static final String[] periods = { "Night time", "Morning",
			"Day time", "Evening" };
	

	private static final String TAG = VaxjoWeather.class.getSimpleName();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			URL url = new URL("http://www.yr.no/sted/Sverige/Kronoberg/V%E4xj%F6/forecast.xml");
			CityEntity currCity = new CityEntity("Veitsch", "Steiermark", "�sterrike");
			if (checkInternetConnection()) {
				AsyncTask task = new WeatherRetriever().execute(currCity);
			}else{
				Toast.makeText(this,getResources().getString(R.string.weather_nointernet),Toast.LENGTH_SHORT).show();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		/* Setup ListAdapter */
		adapter = new WeatherAdapter(this);

		/* Configure this ListActivity */
		setListAdapter(adapter);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
	
	private boolean checkInternetConnection() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm != null) {
			NetworkInfo[] info = cm.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						Log.d(TAG, "connection to internet found");
						return true;
					}
			} else {
				Log.e(TAG, "no connection to internet found");
				return false;
			}

		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, MainList.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class WeatherAdapter extends ArrayAdapter<WeatherForecast> {

		public WeatherAdapter(Context context) {
			super(context, R.layout.weather_row);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row;
			if (convertView == null) { // Create new row view object
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.weather_row, parent, false);
			} else
				// reuse old row view to save time/battery
				row = convertView;

			WeatherForecast wf = getItem(position);

			// filling row with weather forecast
			ImageView icon = (ImageView) row.findViewById(R.id.weather_icon);
			int weatherCode = wf.getWeatherCode(); // TODO check code range
			int resID = getResources().getIdentifier("n" + weatherCode,
					"drawable", getPackageName());
			icon.setImageResource(resID);

			TextView desc = (TextView) row.findViewById(R.id.weather_desc);
			desc.setText(wf.getWeatherName());

			TextView rain = (TextView) row.findViewById(R.id.weather_rain);
			rain.setText(String.valueOf(wf.getRain()) + " "
					+ getResources().getString(R.string.vaxjo_rain_unit));

			TextView temp = (TextView) row.findViewById(R.id.weather_temp);
			temp.setText(String.valueOf(wf.getTemp())
					+ getResources().getString(R.string.vaxjo_temp_unit));

			TextView time = (TextView) row.findViewById(R.id.weather_time);
			time.setText(wf.getStartHHMM() + " - " + wf.getEndHHMM());

			TextView date = (TextView) row.findViewById(R.id.weather_date);
			date.setText(wf.getStartYYMMDD());

			TextView wind = (TextView) row.findViewById(R.id.weather_wind);
			wind.setText(wf.getWindDirectionName());

			TextView windSpeed = (TextView) row
					.findViewById(R.id.weather_windspeed);
			windSpeed.setText(wf.getWindSpeedName());

			TextView period = (TextView) row.findViewById(R.id.weather_period);
			if (wf.getPeriodCode() >= 0 && wf.getPeriodCode() < 4)
				period.setText(periods[wf.getPeriodCode()]);
			else {
				System.out.println("received wrong period code");
				period.setText("");
			}
			return row;
		}
	}

	private void PrintReportToConsole() {
		if (this.report != null) {
			/* Print location meta data */
			System.out.println(report);

			/* Print forecasts */
			int count = 0;
			for (WeatherForecast forecast : report) {
				count++;
				System.out.println("Forecast " + count);
				System.out.println(forecast.toString());
			}
		} else {
			System.out.println("Weather report has not been loaded.");
		}
	}

	private class WeatherRetriever extends AsyncTask<CityEntity, Void, WeatherReport> {
		protected WeatherReport doInBackground(CityEntity... cities) {
			try {
				return WeatherHandler.getWeatherReport(cities[0]);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		protected void onProgressUpdate(Void... progress) {

		}

		protected void onPostExecute(WeatherReport result) {
			report = result;
			PrintReportToConsole();

			Iterator<WeatherForecast> iter = result.iterator();

			// fill the ListAdapter with the Weather Forecasts
			while (iter.hasNext()) {

				WeatherForecast curr = iter.next();
				adapter.add(curr);
				System.out.println("adding curr: " + curr.getPeriodCode()
						+ " start: " + curr.getStartYYMMDD() + " end: "
						+ curr.getEndYYMMDD());
			}
			setListAdapter(adapter);
		}
	}
}