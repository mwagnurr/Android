package at.lnu.ass2.mp3;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import at.lnu.ass2.R;

public class MusicPlayer extends Activity {
	private ImageButton playButton;
	private ImageButton stopButton;
	private ImageButton forwardButton;
	private ImageButton backwardButton;
	private ListView listView;
	private MusicService musicService = null;
	private MusicManager musicMan;
	private PlayListAdapter adapter;

	private boolean repeatPlayList = false;

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

	private final String TAG = "MusicPlayerActivity";

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

		stopButton = (ImageButton) findViewById(R.id.music_stop_button);
		stopButton.setOnClickListener(new StopClick());

		forwardButton = (ImageButton) findViewById(R.id.music_forward_button);
		forwardButton.setOnClickListener(new ForwardClick());

		backwardButton = (ImageButton) findViewById(R.id.music_back_button);
		backwardButton.setOnClickListener(new BackClick());

		Log.d(TAG, "onCreate() - finished");
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

			row = getLayoutInflater().inflate(R.layout.music_list_row, parent,
					false);

			TextView name = (TextView) row.findViewById(R.id.label);

			String listText = song.getArtist() + " - " + song.getTitle();
			name.setText(listText);
			row.setTag(song);

			Log.d(TAG, "playlistadapter - getView called");

			return row;
		}
	}

	private class RetrieverTask extends
			AsyncTask<Integer, Integer, MusicManager> {
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
			Log.d(TAG,
					"finished retrieving music.. filling playlist adapter now");

			final List<Song> playList = result.getPlayList();
			adapter = new PlayListAdapter(MusicPlayer.this, playList);
			listView = (ListView) findViewById(R.id.music_playlist);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int pos, long arg3) {
					Log.d(TAG, "Item clicked - trying to play song: ");
					// play(playList.get(pos));
					List<Song> clickedList = playList.subList(pos,
							playList.size());
					musicService.startPlaying(clickedList, repeatPlayList);

				}
			});
			listView.setAdapter(adapter);

		}
	}

	class PlayClick implements OnClickListener {

		@Override
		public void onClick(View arg0) {

			// PLAY
			Log.d(TAG, "play button clicked!");

			// musicService.setMusicManager(musicMan);
			if (musicService.getCurrentState() == MusicService.State.Stopped) {
				Log.d(TAG, "MusicService in State STOPPED");
				musicService.startPlaying(musicMan.getPlayList(),
						repeatPlayList);
				playButton.setImageResource(R.drawable.btn_pause);

			} else if (musicService.getCurrentState() == MusicService.State.Playing) {
				Log.d(TAG, "MusicService in State PLAYING");
				musicService.pauseOrResume();
				playButton.setImageResource(R.drawable.btn_play);

			} else {
				Log.d(TAG, "MusicService in State PAUSED");
				musicService.pauseOrResume();
				playButton.setImageResource(R.drawable.btn_pause);
			}
		}

	}

	class StopClick implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			Log.d(TAG, "stop button clicked!");
		}

	}

	class ForwardClick implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			Log.d(TAG, "forward button clicked!");
		}

	}

	class BackClick implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			Log.d(TAG, "back button clicked!");
		}

	}

}
