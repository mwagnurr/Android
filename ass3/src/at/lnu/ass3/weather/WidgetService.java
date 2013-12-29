package at.lnu.ass3.weather;

import java.util.Iterator;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.widget.RemoteViews;
import at.lnu.ass3.R;

public class WidgetService extends Service {
	private static final String TAG = WidgetService.class.getSimpleName();

	private SparseArray<CityEntity> widgetCities = new SparseArray<CityEntity>();
	private SparseArray<WeatherForecast> widgetForecasts = new SparseArray<WeatherForecast>();

	private final IBinder binder = new WidgetServiceBinder();

	private Updater updater;

	private static final String PREF_KEY_CITY = "city";

	public static final String SERVICE_INTENT_COMMAND_EXTRA = "command";
	public static final int WIDGET_INIT = 0;
	public static final int WIDGET_UPDATE = 1;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStart() received");

		int command = intent.getIntExtra(SERVICE_INTENT_COMMAND_EXTRA, -1);

		switch (command) {
		case WIDGET_INIT: {
			Log.d(TAG, "command int was init");

			int appWidgetId = intent.getIntExtra("appWidgetId", -1);
			Log.d(TAG, "appWidgetId = " + appWidgetId);
			if (appWidgetId == -1)
				Log.e(TAG, "appWidgetId value = -1 , didn't find the real value apparently");

			CityEntity city = (CityEntity) intent.getSerializableExtra("city");

			Log.d(TAG, "received city = " + city);
			addCityEntity(appWidgetId, city);

			Intent updateWidget = new Intent(getApplicationContext(), WeatherWidget.class);
			updateWidget.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			int[] ids = { appWidgetId };
			updateWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
			sendBroadcast(updateWidget);
			break;
		}
		case WIDGET_UPDATE: {
			Log.d(TAG, "command int was update");

			int appWidgetId = intent.getIntExtra("appWidgetId", -1);
			Log.d(TAG, "appWidgetId = " + appWidgetId);
			if (appWidgetId == -1)
				Log.e(TAG, "appWidgetId value = -1 , didn't find the real value apparently");

			retrieveWeatherReport(appWidgetId);
			break;
		}
		case -1: {
			Log.e(TAG, "error: command int was missing or wrong");
			break;
		}
		default: {
			Log.e(TAG, "error: unknown command int");
			break;
		}

		}

		// Build the widget update for today
		// RemoteViews updateViews = buildUpdate(this);

		return START_STICKY;

	}

	public void addCityEntity(int appWidgetId, CityEntity city) {
		// if (widgetCities.get(appWidgetId) != null) {
		// Log.d(TAG, "appWidgetId is already stored");
		// return;
		// }
		// widgetCities.put(appWidgetId, city);

		/**
		 * SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		 * SharedPreferences.Editor editor = prefs.edit();
		 * 
		 * editor.putString(VisitedCountries.orderColumnPrefName, columnName); editor.apply();
		 */

		SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
		prefs.putString(PREF_KEY_CITY + appWidgetId, city.getFullCityName());
		prefs.apply();

		Log.d(TAG, "stored city: " + city.getFullCityName() + " as: " + PREF_KEY_CITY + appWidgetId);
	}

	public CityEntity getCityEntity(int appWidgetId) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String cityFull = prefs.getString(PREF_KEY_CITY + appWidgetId, null);

		if (cityFull == null) {
			Log.e(TAG, "no city stored in pref ( " + PREF_KEY_CITY + appWidgetId + "); return null");
			return null;
		}
		Log.d(TAG, "got from pref city: " + cityFull);
		return new CityEntity(cityFull);

		// return widgetCities.get(appWidgetId);
	}

	public void retrieveWeatherReport(int appWidgetId) {
		Log.d(TAG, "running Updater");
		updater = new Updater(appWidgetId, getCityEntity(appWidgetId));
		updater.start();
	}

	public WeatherForecast getLastWeatherForecast(int appWidgetId) {
		return widgetForecasts.get(appWidgetId);
	}

	private class Updater extends Thread {
		private final String TAG = WidgetService.TAG + ": " + Updater.class.getSimpleName();
		private CityEntity city;
		private int appWidgetId;

		public Updater(int appWidgetId, CityEntity city) {
			this.city = city;
			this.appWidgetId = appWidgetId;

		}

		@Override
		public void run() {
			Log.d(TAG, "Starting Updater - retrieving weather report for widget: " + appWidgetId
					+ ", city: " + city);
			WeatherReport report = WeatherHandler.getWeatherReport(city);
			WeatherForecast firstForecast = null;

			if (city == null) {
				Log.e(TAG, "unexpected error in Updater: city is null; aborting");
				return;
			}

			Log.d(TAG, city + " data retrieved.");
			Context context = WidgetService.this;
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);

			if (report != null) {
				Log.d(TAG, "report != null");

				Iterator<WeatherForecast> iter = report.iterator();

				if (iter.hasNext()) {
					firstForecast = iter.next();
					widgetForecasts.put(appWidgetId, firstForecast);
					Log.d(TAG, "forecast for widget: " + appWidgetId);
					Log.d(TAG, "new forecast: " + firstForecast.toString());

					int weatherCode = firstForecast.getWeatherCode();
					int resID = getResources().getIdentifier("n" + weatherCode, "drawable",
							getPackageName());

					views.setImageViewResource(R.id.weather_widget_icon, resID);

					views.setTextViewText(R.id.weather_widget_temp, firstForecast.getTemp()
							+ getResources().getString(R.string.vaxjo_temp_unit));

					views.setTextViewText(R.id.weather_widget_wind, firstForecast.getWindSpeed()
							+ getResources().getString(R.string.vaxjo_windspeed_unit));

				} else {
					Log.e(TAG, "report is empty");
				}

			} else {
				Log.e(TAG, "report == null;");
			}

			// registering onClickListener
			Intent clickIntent = new Intent(context, VaxjoWeather.class);
			clickIntent.putExtra("city", city);

			views.setTextViewText(R.id.weather_widget_city, city.getName());

			if (firstForecast != null) {
				// views.setImageViewResource(R.id.weather_widget_icon, srcId);
				views.setTextViewText(R.id.weather_widget_temp, firstForecast.getTemp() + "°C");
			}

			PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId,
					clickIntent, 0);

			views.setOnClickPendingIntent(R.id.weather_widget_mainview, pendingIntent);

			AppWidgetManager manager = AppWidgetManager.getInstance(WidgetService.this);
			manager.updateAppWidget(appWidgetId, views);

			Log.d(TAG, "updated AppWidget remote views");

		}

	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG, "onBind received ");
		return binder;
	}

	public class WidgetServiceBinder extends Binder {
		WidgetService getService() {
			return WidgetService.this;
		}
	}
}
