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
import android.widget.RemoteViews;
import at.lnu.ass3.R;

public class WidgetService extends Service {
	private static final String TAG = WidgetService.class.getSimpleName();

	private final IBinder binder = new WidgetServiceBinder();

	private Updater updater;

	protected static final String PREF_KEY_CITY = "city";

	public static final String SERVICE_INTENT_COMMAND_EXTRA = "command";
	public static final int WIDGET_INIT = 0;
	public static final int WIDGET_UPDATE = 1;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStart() received");

		if (intent == null) {
			Log.e(TAG, "error, Intent is null");
			return START_STICKY;
		}
		int command = intent.getIntExtra(SERVICE_INTENT_COMMAND_EXTRA, -1);

		processIntentCommand(intent, command);

		return START_STICKY;

	}

	/**
	 * processes initialization and update commands
	 * 
	 * @param intent
	 * @param command
	 */
	private void processIntentCommand(Intent intent, int command) {
		switch (command) {
		case WIDGET_INIT: {
			Log.d(TAG, "command int was init");

			int appWidgetId = intent.getIntExtra("appWidgetId", -1);
			Log.d(TAG, "appWidgetId = " + appWidgetId);
			if (appWidgetId == -1)
				Log.e(TAG, "appWidgetId value = -1 , didn't find the real value apparently");

			CityEntity city = (CityEntity) intent.getSerializableExtra("city");

			Log.d(TAG, "received city = " + city);
			storeCityEntity(appWidgetId, city);

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
	}

	/**
	 * stores city entity in shared preferences for appWidgetId
	 * 
	 * @param appWidgetId
	 * @param city
	 */
	public void storeCityEntity(int appWidgetId, CityEntity city) {
		SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
		prefs.putString(PREF_KEY_CITY + appWidgetId, city.getFullCityName());
		prefs.apply();

		Log.d(TAG, "stored city: " + city.getFullCityName() + " as: " + PREF_KEY_CITY + appWidgetId);
	}

	/**
	 * gets the to the appWidgetId corresponding city entity from shared preferences
	 * 
	 * @param appWidgetId
	 * @return
	 */
	public CityEntity getCityEntity(int appWidgetId) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String cityFull = prefs.getString(PREF_KEY_CITY + appWidgetId, null);

		if (cityFull == null) {
			Log.e(TAG, "no city stored in pref ( " + PREF_KEY_CITY + appWidgetId + "); return null");
			return null;
		}
		Log.d(TAG, "got from pref city: " + cityFull);
		return new CityEntity(cityFull);
	}

	/**
	 * spawns a thread to retrieve the weather report for the widget with appWidgetId
	 * 
	 * @param appWidgetId
	 */
	public void retrieveWeatherReport(int appWidgetId) {
		Log.d(TAG, "running Updater");
		updater = new Updater(appWidgetId, getCityEntity(appWidgetId));
		updater.start();
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
					// get only first forecast
					firstForecast = iter.next();
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

			PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId,
					clickIntent, 0);

			views.setOnClickPendingIntent(R.id.weather_widget_mainview, pendingIntent);

			// finally updating all the views to the widget
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
