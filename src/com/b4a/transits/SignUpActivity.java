package com.b4a.transits;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class SignUpActivity extends Activity implements OnClickListener {

	private Button parseButton, facebookButton, anonymousButton;
	public ProgressDialog pDialog;
	private AdView mAdView;

	private static int SIGNUP_PARSE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sign_up);
/*
		mAdView = new AdView(this);
		mAdView.setAdUnitId(getResources().getString(R.string.ADD_UNIT_ID));
		mAdView.setAdSize(AdSize.BANNER);
		mAdView.setAdListener(new CustomAdListener(this));
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.signUpActivityId);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
		layout.addView(mAdView, params);
		mAdView.loadAd(new AdRequest.Builder().build());
		
*/		init();

	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (pDialog != null)
			pDialog.dismiss();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sign_up, menu);

		return true;
	}

	public void init() {

		// Create an ad.
		// adView = new AdView(this);
		// adView.setAdSize(AdSize.BANNER);
		// adView.setAdUnitId(AD_UNIT_ID);
		/*
		 * adView = (AdView) findViewById(R.id.ad);
		 * adView.setAdSize(AdSize.BANNER);
		 * 
		 * Builder adRequestBuilder = new AdRequest.Builder();
		 * adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
		 * adRequestBuilder.addKeyword("sporting goods"); AdRequest adRequest =
		 * adRequestBuilder.build();
		 * 
		 * adView.loadAd(adRequest);
		 */
		parseButton = (Button) findViewById(R.id.buttonParse);
		facebookButton = (Button) findViewById(R.id.buttonFacebook);
		anonymousButton = (Button) findViewById(R.id.buttonAnonymous);

		parseButton.setOnClickListener(this);
		facebookButton.setOnClickListener(this);
		anonymousButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == parseButton) {
			handleParse();
		} else if (v == facebookButton) {
			// Intent intent = new Intent(this, FacebookActivity.class);
			// startActivity(intent);

			FacebookController facebookController = new FacebookController(this);
			facebookController.linkFacebook(ParseUser.getCurrentUser());

			// finish();
		} else if (v == anonymousButton) {
			Intent i = new Intent(this, MainActivity.class);
			i.putExtra("uname", "");
			i.putExtra("pwd", "");
			startActivity(i);
			finish();
		}

	}

	/**
	 * Handle parse a/c
	 * **/
	private void handleParse() {
		// TODO Auto-generated method stub
		Intent i = new Intent(this, AddUserActivity.class);
		startActivityForResult(i, SIGNUP_PARSE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (intent == null) {
			return;
		}
		if (requestCode == SIGNUP_PARSE) {
			final Bundle extras = intent.getExtras();

			String uname = extras.getString("uname");
			String pwd = extras.getString("pwd");
			String email = extras.getString("email");
			String phone = extras.getString("phone");
			String DOB = extras.getString("DOB");
			DOB += "_" + extras.getString("TOB");

			if (Connection.getConnectionAvailable(SignUpActivity.this)) {

				final ProgressDialog pDialog = ProgressDialog.show(this,
						"Signing Up", "Signing in progress");

				try {
					ParseController.createUserSynchronized(this, uname, pwd,
							DOB, email, phone);
					ParseUser.logIn(uname, pwd);
					// ParseController.loginUser(this, uname, pwd);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					CustomToast.showToast(this, "Something went wrong :(");
				}

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

						pDialog.dismiss();
					}
				});

				Intent i = new Intent(SignUpActivity.this, MainActivity.class);
				i.putExtra("uname", uname);
				i.putExtra("pwd", pwd);
				startActivity(i);
				finish();
			} else {
				CustomToast
						.showToast(SignUpActivity.this,
								"Sorry no connection available now. Try again later :D");
				Intent i = new Intent(SignUpActivity.this, MainActivity.class);
				i.putExtra("uname", "");
				i.putExtra("pwd", "");
				startActivity(i);
				finish();
			}
		} else {

			pDialog = ProgressDialog
					.show(this,
							"Retrieving Facebook Info ",
							"Please wait while the app retrieves info from your facebook profile",
							true);

			Log.v("This page is called after linking to facebook",
					"Maybe it's facebook");
			ParseFacebookUtils.finishAuthentication(requestCode, resultCode,
					intent);
			// startActivity(new Intent(this, MainActivity.class));
			// finish();
		}
	}

}
