package com.b4a.transits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class FacebookController {

	private static Context context;
	public static boolean facebookLinkStatus = false;

	public FacebookController(Context context) {
		FacebookController.context = context;
	}

	/**
	 * Link facebook with the existing ParseUser
	 * **/
	public void linkFacebook(final ParseUser parseUser) {

		if (!ParseFacebookUtils.isLinked(parseUser)) {

			ParseFacebookUtils.link(parseUser, Arrays.asList("email",
					"basic_info", "user_about_me", "user_relationships",
					"user_birthday", "user_location"), (Activity) context,
					new SaveCallback() {

						@Override
						public void done(ParseException ex) {

							if (ex == null) {

								if (ParseFacebookUtils.isLinked(parseUser)) {

									Log.v("ParseUser Linked Successfully with Facebook",
											"Woohoo, user logged in with Facebook!"
													+ parseUser.getUsername());

									// Transfer FB info into Parse info
									performAdditionalFBOperation();

									facebookLinkStatus = true;
									Log.v("FB_STATUS", "" + facebookLinkStatus);

								} else {
									Log.e("ParseUser + Facebook failed",
											"ParseUser not linked w/ Facebook"
													+ parseUser.getUsername());
								}
							} else {
								Log.e("ParseException at FacebookController@pg-32",
										"Link failed :(");
							}
						}
					});

			Log.v("FB_STATUS_MAIN_THREAD", "" + facebookLinkStatus);
			// while (facebookLinkStatus == false) {
			// }
		} else {
			Log.v("Facebook is linked", "ParseUser linked with facebook id: ");

			List<String> permissions = ParseFacebookUtils.getSession()
					.getPermissions();
			for (int i = 0; i < permissions.size(); i++) {
				Log.v("Permissions", "" + permissions.get(i));
			}

			performAdditionalFBOperation();
		}

	}

	/**
	 * Import data from facebook into ParseUser a/c and save in sharedPrefs
	 * **/
	protected static void performAdditionalFBOperation() {
		// TODO Auto-generated method stub

		Request.newMeRequest(ParseFacebookUtils.getSession(),
				new Request.GraphUserCallback() {

					@Override
					public void onCompleted(GraphUser user, Response response) {
						// TODO Auto-generated method stub

						if (user != null) {

							String accessToken = Session.getActiveSession()
									.getAccessToken();

							ParseUser.getCurrentUser().put("fbAccessToken",
									accessToken);

							ParseUser.getCurrentUser().put("username",
									user.getFirstName());

							ParseUser.getCurrentUser().put("birthDate",
									user.getBirthday());

							ParseUser.getCurrentUser().put("fbuid",
									user.getId());

							// Log.v("Email", "" + user.getBirthday());

							try {

								ParseUser.getCurrentUser().save();

							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								Log.e("ParseException @FacebookController/lineNo-104",
										"" + e.getMessage());
							}
							Log.v("fbAccessToken", "" + accessToken);

							// Save in sharedPrefs
							Editor saveEditor = context.getSharedPreferences(
									"TransitPref", Context.MODE_PRIVATE).edit();
							saveEditor.putString("uname", user.getFirstName());
							saveEditor.putInt("anonymous", 3); // Set facebook
																// as login mode
							// which is 3
							saveEditor.commit();
						} else {
							Log.v("GraphUser returned null",
									"No graph user found");
						}
					}
				}).executeAsync();

		Request.newMyFriendsRequest(ParseFacebookUtils.getSession(),
				new Request.GraphUserListCallback() {

					@Override
					public void onCompleted(List<GraphUser> users,
							Response response) {
						if (users != null) {
							List<String> friendsList = new ArrayList<String>();
							for (GraphUser user : users) {
								friendsList.add(user.getId());
							}

							// Construct a ParseUser query that will find
							// friends whose
							// facebook IDs are contained in the current user's
							// friend list.
							ParseQuery friendQuery = ParseQuery.getUserQuery();
							friendQuery.whereContainedIn("fbuid", friendsList);

							// findObjects will return a list of ParseUsers that
							// are friends with
							// the current user
							try {
								List<GraphUser> friendUsers = friendQuery
										.find();
								if (friendUsers != null)
									Log.v("Friend size",
											"" + friendsList.size());
								else
									Log.v("Empty friendUsers",
											"Empty friend returnd");
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								Log.e("ParseException", "" + e.getMessage());
							}
						}

						/** Goto main activity **/
						CustomToast.showToast(context,
								"Facebook sign in successful");
						Intent i = new Intent((Activity) context,
								MainActivity.class);
						i.putExtra("uname", ""
								+ ParseUser.getCurrentUser().get("username"));
						i.putExtra("pwd", "");
						
						((Activity) context).startActivity(i);
						((Activity) context).finish();

					}
				}).executeAsync();

	}
}
