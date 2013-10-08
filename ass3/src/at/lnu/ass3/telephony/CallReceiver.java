package at.lnu.ass3.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		Log.d("CallReceiver", "received incomming call");

		Bundle bundle = intent.getExtras();
		if (null == bundle)
			return;
		String state = bundle.getString(TelephonyManager.EXTRA_STATE);
		if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
			String phonenumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
		}
	}

}
