package com.b4a.transits;

import java.util.HashMap;

import android.app.ProgressDialog;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.util.Log;

import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.ParseACL;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class ParseController {

	private static ParseInstallation installation;
	private static ParseUser currentUser;

	public ParseController() {

	}

	/**
	 * Static object to return singleton object of the ParseInstallation
	 * **/
	public static ParseInstallation getCurrentInstallation() {
		if (installation == null) {
			installation = ParseInstallation.getCurrentInstallation();
		}
		return installation;
	}

	/**
	 * For creating new Client user
	 ***/
	public static void createUser(final Context context, final String uname,
			final String pwd, String birthDate, String email, String phone) {

		ParseUser user = ParseUser.getCurrentUser();
		user.setUsername(uname);
		user.setPassword(pwd);
		user.setEmail(email);
		user.put("birthDate", birthDate);
		user.put("phone", phone);

		user.signUpInBackground(new SignUpCallback() {

			@Override
			public void done(ParseException ex) {
				// TODO Auto-generated method stub
				if (ex == null) {
					Log.d("Created New User", "Look at the parse.com !!!");

					Editor saveEditor = context.getSharedPreferences(
							"TransitPref", Context.MODE_PRIVATE).edit();
					// To indicate that the user is a registered user now
					saveEditor.putString("uname", uname);
					saveEditor.putString("pwd", pwd);
					saveEditor.putInt("anonymous", 2);
					saveEditor.commit();

					CustomToast.showToast(context, "User Create >> Success!!");

				} else {
					Log.e("ParseException @ createUser", "" + ex.getMessage());
				}
			}
		});
	}

	public static void createUserSynchronized(final Context context,
			final String uname, final String pwd, String birthDate,
			String email, String phone) {

		ParseUser user = ParseUser.getCurrentUser();
		user.setUsername(uname);
		user.setPassword(pwd);
		user.setEmail(email);
		user.put("birthDate", birthDate);
		user.put("phone", phone);

		try {
			user.signUp();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.d("Created New User", "Look at the parse.com !!!");

		Editor saveEditor = context.getSharedPreferences("TransitPref",
				Context.MODE_PRIVATE).edit();
		// To indicate that the user is a registered user now
		saveEditor.putString("uname", uname);
		saveEditor.putString("pwd", pwd);
		saveEditor.putInt("anonymous", 2);
		saveEditor.commit();

		CustomToast.showToast(context, "User Create >> Success!!");
	}

	/**
	 * For loggin in
	 ***/
	public static void loginUser(final Context context, String uname, String pwd) {
		ParseUser.logInInBackground(uname, pwd, new LogInCallback() {
			public void done(ParseUser user, ParseException ex) {
				if (user != null) {
					Log.d("User logged in", "Hello !!, " + user.getUsername());
					CustomToast.showToast(context, "User Login Success, "
							+ user.getUsername());
				} else {
					Log.e("ParseException @ LoginUser", "" + ex.getMessage());
				}
			}
		});

		// Alternately to show the progress Dialog
		// LoginAsync asyncLogin = new LoginAsync();

	}

	/**
	 * To logout
	 * **/
	public static void logoutUser() {

		Log.d("Logging Out", "User:" + ParseUser.getCurrentUser());
		ParseUser.logOut();
		Log.d("Logged Out", "User:" + ParseUser.getCurrentUser());

	}

	/**
	 * For returning the current user
	 ***/
	public static ParseUser getCurrentUser() {
		if (currentUser == null)
			currentUser = ParseUser.getCurrentUser();
		return currentUser;
	}

	/**
	 * Log out current User
	 * **/
	public void logOutCurrentUser() {
		ParseUser.logOut();
	}

	/**
	 * Save user data in installation
	 * **/

	public static boolean saveInInstallation(ParseUser user,
			final Context context, final int loginMode) {
		
		ParseInstallation inst = ParseController.getCurrentInstallation();
		inst.put("user", user);
		inst.setACL(new ParseACL(user));

		try {
			inst.save();
			Editor saveEditor = context.getSharedPreferences("TransitPref",
					Context.MODE_PRIVATE).edit();
			// To indicate that the user is anonymous already
			saveEditor.putInt("anonymous", loginMode);
			saveEditor.commit();
			return true;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return false;
	/*	
		inst.saveInBackground(new SaveCallback() {

			@Override
			public void done(ParseException ex) {
				// TODO Auto-generated method stub
				if (ex == null) {
					Log.v("Installation Success", "Look at parse.com !!!");
					Editor saveEditor = context.getSharedPreferences(
							"TransitPref", Context.MODE_PRIVATE).edit();
					// To indicate that the user is anonymous already
					saveEditor.putInt("anonymous", loginMode);
					saveEditor.commit();
				} else {
					Log.e("ParseException @ saveInInstallation",
							"" + ex.getMessage());
					// Delete the ParseUser

				}
			}
		});  */
	}

	/**
	 * Test Cloud service retrival
	 * **/
	public static void getCloudServiceData() {
		ParseCloud.callFunctionInBackground("hello",
				new HashMap<String, Object>(), new FunctionCallback<String>() {

					@Override
					public void done(String result, ParseException e) {
						if (e == null) {
							Log.v("Cloud Retrival Service SUCCESS !!!",
									"data: " + result);
							// CustomToast.showToast(MainActivity.getMainContext(),
							// "data: "+result);
						}
					}
				});
	}

	/** For Login in asynchronous mode **/
	private class LoginAsync extends AsyncTask<String, Void, Void> {

		private Context context;

		public LoginAsync(Context cont) {
			this.context = cont;
		}

		public LoginAsync() {
		}

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			ProgressDialog.show(context, "", "Loading...", true);
		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				ParseUser.logIn(params[0], params[1]);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

		}

	}

}
