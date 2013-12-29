package at.lnu.ass3.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver {
	private static final String TAG = CallReceiver.class.getSimpleName();
	@Override
	public void onReceive(Context ctx, Intent intent) {
		Log.d("CallReceiver", "received incomming call");

		Bundle bundle = intent.getExtras();
		if (null == bundle){
			Log.e(TAG, "bundle == null");
			return;
		}
		String state = bundle.getString(TelephonyManager.EXTRA_STATE);
		if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
			String phonenumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
			
			DataSource callsDAO = new DataSource(ctx);
			callsDAO.open();
			callsDAO.createCallEntity(phonenumber);
			Log.d(TAG, "stored call to datasource");
		}
	}

}
