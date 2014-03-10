package com.b4a.transits;

import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class FacebookController {

	public static void linkFacebook(Context context, final ParseUser parseUser) {

		if (!ParseFacebookUtils.isLinked(parseUser)) {

			ParseFacebookUtils.link(parseUser, Arrays.asList("email"),
					(Activity) context, new SaveCallback() {

						@SuppressWarnings("deprecation")
						@Override
						public void done(ParseException ex) {

							if (ex == null) {

								if (ParseFacebookUtils.isLinked(parseUser)) {

									Log.d("ParseUser Linked Successfully with Facebook",
											"Woohoo, user logged in with Facebook!"
													+ parseUser.getUsername());

									// parseUser.put("birthDate", );
									Request.executeMeRequestAsync(
											Session.getActiveSession(),
											new Request.GraphUserCallback() {

												@Override
												public void onCompleted(
														GraphUser user,
														Response response) {
													// TODO Auto-generated
													// method stub
													Log.v("BIRTHDAY",
															user.getBirthday());
												}

											});

								} else {
									Log.e("ParseUser is still not linked with Facebook",
											"ParseUser not linked w/ Facebook");
								}
							} else {
								Log.e("ParseException at FacebookController@pg-32",
										"Link failed :(");
							}
						}
					});
		} else {
			Log.v("Facebook is linked", "ParseUser linked with facebook id: ");
		}

		if (parseUser.get("authData") == null) {
			performAdditionalFBOperation(context);
		}
	}

	@SuppressWarnings("deprecation")
	protected static void performAdditionalFBOperation(final Context context) {
		// TODO Auto-generated method stub
		Request.executeMeRequestAsync(ParseFacebookUtils.getSession(),
				new Request.GraphUserCallback() {

					@Override
					public void onCompleted(GraphUser user, Response response) {
						// TODO Auto-generated method stub

						if (user != null) {
							ParseUser.getCurrentUser().put("username",
									user.getFirstName());

							ParseUser.getCurrentUser().put("birthDate",
									user.getBirthday());

							ParseUser.getCurrentUser().put("email",
									user.getUsername());

							ParseUser.getCurrentUser().saveInBackground();
							Log.v("BIRTHDAY", "" + user.getBirthday());
							
							Editor saveEditor = context.getSharedPreferences(
									"TransitPref", Context.MODE_PRIVATE).edit();
							saveEditor.putString("uname", user.getFirstName());
							saveEditor.putInt("anonymous", 3); // Set facebook
																// as login mode
																// which is 3
							saveEditor.commit();
						}

					}
				});
	}
}
