package at.lnu.ass2.alarm;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import at.lnu.ass2.R;
import at.lnu.ass2.mp3.MusicPlayer;
import at.lnu.ass2.mp3.MusicService;

public class AlarmResult extends Activity {
	private static final String TAG = AlarmResult.class.getSimpleName();
	private Button stopAlarm;
	private MediaPlayer mediaPlayer;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_result);

		stopAlarm = (Button) findViewById(R.id.alarm_result_button);
		stopAlarm.setOnClickListener(new StopClick());

		mediaPlayer = MediaPlayer.create(this, R.raw.annoying_alarmclock);
		mediaPlayer.start();
	}



	private class StopClick implements OnClickListener {

		@Override
		public void onClick(View v) {

			Log.d(TAG, "stop button got clicked. Stopping media player");
			mediaPlayer.stop();
			finish();
		}

	}

}
