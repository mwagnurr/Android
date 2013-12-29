package at.lnu.ass3.weather;

import java.util.Iterator;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
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
		if (widgetCities.get(appWidgetId) != null) {
			Log.d(TAG, "appWidgetId is already stored");
			return;
		}
		widgetCities.put(appWidgetId, city);
	}

	public void changeCityEntity(int appWidgetId, CityEntity city) {
		// TODO
	}

	public CityEntity getCityEntity(int appWidgetId) {
		return widgetCities.get(appWidgetId);
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
		private WeatherWidget callback;
		private RemoteViews remoteViews;

		public Updater(int appWidgetId, CityEntity city) {
			this.city = city;
			this.appWidgetId = appWidgetId;
			this.callback = callback;
			this.remoteViews = remoteViews;
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

			Log.d(TAG, "retrieved.");
			if (report != null) {
				Log.d(TAG, "report != null");

				Iterator<WeatherForecast> iter = report.iterator();

				if (iter.hasNext()) {
					firstForecast = iter.next();
					widgetForecasts.put(appWidgetId, firstForecast);
					Log.d(TAG, "replaced old forecast for widget: " + appWidgetId);
					Log.d(TAG, "new forecast: " + firstForecast.toString());

					// Intent in = new Intent(context, WeatherWidget.class);
					// String action = "FINISHED_FORECAST";
					// in.putExtra("city", city);
					// in.putExtra("id", appWidgetId);
					// in.putExtra("forecast", first);
					// in.setAction(action);
					// WidgetService.this.sendBroadcast(in);

				} else {
					Log.e(TAG, "report is empty");
				}

			} else {
				Log.e(TAG, "report == null;");
			}

			Context context = WidgetService.this;
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);

			// registering onClickListener
			Intent clickIntent = new Intent(context, VaxjoWeather.class);
			clickIntent.putExtra("city", city);

			// views.setImageViewResource(R.id.weather_widget_icon, srcId);
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
