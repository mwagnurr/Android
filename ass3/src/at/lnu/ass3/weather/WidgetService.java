package at.lnu.ass3.weather;

import java.util.Iterator;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;
import android.widget.RemoteViews;

public class WidgetService extends Service {
	private static final String TAG = WidgetService.class.getSimpleName();

	private SparseArray<CityEntity> widgetCities = new SparseArray<CityEntity>();
	private SparseArray<WeatherForecast> widgetForecasts = new SparseArray<WeatherForecast>();

	private final IBinder binder = new WidgetServiceBinder();

	private Updater updater;

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

	public void retrieveWeatherReport(int appWidgetId, WeatherWidget callback, RemoteViews remoteViews) {
		Log.d(TAG, "running Updater");
		updater = new Updater(appWidgetId, getCityEntity(appWidgetId), callback, remoteViews);
		updater.run();
	}
	
	public WeatherForecast getLastWeatherForecast(int appWidgetId){
		return widgetForecasts.get(appWidgetId);
	}

	private class Updater extends Thread {
		private final String TAG = WidgetService.TAG + ": " + Updater.class.getSimpleName();
		private CityEntity city;
		private int appWidgetId;
		private WeatherWidget callback;
		private RemoteViews remoteViews;

		public Updater(int appWidgetId, CityEntity city, WeatherWidget callback, RemoteViews remoteViews) {
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

			Log.d(TAG, "retrieved.");
			if (report != null) {
				Log.d(TAG, "report != null");

				Iterator<WeatherForecast> iter = report.iterator();
				if (iter.hasNext()) {
					WeatherForecast first = iter.next();
					widgetForecasts.put(appWidgetId, first);
					Log.d(TAG, "replaced old forecast for widget: " + appWidgetId);
					Log.d(TAG, "new forecast: " + first.toString());
					
					
					
					
//					Intent in = new Intent(context, WeatherWidget.class);
//					String action = "FINISHED_FORECAST";
//					in.putExtra("city", city);
//					in.putExtra("id", appWidgetId);
//					in.putExtra("forecast", first);
//					in.setAction(action);
//					WidgetService.this.sendBroadcast(in);
					
					Log.d(TAG, "sent intent back with result");
				} else {
					Log.e(TAG, "report is empty");
				}
			} else {
				Log.e(TAG, "report == null; nothing happens");
			}
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
