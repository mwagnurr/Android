package at.lnu.ass2.alarm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
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
import at.lnu.ass2.mycountries.CountryVisit;




public class AlarmClock extends Activity {
	
	private static final String TAG = AlarmClock.class.getSimpleName();
	private TextView timeDisplay;
	private ListView alarmListView;
	private Button setAlarmButton;

	private Handler handler = new Handler();
	
	private boolean timeUpdating = true;
	
	private NotificationManager notifManager;

	private int NOTIFICATION_ID = 2;
	
	private ArrayList<Alarm> alarms = new ArrayList<Alarm>();
	
	private AlarmAdapter adapter;
	
	private final int changeRequestCode = 1;
	 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_main);
		
		timeDisplay = (TextView) findViewById(R.id.alarm_time_display);
		//timePicker = (TimePicker) findViewById(R.id.alarm_timepicker);
		
//		oneShotButton = (Button) findViewById(R.id.alarm_one_shot);
//		oneShotButton.setOnClickListener(oneShotListener);
//		repeatedButton = (Button) findViewById(R.id.alarm_start_repeating);
		setAlarmButton = (Button) findViewById(R.id.alarm_setalarm_button);
		setAlarmButton.setOnClickListener(new SetAlarmClick());
		
		restore(savedInstanceState);
		
		adapter = new AlarmAdapter(this, alarms);
		alarmListView = (ListView) findViewById(R.id.alarm_alarm_list);

		alarmListView.setAdapter(adapter);
		registerForContextMenu(alarmListView);
		
		Thread thr = new Thread(null, time_work, "Time Display");
        thr.start();
		
	}
	
	@SuppressWarnings("unchecked")
	private void restore(Bundle savedInstanceState) {
	    if (savedInstanceState != null) {
	        alarms = (ArrayList<Alarm>) savedInstanceState.getSerializable("alarms"); 
	    }
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    outState.putSerializable("alarms", alarms);
	}
	
	private class SetAlarmClick implements OnClickListener{

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
	protected void onActivityResult(int requestCode, int resultCode,
			Intent result) {
		if (resultCode == RESULT_OK) {
			Log.d(TAG, "result from alarm form was ok");
			
			Alarm alarm = (Alarm) result.getSerializableExtra("alarm");
			alarms.add(alarm);
			//adapter.add(alarm);
			adapter.notifyDataSetChanged();
			
			
//			CountryVisit foo = datasource.createCountryVisit(
//					result.getStringExtra(MyCountriesForm.INTENT_RESULT_NAME),
//					result.getIntExtra(MyCountriesForm.INTENT_RESULT_YEAR, -1));
//
////			adapter.add(foo);
////			// adapter.notifyDataSetChanged();
////			listView.setAdapter(adapter);
//			refreshAdapter();

		}else{
			Log.e(TAG, "result from alarm form was not ok");
		}
	}

	
	private class AlarmAdapter extends ArrayAdapter<Alarm>{
		
		public AlarmAdapter(Context context, List<Alarm> objects) {
			super(context, 0, objects);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			Alarm alarm = getItem(position);

			convertView = getLayoutInflater().inflate(R.layout.alarm_list_row, parent,
					false);

			TextView tv = (TextView) convertView.findViewById(R.id.label);

			tv.setText(alarm.getTimeAsString());
			
			//convertView.setTag(tag);
			return tv;
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
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
			//TODO cancel alarm
			adapter.remove(al);
		}else if (item.getItemId() == 1) {
			Alarm al = adapter.getItem(info.position);
			
			Intent intent = new Intent(this, AlarmForm.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("alarm", al);
			startActivityForResult(intent, changeRequestCode);
		}
		return true;
	}

	private void startNotification() {
		Notification.Builder builder = new Notification.Builder(this);
		builder.setSmallIcon(R.drawable.alarmclock)
				.setTicker("Alarm went off!").setAutoCancel(false);

		builder.setContentTitle(
				getResources().getString(R.string.music_app_name))
				.setContentInfo(
						getResources().getString(R.string.music_notif_click));

		builder.setContentText("Alarm 20:20");

		Intent intent = new Intent(this, AlarmClock.class); // Notification
		// intent
		PendingIntent notifIntent = PendingIntent.getActivity(this, 0, intent,
				0);
		builder.setContentIntent(notifIntent);

		Notification notification = builder.build();
		notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notifManager.notify(NOTIFICATION_ID, notification);

	}

	

	 
	 Runnable time_work = new Runnable(){
		 public void run() {
			 while(timeUpdating){
				 handler.post(time_update);
				 SystemClock.sleep(5000);		 
			 }
		 }
	 };
	 
	 Runnable time_update = new Runnable(){
		 public void run() {  
			 Calendar cal = Calendar.getInstance();
			 int hour = cal.get(Calendar.HOUR_OF_DAY);
			 int min = cal.get(Calendar.MINUTE);
			 int sec = cal.get(Calendar.SECOND);
			 
//			 if(sec<5)sec=5;
//			 else if(sec>5&& sec<10)sec=10;
//			 else if(sec>10 && sec <15)sec=15;
//			 else if(sec>15 && sec <20)sec=20;
//			 else if(sec>20 && sec <25)sec=25;
//			 else if(sec>25 && sec <30)sec=30;
			 
			 String currTime = hour + ":" + min + ":" + sec;
			 
			 timeDisplay.setText(currTime);
			 //Log.d(TAG, "current time is: " + currTime);

	        }
	 };
	 
	 
}
