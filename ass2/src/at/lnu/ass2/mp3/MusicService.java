package at.lnu.ass2.mp3;

import java.util.List;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import at.lnu.ass2.R;

public class MusicService extends Service {

	private int NOTIFICATION_ID = 1;

	private static final String TAG = MusicService.class.getSimpleName();

	private Player player;

	private final IBinder binder = new MusicBinder();

	/**
	 * State of the music player
	 * 
	 */
	enum State {
		Stopped, Playing, Paused
	};

	private State currentState = State.Stopped;

	public State getCurrentState() {
		return currentState;
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate() start");
		super.onCreate();
	}

	/**
	 * builds the notification and sets it with the service as foreground
	 * (higher priority service)
	 */
	private void startNotification() {

		Log.d(TAG, "building the music player notification");
		/* 1. Setup Notification Builder */
		Notification.Builder builder = new Notification.Builder(this);

		/* 2. Configure Notification Alarm */
		builder.setSmallIcon(R.drawable.music_icon)
				.setTicker(
						getResources().getString(R.string.music_notif_ticker))
				.setAutoCancel(false);

		/* 3. Configure Drop-down Action */
		builder.setContentTitle(
				getResources().getString(R.string.music_app_name))
				.setContentInfo(
						getResources().getString(R.string.music_notif_click));

		if (player != null && player.getCurrentSong() != null) {
			String display = player.getCurrentSong().getArtist() + " - "
					+ player.getCurrentSong().getTitle();
			builder.setContentText(display);
		} else {
			builder.setContentText(getResources().getString(
					R.string.music_notif_text));
		}

		Intent intent = new Intent(this, MusicPlayer.class); // Notification
																// intent
		PendingIntent notifIntent = PendingIntent.getActivity(this, 0, intent,
				0);
		builder.setContentIntent(notifIntent);

		/* 4. Create Notification and use Manager to launch it */
		Notification notification = builder.build();

		// notifManager = (NotificationManager) getSystemService(ns);
		// notifManager.notify(NOTIFICATION_ID, notification);

		startForeground(NOTIFICATION_ID, notification);
	}

