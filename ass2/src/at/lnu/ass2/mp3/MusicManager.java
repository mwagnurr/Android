package at.lnu.ass2.mp3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import at.lnu.ass2.R;

public class MusicManager {
	private static final String TAG = MusicManager.class.getSimpleName();

	private List<Song> songs = new ArrayList<Song>();

	private ContentResolver contentResolver;

	public MusicManager(ContentResolver contentResolver) {
		Log.d(TAG, "constructor - ");
		this.contentResolver = contentResolver;
		retrieveMusic();

	}

	public Song getNextRandomSong() {
		Song next = null;
		if (songs.size() == 0) {
			Log.d(TAG, "getNextRandomSong() fails - no songs retrieved");
			return next;
		}
		Random randGen = new Random();
		int rand = randGen.nextInt(songs.size() - 1);
		try {
			next = songs.get(rand);
			Log.d(TAG, "getNextRandomSong selected song nr: " + rand + " / "
					+ next);
		} catch (IndexOutOfBoundsException ie) {
			Log.e(TAG, "error in getNextRandomSong()");
			ie.printStackTrace();
		}
		return next;
	}

	/**
	 * may take long - call asynchronly
	 */
	private void retrieveMusic() {
		Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

		Log.i(TAG, "Querying media from " + uri);

		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			// Toast.makeText(this, R.string.music_nosd,
			// Toast.LENGTH_SHORT).show();
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

			songs.add(song);
		} while (cursor.moveToNext());

		Log.i(TAG, "Finished retrieving music.. songs size: " + songs.size());
	}

}
