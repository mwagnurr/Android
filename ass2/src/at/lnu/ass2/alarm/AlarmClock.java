package at.lnu.ass2.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import at.lnu.ass2.R;


public class AlarmClock extends Activity {
	
	private static final String TAG = AlarmClock.class.getSimpleName();
	private TextView timeDisplay;
	private TextView alarmDisplay;
	private TimePicker timePicker;
	private Button oneShotButton;
	private Button repeatedButton;
	
	 private OnClickListener oneShotListener = new OnClickListener() {
	     public void onClick(View v) {
	         Intent intent = new Intent("at.lnu.ass2.ALARM_BROADCAST");


	 		 intent.putExtra("message", "The one-shot alarm has gone off");
	         PendingIntent alarmIntent = PendingIntent.getBroadcast(AlarmClock.this,0, intent, 0);

	         // Schedule the alarm
	         long startTime = 3*1000 + System.currentTimeMillis();
	         AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
	         am.set(AlarmManager.RTC_WAKEUP, startTime, alarmIntent);

	         // Tell the user about what we did.
	         String msg = "One-shot alarm will go off in 3 seconds.";
	         Toast.makeText(AlarmClock.this, msg,Toast.LENGTH_LONG).show();
	     }
	 };
	 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_main);
		
		timeDisplay = (TextView) findViewById(R.id.alarm_time_display);
		alarmDisplay = (TextView) findViewById(R.id.alarm_alarm_display);
		timePicker = (TimePicker) findViewById(R.id.alarm_timepicker);
		
		oneShotButton = (Button) findViewById(R.id.alarm_one_shot);
		oneShotButton.setOnClickListener(oneShotListener);
		repeatedButton = (Button) findViewById(R.id.alarm_start_repeating);
		
	}
	

	 
	 
}