	/**
	 * stops service foreground and the notification
	 */
	private void stopNotification() {
		Log.d(TAG, "stopping the music player notification");
		stopForeground(true);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG, "onBind received ");
		return binder;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "trying to destroy service");
		// Cancel the persistent notification.
		// notifMan.cancel(NOTIFICATION);

		// Tell the user we stopped.
		Toast.makeText(this,
				getResources().getString(R.string.music_service_stop),
				Toast.LENGTH_SHORT).show();

		stopForeground(true);

		if (player != null) {
			player.destroyMediaPlayer();
			player = null;
		}

		// notifManager.cancel(NOTIFICATION_ID);

		Log.d(TAG, "service destroyed!");
	}

	public void startPlaying(List<Song> playList, boolean playListRepeat) {
		Log.d(TAG, "startPlaying called");

		if (player == null) {
			player = new Player(playList, playListRepeat);
			player.start();
		} else {
			Log.d(TAG, "Player already instantinized");
			player.destroyMediaPlayer();
			player = new Player(playList, playListRepeat);
			player.start();

		}

	}

	/**
	 * plays the next song
	 */
	public void playNextSong() {
		if (player == null) {
			Log.e(TAG, "currently no player playing!");
		} else {
			player.playNext();
		}
	}

	/**
	 * plays the previous song
	 */
	public void playPreviousSong() {
		if (player == null) {
			Log.e(TAG, "currently no player playing!");
		} else {
			player.playPrevious();
		}
	}

	/**
	 * either pauses or resumes the current song
	 */
	public void pauseOrResume() {
		Log.d(TAG, "pauseOrResume called");

		if (player == null) {
			Log.e(TAG, "currently no player playing!");
		} else {
			try {
				player.pauseOrResume();
			} catch (IllegalStateException is) {
				Log.e(TAG, "player currently in an illegal state, aborting");
			}
		}
	}

	/**
	 * player thread that plays music via a MediaPlayer instance and needs a
	 * playlist to play if play list changes, new Player may be started but old
	 * one has to be released via {@link #destroyMediaPlayer()}
	 * 
	 * @author Wagi
	 * 
	 */
	private class Player extends Thread implements OnCompletionListener {
		// nested TAG for debugging
		private final String TAG = MusicService.TAG + ": "
				+ Player.class.getSimpleName();

		private MediaPlayer mediaPlayer;

		boolean repeatPlayList = false;

		private List<Song> playList;
		// private ListIterator<Song> playListIter;

		private Song currSong;
		private int currSongCount = 0;

		public Player(List<Song> playlist, boolean keepPlaying) {
			this.playList = playlist;
			if (playList.isEmpty()) {
				Log.e(TAG, "received playlist is empty");
			}
			currSong = playList.get(currSongCount);

			this.repeatPlayList = keepPlaying;
			mediaPlayer = new MediaPlayer();
			Log.d(TAG, "Player thread created");
		}

		/**
		 * @return current song that is played in the player
		 */
		public Song getCurrentSong() {
			return currSong;
		}

		/**
		 * method which either pauses or resumes depending on the current state
		 * of the media player
		 * 
		 * @throws IllegalStateException
		 *             if there was an error with the media player state
		 */
		public void pauseOrResume() throws IllegalStateException {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();

				currentState = MusicService.State.Paused;
				stopNotification(); // dont show notification when player is
									// paused
				Log.d(TAG, "media player paused");
			} else {
				mediaPlayer.start();

				currentState = MusicService.State.Playing;
				startNotification(); // show notification when player is playing
										// again
				Log.d(TAG, "media player started again");
			}
		}

		/**
		 * plays the next song in the players' play list if repeatPlayList =
		 * false: method call does nothing if repeatPlayList = true: begin from
		 * start again
		 */
		public void playNext() {
			Log.d(TAG, "playNext() called");

			if (currSongCount < playList.size() - 1) {
				currSongCount += 1;
				play(playList.get(currSongCount));
			} else if (repeatPlayList == true) {

				Log.d(TAG,
						"reached end of playlist, but repeat playlist is true; resetting currSongCount");

				currSongCount = 0;
				play(playList.get(currSongCount));
			} else {
				Log.d(TAG,
						"reached end of playlist already! repeat playlist is false");
			}
		}

		/**
		 * plays the previous song in the players' play list if repeatPlayList =
		 * false: method call does nothing if repeatPlayList = true: begin from
		 * end
		 */
		public void playPrevious() {
			Log.d(TAG, "playPrevious() called");

			if (currSongCount >= 1) {
				currSongCount -= 1;
				play(playList.get(currSongCount));

			} else if (repeatPlayList == true) {
				Log.d(TAG,
						"reached beginning of playlist, but repeat playlist is true; resetting currSongCount");
				currSongCount = playList.size() - 1;
			} else {
				Log.d(TAG,
						"reached beginning of playlist already and cant backwards any more, because: repeat playlist is false");
			}

		}

		/**
		 * main music playing method call this with a Song to set up and prepare
		 * the media player and play the song
		 * 
		 * @param song
		 */
		private void play(Song song) {
			Log.d(TAG, "playing song: " + song);
			if (song == null) {
				Log.e(TAG, "song is null, cant play");
				return;
			}
			try {
				if (mediaPlayer.isPlaying())
					mediaPlayer.stop(); // Stop current song.

				mediaPlayer.reset();
				mediaPlayer.setDataSource(MusicService.this,
						Uri.parse(song.getPath())); // set Song to play
				mediaPlayer
						.setAudioStreamType(AudioManager.STREAM_NOTIFICATION); // select
																				// audio
																				// stream
				mediaPlayer.prepare(); // prepare resource synchronosly (since
										// seperate thread anyway)
				mediaPlayer.setOnCompletionListener(this);
				mediaPlayer.start(); // play!
				currentState = MusicService.State.Playing;
				currSong = song;
				startNotification();

				Log.d(TAG, "media player started to play");

			} catch (Exception e) {
				Toast.makeText(MusicService.this,
						"TODO exception playing song", Toast.LENGTH_SHORT)
						.show();
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			Log.d(TAG, "Player runs");

			if (!playList.isEmpty())
				play(currSong);
			else
				Log.d(TAG, "no music in the playlist to play");
		}

		/**
		 * method to manually release the media player (and therefor stops
		 * playing and notification, etc)
		 */
		public void destroyMediaPlayer() {
			Log.i(TAG, "player thread gets stopped manually");
			onThreadDestroy();
		}

		private void onThreadDestroy() {
			if (mediaPlayer == null) {
				Log.d(TAG, "mediaplayer already null");
				return;
			}
			if (mediaPlayer.isPlaying())
				mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
			repeatPlayList = false;
			currentState = MusicService.State.Stopped;
			stopNotification();
			Log.i(TAG,
					"Stopped and released MediaPlayer and removed Notification");
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			Log.d(TAG, " - onCompletion (Listener) - completed song in thread");

			playNext();
		}

	}

	public class MusicBinder extends Binder {
		MusicService getService() {
			return MusicService.this;
		}
	}

}
