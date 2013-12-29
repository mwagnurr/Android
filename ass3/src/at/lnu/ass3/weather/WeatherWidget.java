package at.lnu.ass3.weather;

import java.util.Arrays;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import at.lnu.ass3.R;

public class WeatherWidget extends AppWidgetProvider {
	private static final String TAG = WeatherWidget.class.getSimpleName();

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG, "onUpdate method called: " + Arrays.toString(appWidgetIds));

		update(context, appWidgetManager, appWidgetIds);
	}

	/**
	 * processes the update command
	 * 
	 * @param context
	 * @param appWidgetManager
	 * @param appWidgetIds
	 */
	private void update(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG, "updateWidget method called");

		boolean internetConnection = checkInternetConnection(context);

		if (!internetConnection) {
			Log.d(TAG, "no internet connection; do not update");
			Toast.makeText(context, context.getResources().getString(R.string.weather_nointernet),
					Toast.LENGTH_SHORT).show();
		}

		final int n = appWidgetIds.length;
		for (int i = 0; i < n; i++) {
			int appWidgetId = appWidgetIds[i];

			// only start updater-service when not in airPlaneMode
			if (internetConnection) {
				Intent intent = new Intent(context, WidgetService.class);
				intent.putExtra(WidgetService.SERVICE_INTENT_COMMAND_EXTRA,
						WidgetService.WIDGET_UPDATE);
				intent.putExtra("appWidgetId", appWidgetId);
				context.startService(intent);
				Log.d(TAG, "sent start Service intent for appWidgetId " + appWidgetId);
			}
			RemoteViews views = createDefaultRemoteView(context, appWidgetId);
			appWidgetManager.updateAppWidget(appWidgetId, views);

		}
		Log.d(TAG, "finished update");
	}

	/**
	 * creates the default remote view for the widget (update button click intent / basic layout)
	 * 
	 * @param context
	 * @param appWidgetId
	 * @return
	 */
	private RemoteViews createDefaultRemoteView(Context context, int appWidgetId) {
		// creating initial widget view
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);

		CityEntity city = retrieveCityEntity(context, appWidgetId);
		if (city != null)
			views.setTextViewText(R.id.weather_widget_city, city.getName());

		Intent updateClick = new Intent(context, WeatherWidget.class);
		updateClick.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		int[] ids = { appWidgetId };
		updateClick.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
		PendingIntent updatePen = PendingIntent.getBroadcast(context, appWidgetId, updateClick, 0);
		views.setOnClickPendingIntent(R.id.weather_widget_update_button, updatePen);
		return views;
	}

	/**
	 * retrieves city entity for appWidgetId from shared preferences
	 * 
	 * @param context
	 * @param appWidgetId
	 * @return
	 */
	private CityEntity retrieveCityEntity(Context context, int appWidgetId) {
		CityEntity city = null;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String cityFull = prefs.getString(WidgetService.PREF_KEY_CITY + appWidgetId, null);

		if (cityFull != null) {
			city = new CityEntity(cityFull);

		} else {
			Log.e(TAG, "no city stored in pref ( " + WidgetService.PREF_KEY_CITY + appWidgetId
					+ ")");
		}
		return city;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		Log.d(TAG, "onReceive method called: " + intent.getAction());
	}

	/**
	 * checks if phone has internet connection; returns true if yes, false otherwise
	 * 
	 * @param context
	 * @return
	 */
	private boolean checkInternetConnection(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

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

}
