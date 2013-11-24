package at.lnu.ass2.alarm;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import at.lnu.ass2.R;

public class AlarmClock extends Activity {

	private static final String TAG = AlarmClock.class.getSimpleName();
	private TextView timeDisplay;
	private ListView alarmListView;
	private Button setAlarmButton;
	private Handler handler = new Handler();
	private boolean timeUpdating = true;
	private AlarmAdapter adapter;
	private final int changeRequestCode = 1;
	private DataSourceAlarm alarmData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_main);

		timeDisplay = (TextView) findViewById(R.id.alarm_time_display);

		setAlarmButton = (Button) findViewById(R.id.alarm_setalarm_button);
		setAlarmButton.setOnClickListener(new SetAlarmClick());

		// restore(savedInstanceState);
		alarmData = new DataSourceAlarm(this);
		alarmData.open();
		List<Alarm> alarmList = alarmData.getAllAlarms();
		adapter = new AlarmAdapter(this, alarmList);

		alarmListView = (ListView) findViewById(R.id.alarm_alarm_list);

		alarmListView.setAdapter(adapter);
		registerForContextMenu(alarmListView);

		Thread thr = new Thread(null, time_work, "Time Display");
		thr.start();

	}

	@Override
	protected void onResume() {

		// refresh the adapter everytime on resume (in case we just changed/added)
		super.onResume();
		alarmData.open();
		if (adapter == null)
			adapter = new AlarmAdapter(this, alarmData.getAllAlarms());
		adapter.clear();
		adapter.addAll(alarmData.getAllAlarms());
	}

	private class SetAlarmClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			Log.d(TAG, "SetAlarmClick -");

			Intent intent = new Intent(AlarmClock.this, AlarmForm.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivityForResult(intent, 0);

		}

	}

	/** Called when the activity receives a results. */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent result) {
		if (resultCode == RESULT_OK) {
			Log.d(TAG, "result from alarm form was ok");

			Alarm alarm = (Alarm) result.getSerializableExtra("alarm");
			// updates the adapter
			adapter.add(alarm);
			adapter.notifyDataSetChanged();

		} else {
			Log.e(TAG, "result from alarm form was not ok");
		}
	}

	private class AlarmAdapter extends ArrayAdapter<Alarm> {

		public AlarmAdapter(Context context, List<Alarm> objects) {
			super(context, 0, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			Alarm alarm = getItem(position);

			convertView = getLayoutInflater().inflate(R.layout.alarm_list_row, parent, false);

			TextView tv = (TextView) convertView.findViewById(R.id.label);

			tv.setText(alarm.getTimeAsString());
			tv.setTextSize(16f);

			// convertView.setTag(tag);
			return tv;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.alarm_alarm_list) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

			String title = "Alarm " + adapter.getItem((info.position)).getTimeAsString();
			menu.setHeaderTitle(title);
			menu.add(0, 0, 0, "Cancel");
			menu.add(0, 1, 0, "Change");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		if (item.getItemId() == 0) { // cancel alarm
			Alarm al = adapter.getItem(info.position);

			Intent intent = new Intent("at.lnu.ass2.ALARM_BROADCAST");
			intent.putExtra("message", "The one-shot alarm has gone off");

			PendingIntent alarmIntent = PendingIntent
					.getBroadcast(this, al.getAlarmID(), intent, 0);
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

			am.cancel(alarmIntent);

			alarmData.open();
			alarmData.deleteAlarm(al);

			Log.d(TAG, "canceled alarm with id " + al.getAlarmID());

			adapter.remove(al);
			adapter.notifyDataSetChanged();
		} else if (item.getItemId() == 1) {
			Alarm al = adapter.getItem(info.position);

			Intent intent = new Intent(this, AlarmForm.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("alarm", al);
			startActivityForResult(intent, changeRequestCode);
		}
		return true;
	}

	private Runnable time_work = new Runnable() {
		public void run() {
			while (timeUpdating) {
				handler.post(time_update);
				SystemClock.sleep(5000);
			}
		}
	};

	private Runnable time_update = new Runnable() {
		public void run() {
			Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int min = cal.get(Calendar.MINUTE);
			int sec = cal.get(Calendar.SECOND);

			String secStr = "";
			// for normalisation
			// if(sec<5)secStr=""+05;
			// else if(sec>5&& sec<=10)secStr=""+10;
			// else if(sec>10 && sec <=15)secStr=""+15;
			// else if(sec>15 && sec <=20)secStr=""+20;
			// else if(sec>20 && sec <=25)secStr=""+25;
			// else if(sec>25 && sec <=30)secStr=""+30;
			// else if(sec>30 && sec <=35)secStr=""+35;
			// else if(sec>35 && sec <=40)secStr=""+40;
			// else if(sec>40 && sec <=45)secStr=""+45;
			// else if(sec>45 && sec <=50)secStr=""+50;
			// else if(sec>50 && sec <=59)secStr=""+55;

			if (sec < 10) {
				secStr += "0" + sec;
			} else
				secStr = "" + sec;

			String minStr = "";
			if (min < 10) {
				minStr += "0" + min;
			} else
				minStr = "" + min;

			String currTime = hour + ":" + minStr + ":" + secStr;

			timeDisplay.setText(currTime);
			// Log.d(TAG, "current time is: " + currTime);

		}
	};

}
