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
	private NotificationManager notifManager;

	private int NOTIFICATION_ID = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_result);

		stopAlarm = (Button) findViewById(R.id.alarm_result_button);
		stopAlarm.setOnClickListener(new StopClick());

		mediaPlayer = MediaPlayer.create(this, R.raw.annoying_alarmclock);
		mediaPlayer.start();
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

		Intent intent = new Intent(this, MusicPlayer.class); // Notification
		// intent
		PendingIntent notifIntent = PendingIntent.getActivity(this, 0, intent,
				0);
		builder.setContentIntent(notifIntent);

		Notification notification = builder.build();
		notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notifManager.notify(NOTIFICATION_ID, notification);

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
