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

	public void update(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG, "updateWidget method called");

		final int n = appWidgetIds.length;
		for (int i = 0; i < n; i++) {
			int appWidgetId = appWidgetIds[i];

			Intent intent = new Intent(context, WidgetService.class);
			intent.putExtra(WidgetService.SERVICE_INTENT_COMMAND_EXTRA, WidgetService.WIDGET_UPDATE);
			intent.putExtra("appWidgetId", appWidgetId);
			context.startService(intent);
			Log.d(TAG, "sent start Service intent for appWidgetId " + appWidgetId);

			
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
			
			appWidgetManager.updateAppWidget(appWidgetId, views);
			/**
			 * WeatherForecast wf = getItem(position);
			 * 
			 * // filling row with weather forecast ImageView icon = (ImageView)
			 * row.findViewById(R.id.weather_icon); int weatherCode = wf.getWeatherCode(); // TODO
			 * check code range int resID = getResources().getIdentifier("n" + weatherCode,
			 * "drawable", getPackageName()); icon.setImageResource(resID);
			 */
			// clickIntent.putExtra("city", currCity);

			// clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			// clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});

			// PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId,
			// clickIntent,
			// PendingIntent.FLAG_UPDATE_CURRENT);
			// PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId,
			// clickIntent, 0);
			//
			// views.setOnClickPendingIntent(R.id.weather_widget_mainview, pendingIntent);

			
		}

		/*
		 * Intent intent = new Intent(this, MusicPlayer.class); // Notification // intent
		 * PendingIntent notifIntent = PendingIntent.getActivity(this, 0, intent, 0);
		 * builder.setContentIntent(notifIntent);
		 */

		Log.d(TAG, "finished update");
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
		} else if (intent.getAction().equals("FINISHED_FORECAST")) {

			Log.d(TAG, "");

		}
	}

}
