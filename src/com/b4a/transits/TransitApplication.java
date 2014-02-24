package com.b4a.transits;

import com.parse.Parse;
import com.parse.PushService;
import android.app.Application;

public class TransitApplication extends Application{

	public TransitApplication() {
		
	}
	
	 @Override
	  public void onCreate() {
	    super.onCreate();

		// Initialize the Parse SDK.
		Parse.initialize(this, "i9sIs4S88wxULX32ltE8P2Xyvmsk37UYpjEkbKFo", "BFBLBu2gL2Pp7VcZ2V6x5Umb6F5ORmcMJbLPu9Bs"); 

		// Specify an Activity to handle all pushes by default.
		PushService.setDefaultPushCallback(this, MainActivity.class);
	  }
}
