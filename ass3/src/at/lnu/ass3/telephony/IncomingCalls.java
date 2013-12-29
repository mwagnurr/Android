package at.lnu.ass3.telephony;

import java.util.List;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import at.lnu.ass3.MainList;
import at.lnu.ass3.R;

public class IncomingCalls extends ListActivity {
	private static final String TAG = IncomingCalls.class.getSimpleName();

	private ArrayAdapter<CallEntity> adapter;
	private DataSource callsDAO;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		List<CallEntity> calls = callsDAO.getAllCalls();
		adapter = new ArrayAdapter<CallEntity>(this, R.id.call_row, calls);
		setListAdapter(adapter);

		registerForContextMenu(getListView());

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.listView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle(adapter.getItem((info.position)).getSenderPhoneNumber());
			menu.add(0, 0, 0, "Call");
			menu.add(0, 1, 1, "Send SMS");

		}

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();

		PackageManager pm = getPackageManager();
		boolean telephonySupported = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);

		if (telephonySupported == false) {
			Log.e(TAG, "phone doesnt support telephony!");

		}
		if (item.getItemId() == 0) { // CALL
			CallEntity ce = adapter.getItem((info.position));

			String toDial = "tel:" + ce.getSenderPhoneNumber();
			// start activity for ACTION_DIAL
			startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(toDial)));
		} else if (item.getItemId() == 1) { // SMS
			CallEntity ce = adapter.getItem((info.position));

			String toDial = "tel:" + ce.getSenderPhoneNumber();
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"
					+ ce.getSenderPhoneNumber()));
			intent.putExtra("sms_body", ce.getSenderPhoneNumber());
			startActivity(intent);
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
