package at.lnu.ass2.mp3;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import at.lnu.ass2.R;

public class MusicPlayer extends Activity {
	private Button playButton;
	private MusicService musicService = null;
	
    private ServiceConnection connection = new ServiceConnection() {
    	//@Override  // Called when connection is made
    	public void onServiceConnected(ComponentName cName, IBinder binder) {
    		musicService =  ((MusicService.MusicBinder) binder).getService();
    	}
    	//@Override   //
    	public void onServiceDisconnected(ComponentName cName) {
    		musicService = null;
    	}
    };
	
	private final String TAG = "MusicPlayerActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_main);
		
		playButton = (Button) findViewById(R.id.music_play_button);
		playButton.setOnClickListener(new PlayClick());
		
		Intent intent = new Intent(this, MusicService.class);
		this.bindService(intent, connection, Context.BIND_AUTO_CREATE);
		Log.d(TAG, "bound the service!");
	}
	
	@Override
	public void onDestroy(){
		unbindService(connection);
		Log.d(TAG, "Unbound SlowCountService");
		super.onDestroy();
	}

	
	class PlayClick implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			
			//PLAY
			Log.d(TAG," play button clicked!");
			
			musicService.startPlaying(false);
			
			//musicService.
			/*
			 * int data = service.getCount();
    		data_display.setText(Integer.toString(data));
			 */
			
			/*
			 * Intent intent = new Intent(main_activity,SlowCountService.class);
    		main_activity.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    		System.out.println("Binding to SlowCountService");
			 */
/*
 * // check for already playing
				if(mp.isPlaying()){
					if(mp!=null){
						mp.pause();
						// Changing button image to play button
						btnPlay.setImageResource(R.drawable.btn_play);
					}
				}else{
					// Resume song
					if(mp!=null){
						mp.start();
						// Changing button image to pause button
						btnPlay.setImageResource(R.drawable.btn_pause);
					}
				}
 */
			
		}
		
	}

}
