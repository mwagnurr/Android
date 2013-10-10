package at.lnu.ass3.weather;

import java.util.Arrays;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import at.lnu.ass3.R;

public class WeatherWidget extends AppWidgetProvider{
	private static final String TAG = WeatherWidget.class.getSimpleName();
	
	public void update(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Log.d(TAG, "updateTime(3) method called");
	
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
			
			String city = "Veitsch";
			views.setTextViewText(R.id.weather_city, city);
			
			//registering onClickListener
			Intent clickIntent = new Intent(context, VaxjoWeather.class);
			//clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			//clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});
			
//			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, clickIntent,
//					PendingIntent.FLAG_UPDATE_CURRENT);
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
	}
	
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Log.d(TAG, "onUpdate method called: " + Arrays.toString(appWidgetIds));

		update(context, appWidgetManager, appWidgetIds);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		Log.d(TAG, "onReceive method called: " + intent.getAction());
		
		if (intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)){
			
			ComponentName thisWidget = new ComponentName(context, WeatherWidget.class);
			AppWidgetManager appWidgetManager =	AppWidgetManager.getInstance(context);
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
			update(context, appWidgetManager, appWidgetIds);
		}
	}
	
}
