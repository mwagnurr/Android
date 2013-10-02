package at.lnu.ass2.alarm;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;
import at.lnu.ass2.R;

public class AlarmForm extends Activity {
	private static final String TAG = AlarmForm.class.getSimpleName();
	
	private TimePicker timePicker;
	private Button oneShotButton;
	private Button repeatedButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_form);

		timePicker = (TimePicker) findViewById(R.id.alarm_timepicker);
		
		oneShotButton = (Button) findViewById(R.id.alarm_one_shot);
		oneShotButton.setOnClickListener(oneShotListener);
		repeatedButton = (Button) findViewById(R.id.alarm_start_repeating);
		
//		/* Assign listener to button */
//		Button button = (Button) findViewById(R.id.mycountry_done_button);
//		button.setOnClickListener(new ButtonClick());
//
//		createAlerts();
	}
	
	
	 /**
	 * 
	 */
	private void finishIntentRequest() {
		Intent reply = new Intent();
		setResult(RESULT_OK, reply);

		finish();
	}


	private OnClickListener oneShotListener = new OnClickListener() {
	     public void onClick(View v) {
	    	 
	    	 Log.d(TAG, "oneShot button clicked");
	         //Intent intent = new Intent(getResources().getString((R.string.alarm_broadcast))); //not working
	    	 Intent intent = new Intent("at.lnu.ass2.ALARM_BROADCAST");
	 		 intent.putExtra("message", "The one-shot alarm has gone off");
	 		 
	 		 
	         PendingIntent alarmIntent = PendingIntent.getBroadcast(AlarmForm.this,0, intent, 0);

	         
	         // Schedule the alarm

	         AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
	         
	         int hour = timePicker.getCurrentHour();
	         int min = timePicker.getCurrentMinute();
	         
	         Calendar calendar = Calendar.getInstance();
	         calendar.set(Calendar.HOUR_OF_DAY, hour);
	         calendar.set(Calendar.MINUTE, min);
	         calendar.set(Calendar.SECOND, 0);
	         
	         
	         long alarmTime = calendar.getTimeInMillis();
	         
	         Log.d(TAG, "alarmTime: " + alarmTime);
	         Log.d(TAG, "currentTime: " +System.currentTimeMillis());
	         
	         am.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);

	         // Tell the user about what we did.
	         String msg = "Alarm set for " + hour +":" +min;
	         Toast.makeText(AlarmForm.this, msg,Toast.LENGTH_LONG).show();
	         
	         finishIntentRequest();
	     }
	 };
	 
	 
}
