package at.lnu.ass2.alarm;

import java.util.Calendar;

import org.w3c.dom.Text;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import at.lnu.ass2.R;
import at.lnu.ass2.mp3.MusicPlayer;
import at.lnu.ass2.mycountries.DbHelper;
import at.lnu.ass2.mycountries.VisitedCountries;

public class AlarmForm extends Activity {
	private static final String TAG = AlarmForm.class.getSimpleName();

	private TimePicker timePicker;
	private Button oneShotButton;
	private Button repeatedButton;
	private TextView textView;
	private static int currAlarmID = 0;
	private boolean newAlarm = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_form);

		Intent intent = getIntent();
		Alarm alarm = (Alarm) intent.getSerializableExtra("alarm");

		textView = (TextView) findViewById(R.id.alarm_setalarm_text);
		timePicker = (TimePicker) findViewById(R.id.alarm_timepicker);

		oneShotButton = (Button) findViewById(R.id.alarm_one_shot);
		oneShotButton.setOnClickListener(oneShotListener);
		repeatedButton = (Button) findViewById(R.id.alarm_start_repeating);

		if (alarm != null) {
			Log.d(TAG, "received intent with an alarm extra");
			Calendar cal = alarm.getCalendar();
			timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
			timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
			textView.setText("Change alarm: ");
			newAlarm = false;

			// Schedule the alarm
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

			Intent intent1 = new Intent("at.lnu.ass2.ALARM_BROADCAST");
			intent1.putExtra("message", "The one-shot alarm has gone off");

			PendingIntent alarmIntent = PendingIntent.getBroadcast(AlarmForm.this,
					alarm.getAlarmID(), intent1, 0);

			am.cancel(alarmIntent);
			
			Log.d(TAG, "cancelled old alarm. ready to set a new alarm");

		} else {
			Log.d(TAG, "received intent without any extras");
			textView.setText("Set new alarm: ");
			newAlarm = true;
		}

		// /* Assign listener to button */
		// Button button = (Button) findViewById(R.id.mycountry_done_button);
		// button.setOnClickListener(new ButtonClick());
		//
		// createAlerts();
	}

	/**
	 * 
	 */
	private void finishIntentRequest(Alarm alarm) {
		Intent reply = new Intent();
		reply.putExtra("alarm", alarm);
		setResult(RESULT_OK, reply);

		finish();
	}

	private OnClickListener oneShotListener = new OnClickListener() {
		public void onClick(View v) {

			Log.d(TAG, "oneShot button clicked");

			if (!newAlarm) {
				Log.d(TAG, "newAlarm is false");

			}

			currAlarmID = retrieveCurrAlarmID();
			PendingIntent alarmIntent = createAlarmIntent(currAlarmID);

			// Schedule the alarm
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

			int hour = timePicker.getCurrentHour();
			int min = timePicker.getCurrentMinute();

			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, hour);
			calendar.set(Calendar.MINUTE, min);
			calendar.set(Calendar.SECOND, 0);

			Alarm newAlarm = new Alarm(currAlarmID, calendar);
			long alarmTime = calendar.getTimeInMillis();

			Log.d(TAG, "alarmTime: " + alarmTime);
			Log.d(TAG, "currentTime: " + System.currentTimeMillis());

			am.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);

			// Tell the user about what we did.
			String msg = "Alarm set for " + hour + ":" + min;
			Toast.makeText(AlarmForm.this, msg, Toast.LENGTH_LONG).show();

			currAlarmID++;
			storeCurrAlarmID();

			Log.d(TAG, "onClick ran, currAlarmID = " + currAlarmID);

			finishIntentRequest(newAlarm);

		}

	};

	/**
	 * @return
	 */
	private PendingIntent createAlarmIntent(int alarmID) {
		Intent intent = new Intent("at.lnu.ass2.ALARM_BROADCAST");
		intent.putExtra("message", "The one-shot alarm has gone off");

		PendingIntent alarmIntent = PendingIntent.getBroadcast(AlarmForm.this, alarmID, intent, 0);
		return alarmIntent;
	}

	/**
	 * 
	 */
	private void storeCurrAlarmID() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();

		editor.putInt("currAlarmID", currAlarmID);
		editor.apply();
	}

	/**
	 * 
	 */
	private int retrieveCurrAlarmID() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs.getInt("currAlarmID", 0);
	}

}
