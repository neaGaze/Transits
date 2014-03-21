package com.b4a.transits;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

public class CustomAdListener extends AdListener{

	private Context mContext;

	public CustomAdListener(Context context) {
		this.mContext = context;
	}

	@Override
	public void onAdLoaded() {
		Toast.makeText(mContext, "onAdLoaded()", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onAdFailedToLoad(int errorCode) {
		String errorReason = "";
		switch (errorCode) {
		case AdRequest.ERROR_CODE_INTERNAL_ERROR:
			errorReason = "Internal error";
			break;
		case AdRequest.ERROR_CODE_INVALID_REQUEST:
			errorReason = "Invalid request";
			break;
		case AdRequest.ERROR_CODE_NETWORK_ERROR:
			errorReason = "Network Error";
			break;
		case AdRequest.ERROR_CODE_NO_FILL:
			errorReason = "No fill";
			break;
		}

		Log.v("onAdFailedToLoad()", String.format("onAdFailedToLoad(%s)", errorReason));
		Toast.makeText(mContext,
				String.format("onAdFailedToLoad(%s)", errorReason),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onAdOpened() {
		Log.v("onAdOpened()", "onAdOpened()");
	}

	@Override
	public void onAdClosed() {
		Log.v("onAdClosed()", "onAdClosed()");
	}

	@Override
	public void onAdLeftApplication() {
		Log.v("onAdLeftApplication()", "onAdLeftApplication()");
	}

}
