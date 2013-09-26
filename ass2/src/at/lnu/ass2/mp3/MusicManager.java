package at.lnu.ass2.mp3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class MusicManager {
	private static final String TAG = MusicManager.class.getSimpleName();

	private List<Song> playList = new ArrayList<Song>();

	private ContentResolver contentResolver;

	public MusicManager(ContentResolver contentResolver) {
		Log.d(TAG, "constructor - ");
		this.contentResolver = contentResolver;

	}

	/**
	 * method to get a random song from the play list
	 * 
	 * @return
	 */
	public Song getNextRandomSong() {
		Song next = null;
		if (playList.size() == 0) {
			Log.d(TAG, "getNextRandomSong() fails - no songs retrieved");
			return next;
		}
		Random randGen = new Random();
		int rand = randGen.nextInt(playList.size() - 1);
		try {
			next = playList.get(rand);
			Log.d(TAG, "getNextRandomSong selected song nr: " + rand + " / "
					+ next);
		} catch (IndexOutOfBoundsException ie) {
			Log.e(TAG, "error in getNextRandomSong()");
			ie.printStackTrace();
		}
		return next;
	}

	/**
	 * 
	 * @return play list of the retrieved music (null if not yet retrieved!)
	 */
	public List<Song> getPlayList() {
		return playList;
	}

	/**
	 * retrieve all music from the sd card may take long - call asynchronly
	 */
	public void retrieveMusic() {
		Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

		Log.i(TAG, "Querying media from " + uri);

		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			Log.d(TAG, "external storage state is not mounted");

			return;
		}
		Log.d(TAG, "there seems to be an sd card!");

		Cursor cursor = contentResolver.query(
				// using content resolver to read music from media storage
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] {
						MediaStore.Audio.Media.ARTIST,
						MediaStore.Audio.Media.ALBUM,
						MediaStore.Audio.Media.TITLE,
						MediaStore.Audio.Media.DATA,
						MediaStore.Audio.Media.DURATION,
						MediaStore.Audio.Media.TRACK,
						MediaStore.Audio.Media.YEAR },
				MediaStore.Audio.Media.IS_MUSIC + " > 0 ", null, null);

		if (cursor == null || !cursor.moveToFirst()) {
			Log.e(TAG, "No music to retrieve");
		}

		do {
			Song song = new Song(cursor.getString(0), cursor.getString(1),
					cursor.getString(2), cursor.getString(3),
					cursor.getLong(4), cursor.getInt(5), cursor.getInt(6));

			playList.add(song);
		} while (cursor.moveToNext());

		Log.i(TAG, "Finished retrieving music.. songs size: " + playList.size());
	}

}
