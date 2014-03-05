package com.b4a.transits;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Connection {
	/*****************************************************************************************
	 * get Connection Availability
	 * ************************************************************************************/
	public static boolean getConnectionAvailable(Context context) {
		ConnectivityManager conMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

		if (netInfo != null && netInfo.isAvailable() && netInfo.isConnected()) {

			Log.e("Connection is good dude", ":D :D");
			return true;
		} else {
			if (netInfo == null)
				Log.e("Connection is null", "It's empty buddy :( :(");
			else if (!netInfo.isAvailable())
				Log.e("Connection is not Available",
						"No Connection available :( :(");
			else
				Log.e("Connection is not Connected",
						"No Connection buddy :( :(");
			return false;
		}
	}
}
