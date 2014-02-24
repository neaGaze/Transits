package com.b4a.transits;

import android.content.Context;
import android.widget.Toast;

public class CustomToast {
	
	public static void showToast(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

}
