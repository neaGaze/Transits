package com.b4a.transits;

import java.util.HashMap;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.CursorJoiner.Result;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
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
	public static void createUser(String uname, String pwd, String birthDate,
			String email, String phone) {
		ParseUser user = new ParseUser();
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
					CustomToast.showToast(MainActivity.getMainContext(),
							"User Create >> Success!!");
				} else {
					Log.e("ParseException @ createUser", "" + ex.getMessage());
				}
			}
		});
	}

	/**
	 * For loggin in
	 ***/
	public static void loginUser(String uname, String pwd) {
		ParseUser.logInInBackground(uname, pwd, new LogInCallback() {
			public void done(ParseUser user, ParseException ex) {
				if (user != null) {
					Log.d("User logged in", "Hello !!, " + user.getUsername());
					// CustomToast.showToast(MainActivity.getMainContext(),
					// "User Login Success, "+user.getUsername());
				} else {
					Log.e("ParseException @ LoginUser", "" + ex.getMessage());
				}
			}
		});

		// Alternately to show the progress Dialog
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected void onPreExecute() {
				// ProgressDialog.show(ToDoListActivity.this, "", "Loading...",
				// true);
				super.onPreExecute();
			}

			@Override
			protected void onProgressUpdate(Void... values) {
				super.onProgressUpdate(values);
			}

			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
			}

		};
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
	public static void saveInInstallation(ParseUser user) {
		ParseInstallation inst = ParseController.getCurrentInstallation();
		inst.put("user", user);
		inst.saveInBackground(new SaveCallback() {

			@Override
			public void done(ParseException ex) {
				// TODO Auto-generated method stub
				if (ex == null) {
					Log.v("Installation Success", "Look at parse.com !!!");
					// CustomToast.showToast(MainActivity.getMainContext(),
					// "Installation Success");
				} else
					Log.e("ParseException @ saveInInstallation",
							"" + ex.getMessage());
			}
		});
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

}
