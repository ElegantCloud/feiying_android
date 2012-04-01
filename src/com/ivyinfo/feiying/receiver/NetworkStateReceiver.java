package com.ivyinfo.feiying.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			
//			NetworkInfo ni = intent
//					.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if (ni != null) {
				int type = ni.getType();
				String typeName = ni.getTypeName();
				boolean available = ni.isAvailable();
				State state = ni.getState();
				Log.d("feiying", "network changed");
				Log.d("feiying", "type: " + type + " type name: " + typeName);
				Log.d("feiying", "available: " + available);
				Log.d("feiying", "state: "  + state.name());
			}
			
		}
	}

}
