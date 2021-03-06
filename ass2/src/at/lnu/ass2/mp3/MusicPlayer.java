package at.lnu.ass2.mp3;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import at.lnu.ass2.MainList;
import at.lnu.ass2.R;

public class MusicPlayer extends Activity {
	private ImageButton playButton;
	private ImageButton forwardButton;
	private ImageButton backwardButton;
	private ListView listView;
	private MusicService musicService = null;
	private MusicManager musicMan;
	private PlayListAdapter adapter;

	/**
	 * defines if the play list should be repeated or not - currently hardcoded and not via UI
	 */
	private boolean repeatPlayList = true;

	private boolean playing = false;

	private ServiceConnection connection = new ServiceConnection() {
		// @Override // Called when connection is made
		public void onServiceConnected(ComponentName cName, IBinder binder) {
			musicService = ((MusicService.MusicBinder) binder).getService();
		}

		// @Override //
		public void onServiceDisconnected(ComponentName cName) {
			musicService = null;
		}
	};

	private static final String TAG = MusicPlayer.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_main);

		Intent intent = new Intent(this, MusicService.class);
		this.startService(intent);
		Log.d(TAG, "started the service");

		this.bindService(intent, connection, Context.BIND_AUTO_CREATE);
		Log.d(TAG, "bound the service");

		// init musicMan with asyncTask:
		RetrieverTask retriever = new RetrieverTask();
		retriever.execute(0);

		playButton = (ImageButton) findViewById(R.id.music_play_button);
		playButton.setOnClickListener(new PlayClick());

		forwardButton = (ImageButton) findViewById(R.id.music_forward_button);
		forwardButton.setOnClickListener(new ForwardClick());

		backwardButton = (ImageButton) findViewById(R.id.music_back_button);
		backwardButton.setOnClickListener(new BackClick());

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState != null)
			playing = savedInstanceState.getBoolean("playing");

		changePlayButton();

		Log.d(TAG, "onCreate() - finished");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("playing", playing);
	}

	@Override
	public void onDestroy() {
		unbindService(connection);
		Log.d(TAG, "Unbound MusicService");
		super.onDestroy();
	}

	private class PlayListAdapter extends ArrayAdapter<Song> {
		public PlayListAdapter(Context context, List<Song> objects) {
			super(context, 0, objects);
		}

		@Override
		public View getView(int position, View row, ViewGroup parent) {
			Song song = getItem(position);

			row = getLayoutInflater().inflate(R.layout.music_list_row, parent, false);

			TextView name = (TextView) row.findViewById(R.id.label);

			String listText = song.getArtist() + " - " + song.getTitle();
			name.setText(listText);

			if (musicService != null) {
				// Log.d(TAG, "getView - service not null");
				Song currSong = musicService.getCurrentPlayerSong();
				if (currSong != null) {

					if (song.equals(currSong)) {
						Log.d(TAG, "getView - found curr playing song in adapter, setting yellow");
						name.setTextColor(getResources().getColor(R.color.solid_yellow));
					}
				}

			} else {
				// Log.d(TAG, "getView - service null!");
			}

			row.setTag(song);

			// Log.d(TAG, "playlistadapter - getView called");

			return row;
		}
	}

	/**
	 * asynchronousily retrieves music from the external storage with MusicManager and initialises
	 * music song list
	 * 
	 * @author Wagi
	 * 
	 */
	private class RetrieverTask extends AsyncTask<Integer, Integer, MusicManager> {
		@Override
		// Heavy work, executed in separate thread
		protected MusicManager doInBackground(Integer... steps) {

			// retrieving music
			musicMan = new MusicManager(MusicPlayer.this.getContentResolver());
			musicMan.retrieveMusic();

			return musicMan;
		}

		@Override
		protected void onProgressUpdate(Integer... update) {
			Log.d(TAG, "onProgressUpdate: " + update);
		}

		@Override
		// called when doInBackground completed, executed in GUI thread
		protected void onPostExecute(MusicManager result) {
			Log.d(TAG, "finished retrieving music.. filling playlist adapter now");

			final List<Song> playList = result.getPlayList();
			adapter = new PlayListAdapter(MusicPlayer.this, playList);
			listView = (ListView) findViewById(R.id.music_playlist);
			listView.setOnItemClickListener(new MusicListItemClick(playList));
			listView.setAdapter(adapter);

			// String msg = getResources().getString(R.string.music_toast_retrieved);
			// Toast.makeText(MusicPlayer.this, msg, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Listener for item click on the music song list
	 * 
	 * @author Wagi
	 * 
	 */
	private class MusicListItemClick implements OnItemClickListener {

		private List<Song> playlist;

		public MusicListItemClick(List<Song> playlist) {
			this.playlist = playlist;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int pos, long arg3) {
			Log.d(TAG, "Item clicked - trying to play song with pos: " + pos + " playlist size: "
					+ playlist.size());

			musicService.startPlaying(playlist, pos, repeatPlayList);
			playing = true;

			// update play button image and notify adapter to redraw list for highlighting
			changePlayButton();
			adapter.notifyDataSetChanged();
		}

	}

	private class PlayClick implements OnClickListener {

		@Override
		public void onClick(View arg0) {

			// PLAY
			Log.d(TAG, "play button clicked!");

			// call service methods and set correct button state
			if (musicService.getCurrentState() == MusicService.State.Stopped) {
				Log.d(TAG, "starting new playing");
				musicService.startPlaying(musicMan.getPlayList(), repeatPlayList);
				playing = true;
			} else {

				musicService.pauseOrResume();

				if (playing || musicService.getCurrentState() == MusicService.State.Playing)
					playing = false;
				else
					playing = true;

			}

			// update play button image and notify adapter to redraw list for highlighting
			updatePlayPauseButtonImage();
			adapter.notifyDataSetChanged();
		}

	}

	/**
	 * changes the image from the play button to pause button while playing (and back)
	 */
	private void changePlayButton() {
		// call service methods and set correct button state

		if (musicService != null) {
			if (musicService.getCurrentState() == MusicService.State.Stopped) {
				Log.d(TAG, "MusicService in State STOPPED");
				playing = false;
			} else if (musicService.getCurrentState() == MusicService.State.Playing) {
				Log.d(TAG, "MusicService in State PLAYING");
				playing = true;
			} else {
				Log.d(TAG, "MusicService in State PAUSED");
				playing = false;
			}
		}

		updatePlayPauseButtonImage();
	}

	/**
	 * updates the background image of the play/pause button depending on the member variable
	 * "playing"
	 */
	private void updatePlayPauseButtonImage() {
		if (playing) {
			playButton.setImageResource(R.drawable.btn_pause);
		} else {
			playButton.setImageResource(R.drawable.btn_play);
		}
	}

	private class ForwardClick implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			Log.d(TAG, "forward button clicked!");
			musicService.playNextSong();
			playing = true;

			// update play button image and notify adapter to redraw list for highlighting
			changePlayButton();
			adapter.notifyDataSetChanged();
		}

	}

	private class BackClick implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			Log.d(TAG, "back button clicked!");
			musicService.playPreviousSong();
			playing = true;

			// update play button image and notify adapter to redraw list for highlighting
			changePlayButton();
			adapter.notifyDataSetChanged();
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, MainList.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
