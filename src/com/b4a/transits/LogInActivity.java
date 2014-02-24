package com.b4a.transits;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LogInActivity extends Activity {

	private EditText unameField, pwdField;
	private Button signUpButton;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log_in_activity);
		
		unameField = (EditText) findViewById(R.id.editTextLogin);
		pwdField = (EditText) findViewById(R.id.editTextLoginPwd);
		signUpButton = (Button) findViewById(R.id.Login);
		signUpButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Bundle bundle = new Bundle();
				bundle.putString("uname", unameField.getText().toString());
				bundle.putString("pwd", pwdField.getText().toString());

				Intent intent = new Intent();
				intent.putExtras(bundle);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.log_in, menu);
		return true;
	}

}
