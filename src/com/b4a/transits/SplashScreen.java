package com.b4a.transits;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;

public class SplashScreen extends Activity {

	SharedPreferences sharedPreferences;
	String uname, pwd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		 new PrefetchData().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash_screen, menu);
		return true;
	}
	
	 /**
     * Async Task to make http call
     */
    private class PrefetchData extends AsyncTask<Void, Void, Void> {
 
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // before making http calls         
 
        }
 
        @Override
        protected Void doInBackground(Void... arg0) {
           
           sharedPreferences = getSharedPreferences("TransitPref", Context.MODE_PRIVATE);
           if(sharedPreferences.contains("uname")) {
        	   uname = sharedPreferences.getString("uname", "");
           }
           else {
        	   uname = "";
           }
 
           if(sharedPreferences.contains("pwd")) {
        	   pwd = sharedPreferences.getString("pwd", "");
           }
           else {
        	   pwd = "";
           }
           
       
           if(uname != "" && pwd != "")
           {
        	   try {
        		//     ParseController.loginUser(uname, pwd);
        		   ParseUser.logIn(uname, pwd);
        		   Log.v("Login from Shared PReferences","SUCCESS !!,"+uname+":"+pwd);
        	   } catch (ParseException e) {
        		   // TODO Auto-generated catch block
        		   e.printStackTrace();
        		   Log.e("ParseException", ""+e.getMessage());
        	   }
           }
           else{
        	   ParseAnonymousUtils.logIn(new LogInCallback() {
				
				@Override
				public void done(ParseUser arg0, ParseException ex) {
					// TODO Auto-generated method stub
					if(ex == null)
						Log.d("Anonymous Login", "ANONYMOUS !!");
					else
						Log.e("ParseException  @ Anonymous login", ""+ex.getMessage());
				}
			});
           }
           return null;
        }
 
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // After completing http call
            // will close this activity and lauch main activity
            
            Intent i = new Intent(SplashScreen.this, MainActivity.class);
            i.putExtra("uname", uname);
            i.putExtra("pwd", pwd);
            startActivity(i);
 
            // close this activity
            finish();
        }
 
    }

}
