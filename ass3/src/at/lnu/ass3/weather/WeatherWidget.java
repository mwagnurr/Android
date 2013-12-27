package at.lnu.ass3.weather;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import at.lnu.ass3.R;

public class WeatherWidget extends AppWidgetProvider {
	private static final String TAG = WeatherWidget.class.getSimpleName();

	private WidgetService widgetService = null;
	
	public void updateWidget(Context context){
		
	}

	public void update(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG, "updateWidget method called");

		Intent intent = new Intent(context, WidgetService.class);
		context.startService(intent);
		Log.d(TAG, "started the widget service (if not already running)");

		context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
		Log.d(TAG, "bound the widget service");

		final int n = appWidgetIds.length;
		for (int i = 0; i < n; i++) {
			int appWidgetId = appWidgetIds[i];
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);

			// registering onClickListener
			Intent clickIntent = new Intent(context, VaxjoWeather.class);

			// get the with the widgetID associated cityEntity from the service
			CityEntity currCity = widgetService.getCityEntity(i);
			//views.setImageViewResource(R.id.weather_widget_icon, srcId);
			views.setTextViewText(R.id.weather_widget_city, currCity.getName());
			widgetService.retrieveWeatherReport(i, this, views);
			
			
			/**
			 * WeatherForecast wf = getItem(position);

			// filling row with weather forecast
			ImageView icon = (ImageView) row.findViewById(R.id.weather_icon);
			int weatherCode = wf.getWeatherCode(); // TODO check code range
			int resID = getResources().getIdentifier("n" + weatherCode, "drawable",
					getPackageName());
			icon.setImageResource(resID);
			 */
			clickIntent.putExtra("city", currCity);
			

			// clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			// clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});

			// PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId,
			// clickIntent,
			// PendingIntent.FLAG_UPDATE_CURRENT);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId,
					clickIntent, 0);

			views.setOnClickPendingIntent(R.id.weather_widget_mainview, pendingIntent);

			appWidgetManager.updateAppWidget(appWidgetId, views);
		}

		/*
		 * Intent intent = new Intent(this, MusicPlayer.class); // Notification // intent
		 * PendingIntent notifIntent = PendingIntent.getActivity(this, 0, intent, 0);
		 * builder.setContentIntent(notifIntent);
		 */

		context.unbindService(connection);

		Log.d(TAG, "finished update (unbound widget service)");
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG, "onUpdate method called: " + Arrays.toString(appWidgetIds));

		update(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		Log.d(TAG, "onReceive method called: " + intent.getAction());

		if (intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {

			ComponentName thisWidget = new ComponentName(context, WeatherWidget.class);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
			update(context, appWidgetManager, appWidgetIds);
		}else if (intent.getAction().equals("FINISHED_FORECAST")){
			
			Log.d(TAG, "");
			
			
		}
	}

	private ServiceConnection connection = new ServiceConnection() {
		// @Override // Called when connection is made
		public void onServiceConnected(ComponentName cName, IBinder binder) {
			widgetService = ((WidgetService.WidgetServiceBinder) binder).getService();
		}

		// @Override //
		public void onServiceDisconnected(ComponentName cName) {
			widgetService = null;
		}
	};
	
	public interface WeatherCallBackListener{
		
	}

}
