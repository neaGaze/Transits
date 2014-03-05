package com.b4a.transits;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.os.Bundle;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.*;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

public class AddUserActivity extends Activity implements OnClickListener {

	private EditText uname, pwd, email, phone;
	private Button signUp, DOBButton, TOBButton, addContact;
	private String day, month, year, hour, minute, combinedDate, combinedTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_user);

		uname = (EditText) findViewById(R.id.editText1);
		pwd = (EditText) findViewById(R.id.editText2);
		email = (EditText) findViewById(R.id.editText3);
		phone = (EditText) findViewById(R.id.editText4);

		DOBButton = (Button) findViewById(R.id.buttonDOB);
		TOBButton = (Button) findViewById(R.id.buttonTOB);
		addContact = (Button) findViewById(R.id.buttonAddContact);
		DOBButton.setOnClickListener(this);
		TOBButton.setOnClickListener(this);
		addContact.setOnClickListener(this);

		day = "";
		month = "";
		year = "";
		hour = "";
		minute = "";

		signUp = (Button) findViewById(R.id.button1);
		signUp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				if (verifyFields()) {

					Bundle bundle = new Bundle();
					bundle.putString("uname", uname.getText().toString());
					bundle.putString("pwd", pwd.getText().toString());
					bundle.putString("email", email.getText().toString());
					bundle.putString("phone", phone.getText().toString());
					bundle.putString("DOB", combinedDate);
					bundle.putString("TOB", combinedTime);

					intent.putExtras(bundle);

					setResult(RESULT_OK, intent);
				}
				finish();
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_user, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1: {
			// ParseController.saveInInstallation(ParseController.getCurrentUser());
		}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.equals(DOBButton)) {

			String saf = null;

			if (year == "" && month == "" && day == "") {
				Calendar currDate = Calendar.getInstance();
				SimpleDateFormat dtFormat = new SimpleDateFormat(
						"yyyyMMddHHmmss", Locale.US);
				saf = dtFormat.format(currDate.getTime());

				year = saf.subSequence(0, 4).toString();
				month = saf.subSequence(4, 6).toString();
				day = saf.subSequence(6, 8).toString();
			}

			Log.e("Date are: ", "" + year + "-" + month + "-" + day);
			new DatePickerDialog(this, dateSet, Integer.parseInt(year),
					Integer.parseInt(month), Integer.parseInt(day)).show();

		} else if (v.equals(TOBButton)) {

			if (hour == "" && minute == "") {
				hour = new StringBuilder().append(
						Calendar.getInstance().getTime().getHours()).toString();
				minute = new StringBuilder().append(
						Calendar.getInstance().getTime().getMinutes())
						.toString();
			}
			Log.e("Time Set are: ", "" + hour + ":" + minute);
			new TimePickerDialog(this, timeSet, Integer.parseInt(hour),
					Integer.parseInt(minute), true).show();

		} else if (v.equals(addContact)) {
			CustomToast.showToast(AddUserActivity.this, "This feature will be made available sooner");
		} else {

		}
	}

	/**
	 * To verify fields
	 * **/
	private boolean verifyFields() {
		// TODO Auto-generated method stub
		try {
			if (uname.getText().toString() == ""
					|| pwd.getText().toString() == "" || email == null
					|| combinedDate == null || combinedTime == null) {
				Log.e("Fields empty", "Check empty fields");
				CustomToast.showToast(AddUserActivity.this, "Empty fields");
			} else
				return true;
		} catch (Exception e) {
			Log.e("Something empty", "" + e.getMessage());
		}
		return false;
	}

	/*****************************************************************************************************
	 * For Date Picker Dialog
	 * **************************************************************************************************/
	DatePickerDialog.OnDateSetListener dateSet = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int Year, int monthOfYear,
				int dayOfMonth) {
			/** perform your required operation after date has been set **/

			if (dayOfMonth < 10) {
				day = new StringBuilder().append("0").append(dayOfMonth)
						.toString();
			} else
				day = new StringBuilder().append(dayOfMonth).toString();

			if (monthOfYear < 10) {
				month = new StringBuilder().append("0").append(monthOfYear)
						.toString();
			} else
				month = new StringBuilder().append(monthOfYear).toString();

			combinedDate = (new StringBuilder()).append(Year).append("-")
					.append(month).append("-").append(day).toString(); // combinedDate
																		// ==
																		// yyyymmdd

			year = new StringBuilder().append(Year).toString();
		}
	};

	/*****************************************************************************************************
	 * For Time Picker Dialog
	 * **************************************************************************************************/
	TimePickerDialog.OnTimeSetListener timeSet = new TimePickerDialog.OnTimeSetListener() {

		public void onTimeSet(TimePicker view, int Hour, int min) {
			/** perform your required operation after time has been set **/

			if (Hour < 10)
				hour = new StringBuilder().append("0").append(Hour).toString();
			else
				hour = new StringBuilder().append(Hour).toString();

			if (min < 10)
				minute = new StringBuilder().append("0").append(min).toString();
			else {
				minute = new StringBuilder().append(min).toString();
			}
			combinedTime = (new StringBuilder()).append(hour).append(":")
					.append(minute).toString(); // combinedTime ==
												// HH:mm

		}
	};

}
