package at.lnu.ass2.alarm;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
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

public class AlarmForm extends Activity {
	private static final String TAG = AlarmForm.class.getSimpleName();

	private TimePicker timePicker;
	private Button oneShotButton;
	private TextView textView;
	private static int currAlarmID = 0;
	private boolean newAlarm = true;
	private Alarm updateAlarm;

	private DataSourceAlarm alarmData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_form);

		alarmData = new DataSourceAlarm(this);

		Intent intent = getIntent();
		updateAlarm = (Alarm) intent.getSerializableExtra("alarm");

		textView = (TextView) findViewById(R.id.alarm_setalarm_text);
		timePicker = (TimePicker) findViewById(R.id.alarm_timepicker);

		oneShotButton = (Button) findViewById(R.id.alarm_one_shot);
		oneShotButton.setOnClickListener(oneShotListener);

		if (updateAlarm != null) {
			Log.d(TAG, "received intent with an alarm extra");
			Calendar cal = updateAlarm.getCalendar();
			timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
			timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
			if(textView!=null)
				textView.setText("Change alarm: ");
			newAlarm = false;

			// Schedule the alarm
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

			Intent intent1 = new Intent("at.lnu.ass2.ALARM_BROADCAST");
			intent1.putExtra("message", "The one-shot alarm has gone off");

			PendingIntent alarmIntent = PendingIntent.getBroadcast(AlarmForm.this,
					updateAlarm.getAlarmID(), intent1, 0);

			am.cancel(alarmIntent);

			Log.d(TAG, "cancelled old alarm. ready to set a new alarm");

		} else {
			Log.d(TAG, "received intent without any extras");
			if(textView!=null)
				textView.setText("Set new alarm: ");
			newAlarm = true;
		}
	}

	/**
	 * prepare the result intent for finishing the form
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

			// Schedule the alarm
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

			int hour = timePicker.getCurrentHour();
			int min = timePicker.getCurrentMinute();

			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, hour);
			calendar.set(Calendar.MINUTE, min);
			calendar.set(Calendar.SECOND, 0);

			alarmData.open();

			// new alarm in the form
			if (newAlarm == true || updateAlarm == null) {
				Log.d(TAG, "newAlarm is true");
				currAlarmID = retrieveCurrAlarmID();
				updateAlarm = alarmData.createAlarmEntry(currAlarmID, calendar);

				// add to alarm count for next created alarm
				currAlarmID++;
				storeCurrAlarmID();
			} else { // updating alarm
				Log.d(TAG, "newAlarm is false");

				updateAlarm.setCalendar(calendar);
				alarmData.updateAlarm(updateAlarm);

			}
			// create the alarm intent either with the old (but now updated) alarm, or a new one
			PendingIntent alarmIntent = createAlarmIntent(updateAlarm);

			long alarmTime = calendar.getTimeInMillis();

			Log.d(TAG, "alarmTime: " + alarmTime);
			Log.d(TAG, "currentTime: " + System.currentTimeMillis());

			am.set(AlarmManager.RTC_WAKEUP, updateAlarm.getCalendar().getTimeInMillis(),
					alarmIntent);

			// Tell the user about the alarm creation
			String msg = getResources().getString(R.string.alarm_toast_set) + hour + ":" + min;
			Toast.makeText(AlarmForm.this, msg, Toast.LENGTH_LONG).show();

			Log.d(TAG, "onClick ran, currAlarmID = " + currAlarmID);

			finishIntentRequest(updateAlarm);

		}

	};

	/**
	 * creates a PendingIntent to be received at the AlarmReceiver with a message and the alarm put
	 * as extra
	 * 
	 * @return
	 */
	private PendingIntent createAlarmIntent(Alarm alarm) {
		Intent intent = new Intent("at.lnu.ass2.ALARM_BROADCAST");
		intent.putExtra("message", getResources().getString(R.string.alarm_toast_goneoff));
		intent.putExtra("alarm", alarm);

		PendingIntent alarmIntent = PendingIntent.getBroadcast(AlarmForm.this, alarm.getAlarmID(),
				intent, 0);
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
