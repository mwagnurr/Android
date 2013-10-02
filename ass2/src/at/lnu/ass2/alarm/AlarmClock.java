package at.lnu.ass2.alarm;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import at.lnu.ass2.R;



public class AlarmClock extends Activity {
	
	private static final String TAG = AlarmClock.class.getSimpleName();
	private TextView timeDisplay;
	private ListView alarmDisplay;
	private Button setAlarmButton;

	private Handler handler = new Handler();
	
	private boolean timeUpdating = true;

	 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_main);
		
		timeDisplay = (TextView) findViewById(R.id.alarm_time_display);
		alarmDisplay = (ListView) findViewById(R.id.alarm_alarm_display);
		//timePicker = (TimePicker) findViewById(R.id.alarm_timepicker);
		
//		oneShotButton = (Button) findViewById(R.id.alarm_one_shot);
//		oneShotButton.setOnClickListener(oneShotListener);
//		repeatedButton = (Button) findViewById(R.id.alarm_start_repeating);
		setAlarmButton = (Button) findViewById(R.id.alarm_setalarm_button);
		setAlarmButton.setOnClickListener(new SetAlarmClick());
		
		Thread thr = new Thread(null, time_work, "Time Display");
        thr.start();
		
	}
	
	class SetAlarmClick implements OnClickListener{

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
