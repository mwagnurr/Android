package at.lnu.ass2.mp3;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
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
import android.view.View;
import android.widget.Toast;
import at.lnu.ass2.R;

public class MusicService extends Service {

	private NotificationManager notifManager;

	private int NOTIFICATION_ID = 1;
	
	private static final String TAG = MusicService.class.getSimpleName();
	
	private Player player;
	
	private final IBinder binder = new MusicBinder();
	
	/**
	 * State of the music player
	 *
	 */
    enum State {
        Stopped,
        Playing,
        Paused  
    };
    
    private State currentState = State.Stopped;
	
    public State getCurrentState(){
    	return currentState;
    }

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate() start");
		super.onCreate();
		
		buildNotification();

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
	
//	public void setMusicManager(MusicManager musicMan){
//		this.musicMan = musicMan;
//	}
	
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//    	Log.d(TAG, "onStartCommand(...) flags: " + flags + ", startId: " + startId);
//    	//return Service.START_STICKY;
//    	return Service.START_NOT_STICKY;  
//    }
	
	
	public void buildNotification(){
		/* 1. Setup Notification Builder */			
		Notification.Builder builder = new Notification.Builder(this); 
		
		/* 2. Configure Notification Alarm */
		builder.setSmallIcon(R.drawable.misc)
			.setWhen(System.currentTimeMillis())
			.setTicker("Test")
			.setAutoCancel(true);
			
			
		/* 3. Configure Drop-down Action */
		builder.setContentTitle("More information")
				.setContentText("Click to continue.")
				.setContentInfo("Click!");
		Intent intent = new Intent(this, MusicPlayer.class);   // Notification intent
		PendingIntent notifIntent = PendingIntent.getActivity(this, 0, intent, 0);
		builder.setContentIntent(notifIntent);
		
		/* 4. Create Notification and use Manager to launch it */
		Notification notification = builder.build();	
		String ns = Context.NOTIFICATION_SERVICE;
		notifManager = (NotificationManager) getSystemService(ns);
		notifManager.notify(NOTIFICATION_ID, notification);
	}


	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG, "onBind received ");
		return binder;
		//TODO thread safeness in IBinder
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "trying to destroy service");
		// Cancel the persistent notification.
		//notifMan.cancel(NOTIFICATION);

		// Tell the user we stopped.
		Toast.makeText(this, "service stopped I guess lawl", Toast.LENGTH_SHORT)
				.show();
		
		stopForeground(true);
		
		if (player != null) {
			player.destroyMediaPlayer();
			player = null;
		}
		
		Log.d(TAG, "service destroyed!");
	}
	
	public void startPlaying(List<Song> playList, boolean playListRepeat){
		Log.d(TAG, "startPlaying called");
		
		if(player==null){
			player = new Player(playList, playListRepeat);
			player.start();
		}else{
			Log.d(TAG, "Player already instantinized");
			player.destroyMediaPlayer();
			player = new Player(playList, playListRepeat);
			player.start();
			
		}
		
	}
	
	public void playNextSong(){
		if(player==null){
			Log.e(TAG, "currently no player playing!");
		}else{
			player.playNext();
		}
	}
	
	public void playPreviousSong(){
		if(player==null){
			Log.e(TAG, "currently no player playing!");
		}else{
			player.playPrevious();
		}
	}
	
	public void pauseOrResume(){
		Log.d(TAG, "pauseOrResume called");
		
		if(player==null){
			Log.e(TAG, "currently no player playing!");
		}else{
			try{
			player.pauseOrResume();
			}catch(IllegalStateException is){
				Log.e(TAG, "player currently in an illegal state, aborting");
			}
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
		
		boolean repeatPlayList = false;
		
		private List<Song> playList;
		private ListIterator<Song> playListIter;
	
		public Player(List<Song> playlist, boolean keepPlaying){		
			this.playList = playlist;
			playListIter = playList.listIterator();
			this.repeatPlayList = keepPlaying;
			mediaPlayer = new MediaPlayer();
			Log.d(TAG, "Player thread created");
		}
		
		public synchronized void setRepeatPlayList(boolean keepPlaying){
			this.repeatPlayList = keepPlaying;
		}
		
		public synchronized boolean getRepeatPlayList(){
			return repeatPlayList;
		}
		
		public void pauseOrResume() throws IllegalStateException{
			if (mediaPlayer.isPlaying()){
				mediaPlayer.pause(); 
				currentState = MusicService.State.Paused;
				Log.d(TAG, "media player paused");
			}
			else{
				mediaPlayer.start(); 
				currentState = MusicService.State.Playing;
				Log.d(TAG, "media player started again");
			}
		}
		
		
		
		public void playNext(){
			Log.d(TAG, "playNext() called");
			if (playListIter.hasNext()) {
				play(playListIter.next());
			}else if (repeatPlayList == true) {
				
				Log.d(TAG, "reached end of playlist: recreating ListIterator");
				playListIter = playList.listIterator();

				if (playListIter.hasNext()) {
					play(playListIter.next());
				}
			}
			
		}
		
		public void playPrevious(){
			Log.d(TAG, "playPrevious() called");
			
			if(playListIter.hasPrevious()){
				play(playListIter.previous());
			}else{
				Log.d(TAG, "no previous song!");			
			}
		}
		
		
		private void play(Song song) {
			Log.d(TAG,"playing song: " + song);
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
				mediaPlayer.prepare(); //prepare resource synchronosly (since seperate thread anyway)
				mediaPlayer.setOnCompletionListener(this);
				mediaPlayer.start(); //play!
				currentState = MusicService.State.Playing;
				
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
			
			if(playListIter.hasNext())
				play(playListIter.next());
			else
				Log.d(TAG, "no music in the playlist to play");
			
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
			repeatPlayList = false;
			currentState = MusicService.State.Stopped;
			Log.i(TAG, "Stopped and released MediaPlayer");
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			Log.d(TAG, " - onCompletion (Listener) - completed song in thread");

				playNext();

//				Log.d(TAG,
//						"Player stopped playing. Thread going to stop and releasing MediaPlayer");
//
//				onThreadDestroy();

		}

	}

	public class MusicBinder extends Binder {
		MusicService getService() {
			return MusicService.this;
		}
	}

}
