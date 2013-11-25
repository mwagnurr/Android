package at.lnu.ass2.alarm;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import at.lnu.ass2.R;

public class AlarmResult extends Activity {
	private static final String TAG = AlarmResult.class.getSimpleName();
	private Button stopAlarm;
	private MediaPlayer mediaPlayer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_result);

		stopAlarm = (Button) findViewById(R.id.alarm_result_button_stop);
		stopAlarm.setOnClickListener(new StopClick());
		
		mediaPlayer = MediaPlayer.create(this, R.raw.annoying_alarmclock);
		mediaPlayer.start();
	}

	private class StopClick implements OnClickListener {

		@Override
		public void onClick(View v) {

			Log.d(TAG, "stop button got clicked. Stopping media player");
			mediaPlayer.stop();

			Alarm alarm = (Alarm) AlarmResult.this.getIntent().getSerializableExtra("alarm");

			if (alarm == null) {
				Log.e(TAG, "Alarm was null: should have been the alarm that just went off!");

			} else {
				Log.d(TAG, "Alarm received with intent, deleting from database");
				
				DataSourceAlarm alarmData = new DataSourceAlarm(AlarmResult.this);
				alarmData.open();
				alarmData.deleteAlarm(alarm);
			}
			finish();

		}

	}
	

}
