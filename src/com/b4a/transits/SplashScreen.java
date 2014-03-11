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
import android.util.Log;
import android.view.Menu;

public class SplashScreen extends Activity {

	SharedPreferences sharedPreferences;
	String uname, pwd;
	private int anonymous;

	private static int UNREGISTERED = 0;
	private static int ANONYMOUS = 1;
	private static int PARSE = 2;
	private static int FACEBOOK = 3;

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
	private class PrefetchData extends AsyncTask<Integer, Void, Integer> {

		public boolean registeredInParse = true;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// before making http calls

		}

		@Override
		protected Integer doInBackground(Integer... arg0) {

			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			sharedPreferences = getSharedPreferences("TransitPref",
					Context.MODE_PRIVATE);

			/** Fetch the shared Preferences **/
			if (sharedPreferences.contains("uname"))
				uname = sharedPreferences.getString("uname", "");
			else
				uname = "";

			if (sharedPreferences.contains("pwd"))
				pwd = sharedPreferences.getString("pwd", "");
			else
				pwd = "";

			if (sharedPreferences.contains("anonymous"))
				anonymous = sharedPreferences.getInt("anonymous", 0);
			else
				anonymous = UNREGISTERED;

			Log.v("LOGIN MODE", "" + anonymous);
			// anonymous = ANONYMOUS;

			/** Check what kind of login is this **/
			if (anonymous == PARSE) {
				try {
					// ParseController.loginUser(uname, pwd);
					ParseUser.logIn(uname, pwd);
					Log.v("Login from Shared PReferences", "SUCCESS !!,"
							+ uname + ":" + pwd);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e("ParseException", "" + e.getMessage());
				}
			} else if (anonymous == ANONYMOUS) {
				ContactList contactList = new ContactList(
						SplashScreen.this);
				contactList.getContacts();

			} else if (anonymous == FACEBOOK) {
				// Login in Facebook and wait till login is success or fiasco

			} else if (anonymous == UNREGISTERED) {

				registeredInParse = false;

				if (Connection.getConnectionAvailable(SplashScreen.this)) {

					// TODO Auto-generated method stub
					ParseAnonymousUtils.logIn(new LogInCallback() {

						@Override
						public void done(final ParseUser parseUser,
								ParseException ex) {
							// TODO Auto-generated method stub
							if (ex == null) {

								// TODO Auto-generated method stub
								Log.d("Anonymous Login", "ANONYMOUS !!");

								ContactList contactList = new ContactList(
										SplashScreen.this);
								contactList.getContacts();

								registeredInParse = ParseController
										.saveInInstallation(parseUser,
												SplashScreen.this, ANONYMOUS);

							} else
								Log.e("ParseException  @ Anonymous login", ""
										+ ex.getMessage());
						}
					});

				}

			} else {
				CustomToast
						.showToast(SplashScreen.this,
								"Are you on dope? 'cause this ain't never gonna happen nigga' !!!");
			}
			while (registeredInParse == false) {

				// Log.v("WAIT !! REGISTERING IN PARSE...","background process running");
			}
			return anonymous;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			Log.d("RESULT", "" + result);
			if (result == UNREGISTERED || result == ANONYMOUS) {

				Intent i = new Intent(SplashScreen.this, SignUpActivity.class);
				startActivity(i);

			} else if (result == PARSE || result == FACEBOOK) {

				Intent i = new Intent(SplashScreen.this, MainActivity.class);
				i.putExtra("uname", uname);
				i.putExtra("pwd", pwd);
				startActivity(i);
			} else {
				CustomToast
						.showToast(SplashScreen.this, "Go home you're drunk");
			}
			// close this activity
			finish();
		}

	}

}
