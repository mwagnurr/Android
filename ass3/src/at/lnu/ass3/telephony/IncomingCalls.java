package at.lnu.ass3.telephony;

import java.util.List;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import at.lnu.ass3.MainList;
import at.lnu.ass3.R;

public class IncomingCalls extends ListActivity {
	private static final String TAG = IncomingCalls.class.getSimpleName();

	private ArrayAdapter<CallEntity> adapter;
	private DataSource callsDAO;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		callsDAO = new DataSource(this);
		callsDAO.open();

		List<CallEntity> calls = callsDAO.getAllCalls();
		adapter = new ArrayAdapter<CallEntity>(this, R.layout.telephony_list_row, calls);
		setListAdapter(adapter);

		registerForContextMenu(getListView());

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		Log.d(TAG, "onCreate finished");

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == getListView().getId()) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle(adapter.getItem((info.position)).getSenderPhoneNumber());
			menu.add(0, 0, 0, getResources().getString(R.string.telephony_call));
			menu.add(0, 1, 1, getResources().getString(R.string.telephony_share));
			Log.d(TAG, "created context menu");
		}

	}

	/**
	 * calls number with ACTION_DIAL
	 * 
	 * @param number
	 */
	private void call(String number) {

		String toDial = "tel:" + number;
		startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(toDial)));
	}

	/**
	 * sends message via app chooser
	 * 
	 * @param theMessage
	 */
	private void sendMessage(String theMessage) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_TEXT, theMessage);
		startActivity(Intent.createChooser(i, getResources().getString(R.string.telephony_share)));
	}

	private boolean isTelephonyEnabled() {
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		return tm != null && tm.getSimState() == TelephonyManager.SIM_STATE_READY;
	}

	@SuppressWarnings("deprecation")
	private static boolean isAirplaneModeOn(Context context) {

		return Settings.System.getInt(context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) != 0;

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();

		PackageManager pm = getPackageManager();
		boolean telephonySupported = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);

		// telephony checks

		if (telephonySupported == false) {
			Log.e(TAG, "phone doesnt support telephony!");
		}

		if (isAirplaneModeOn(this)) {
			Log.e(TAG, "phone is in airplane mode, cant call!");
			Toast.makeText(this, getResources().getString(R.string.telephony_alert_airplane),
					Toast.LENGTH_SHORT).show();
			return true;
		} else {
			Log.d(TAG, "phone is not in airplane mode! good!");
		}

		if (isTelephonyEnabled()) {
			Log.d(TAG, "DEBUG, second function says telephony is available!");
		} else {
			Log.e(TAG, "DEBUG, second function says telephony not available as well!");
			return true;
		}
		if (item.getItemId() == 0) { // CALL

			Log.d(TAG, "selected CALL");
			CallEntity ce = adapter.getItem((info.position));

			call(ce.getSenderPhoneNumber());
		} else if (item.getItemId() == 1) { // SMS
			Log.d(TAG, "selected SHARE");
			CallEntity ce = adapter.getItem((info.position));

			sendMessage(ce.getSenderPhoneNumber());
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent1 = new Intent(this, MainList.class);
			intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent1);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
