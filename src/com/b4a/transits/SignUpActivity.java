package com.b4a.transits;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import android.os.Bundle;
import android.renderscript.Element;
import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SignUpActivity extends Activity implements OnClickListener {

	private Button parseButton, facebookButton, anonymousButton;
	private static int SIGNUP_PARSE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sign_up);

		init();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sign_up, menu);

		return true;
	}

	public void init() {
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

				ProgressDialog pDialog = new ProgressDialog(SignUpActivity.this);
				pDialog.show(this, "Signing Up", "Signing in progress");
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

				pDialog.dismiss();

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
		}
	}

}
