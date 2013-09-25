package at.lnu.ass2.mp3;


import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

public class MusicService extends Service {
	public static final String ACTION_PLAY = "com.example.android.musicplayer.action.PLAY";
	public static final String ACTION_PAUSE = "com.example.android.musicplayer.action.PAUSE";
	public static final String ACTION_STOP = "com.example.android.musicplayer.action.STOP";
	public static final String ACTION_SKIP = "com.example.android.musicplayer.action.SKIP";
	public static final String ACTION_REWIND = "com.example.android.musicplayer.action.REWIND";
	public static final String ACTION_URL = "com.example.android.musicplayer.action.URL";



	private NotificationManager notifMan;

	private int NOTIFICATION = 1;
	
	private static final String TAG = MusicService.class.getSimpleName();
	
	private Player player;
	
	private final IBinder binder = new MusicBinder();
	
	
	
	private MusicManager musicMan;

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate() start");
		super.onCreate();
		
		musicMan = new MusicManager(getContentResolver());

		// Remember that to use this, we have to declare the
		// android.permission.WAKE_LOCK
		// permission in AndroidManifest.xml.
		//mediaPlayer.setWakeMode(getApplicationContext(),
		//		PowerManager.PARTIAL_WAKE_LOCK);
		
		//<uses-permission android:name="android.permission.WAKE_LOCK" />

		/*
		 * // Display a notification about us starting. We put an icon in the
		 * status bar. showNotification();
		 */

		
		
		//TODO start foreground service with notification
		
		//MAY TODO - Audio Focus and ducking
		
		/*
		 * String songName;
// assign the song name to songName
PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
Notification notification = new Notification();
notification.tickerText = text;
notification.icon = R.drawable.play0;
notification.flags |= Notification.FLAG_ONGOING_EVENT;
notification.setLatestEventInfo(getApplicationContext(), "MusicPlayerSample",
                "Playing: " + songName, pi);
startForeground(NOTIFICATION_ID, notification);
		 */
		Log.d(TAG, "onCreate() finished");
	}
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Log.d(TAG, "onStartCommand(...) flags: " + flags + ", startId: " + startId);
    	//return Service.START_STICKY;
    	return Service.START_NOT_STICKY;  
    }

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG, "onBind received ");
		return binder;
		//TODO thread safeness in IBinder
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		//notifMan.cancel(NOTIFICATION);

		// Tell the user we stopped.
		Toast.makeText(this, "service stopped I guess lawl", Toast.LENGTH_SHORT)
				.show();
		
//		mediaPlayer.stop();
//		mediaPlayer.release();
//		mediaPlayer = null;
		
		stopForeground(true);
		
		Log.d(TAG, "service stopped!");
	}
	
	public void startPlaying(boolean keepPlaying){
		Log.d(TAG, "startPlaying called");
		
		if(player==null){
			player = new Player(keepPlaying);
			player.start();
		}else{
			Log.d(TAG, "Player already instantinized");
			player.destroyMediaPlayer();
			player = new Player(keepPlaying);
			player.start();
			
		}
		
	}

	
	/**
	 * player thread that plays music via an mediaplayer instance until its keepPlaying is set to false
	 * @author Wagi
	 *
	 */
	private class Player extends Thread implements OnCompletionListener{
		//nested TAG for debugging
		private final String TAG = MusicService.TAG + ": " + Player.class.getSimpleName();
		
		private MediaPlayer mediaPlayer;
		
		boolean keepPlaying = false;

		
		public Player(){		
			
			mediaPlayer = new MediaPlayer();
			Log.d(TAG, "Player thread created");
		}
		public Player(boolean keepPlaying){		
			
			this.keepPlaying = keepPlaying;
			mediaPlayer = new MediaPlayer();
			Log.d(TAG, "Player thread created");
		}
		
		public void setKeepPlaying(boolean keepPlaying){
			this.keepPlaying = keepPlaying;
		}
		
		public boolean getKeepPlaying(){
			return keepPlaying;
		}
		
		public void pauseOrResume(){
			if (mediaPlayer.isPlaying()){
				mediaPlayer.pause(); 
				Log.d(TAG, "media player paused");
			}
			else{
				mediaPlayer.start(); 
				Log.d(TAG, "media player started again");
			}
		}
		private void play(Song song) {
			Log.d(TAG,"play called: " + song);
			if (song == null){
				Log.e(TAG, "song is null, cant play");
				return;
			}
			try {
				if (mediaPlayer.isPlaying())
					mediaPlayer.stop(); // Stop current song.

				mediaPlayer.reset();
				mediaPlayer.setDataSource(MusicService.this, Uri.parse(song.getPath())); //set Song to play
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION); //select aoudio stream
				mediaPlayer.prepare(); //prepare resource synchron (since seperate thread anyway)
				mediaPlayer.setOnCompletionListener(this);
//						new OnCompletionListener() //handler onDone
//				{
//					@Override
//					public void onCompletion(MediaPlayer mp)
//					{
//						Log.d(TAG, " - onCompletion (Listener) - completed song");
//						play(song);
//					}
//				});
				mediaPlayer.start(); //play!
				Log.d(TAG, "media player started to play");

			} catch (Exception e) {
				Toast.makeText(MusicService.this, "TODO exception playing song",
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {

			Log.d(TAG, "Player runs");
			Song song = musicMan.getNextRandomSong();
			play(song);


			
			// MusicService.this.stopSelf();
		}
		
		public void destroyMediaPlayer(){
			Log.i(TAG, "player thread gets stopped manually");
			onThreadDestroy();
		}

		private void onThreadDestroy() {
			if(mediaPlayer==null){
				Log.d(TAG, "mediaplayer already null");
				return;
			}
			if (mediaPlayer.isPlaying())mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
			keepPlaying = false;
			Log.i(TAG, "Stopped and released MediaPlayer");
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			Log.d(TAG, " - onCompletion (Listener) - completed song in thread");
			if(keepPlaying ==true){
				Song next = musicMan.getNextRandomSong();
				play(next);
			}else{
				Log.d(TAG,
						"Player stopped playing. Thread going to stop and releasing MediaPlayer");

				onThreadDestroy();
			}
		}
		
	}

	public class MusicBinder extends Binder {
		MusicService getService() {
			return MusicService.this;
		}
	}

}
