package com.b4a.transits;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import com.parse.ParseAnalytics;

import android.os.Bundle;
import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.util.Log;
import android.view.*;
import android.view.WindowManager.*;
import android.widget.*;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.B4AMenuItem;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.keywords.StringBuilderWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import anywheresoftware.b4a.objects.EditTextWrapper;
import anywheresoftware.b4a.sample1.SweDate;
import anywheresoftware.b4a.sample1.SwissEph;
import anywheresoftware.b4a.objects.*;
import anywheresoftware.b4a.objects.drawable.CanvasWrapper.*;
import anywheresoftware.b4a.objects.drawable.*;
import anywheresoftware.b4a.keywords.constants.*;
import anywheresoftware.b4a.objects.collections.*;
import anywheresoftware.b4a.*;
import anywheresoftware.b4a.agraham.dialogs.InputDialog.*;
import android.graphics.*;
import android.graphics.Paint.*;

public class MainActivity extends Activity implements B4AActivity {

	public static MainActivity mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
	private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
	ActivityWrapper _activity;
	ArrayList<B4AMenuItem> menuItems;
	private static final boolean fullScreen = false;
	private static final boolean includeTitle = true;
	public static WeakReference<Activity> previousOne;
	private Boolean onKeySubExist = null;
	private Boolean onKeyUpSubExist = null;

	public static final int INSERT_ID = Menu.FIRST;
	private static final int LOGIN_ID = Menu.FIRST + 1;
	private static final int LOGOUT_ID = Menu.FIRST + 2;
	private static final int SAVE_ID = Menu.FIRST + 3;
	private static final int ACTIVITY_CREATE = 0;
	private static final int LOGIN_CODE = 1;

	private String currentUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/*
		 * super.onCreate(savedInstanceState);
		 * setContentView(R.layout.activity_main);
		 */

		super.onCreate(savedInstanceState);
		if (isFirst) {
			processBA = new BA(this.getApplicationContext(), null, null,
					"com.b4a.transits", "MainActivity");
			processBA.loadHtSubs(this.getClass());
			float deviceScale = getApplicationContext().getResources()
					.getDisplayMetrics().density;
			BALayout.setDeviceScale(deviceScale);
		} else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
				Common.Log("Killing previous instance (MainActivity).");
				p.finish();
			}
		}

		if (!includeTitle) {
			this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		}

		if (fullScreen) {
			getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN,
					LayoutParams.FLAG_FULLSCREEN);
		}

		mostCurrent = this;
		processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
		BA.handler.postDelayed(new WaitForLayout(), 5);

		// Retrieve logged in user's info
		Intent intent = getIntent();
		currentUser = intent.getStringExtra("uname");
		if (currentUser.length() != 0)
			CustomToast.showToast(this, "You are logged in as," + currentUser);
		else
			CustomToast.showToast(this, "You are anonymous");

		// Track app opens.
		ParseAnalytics.trackAppOpened(getIntent());

	}

	@Override
	public void onPause() {
		super.onPause();
		if (_activity == null) // workaround for emulator bug (Issue 2423)
			return;

		Msgbox.dismiss(true);
		Common.Log("** Activity (MainActivity) Pause, UserClosed = "
				+ activityBA.activity.isFinishing() + " **");
		processBA.raiseEvent2(_activity, true, "activity_pause", false,
				activityBA.activity.isFinishing());
		processBA.setActivityPaused(true);
		mostCurrent = null;

		if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);

		Msgbox.isDismissing = false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		previousOne = null;
	}

	@Override
	public void onResume() {
		super.onResume();
		mostCurrent = this;
		Msgbox.isDismissing = false;
		if (activityBA != null) { // will be null during activity create (which
									// waits for AfterLayout).
			ResumeMessage rm = new ResumeMessage(mostCurrent);
			BA.handler.post(rm);
		}
	}

	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}

	private class B4AMenuItemsClickListener implements
			MenuItem.OnMenuItemClickListener {
		private final String eventName;

		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}

		public boolean onMenuItemClick(MenuItem item) {
			processBA.raiseEvent(item.getTitle(), eventName + "_click");
			return true;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");

		if (onKeySubExist) {
			Boolean res = (Boolean) processBA.raiseEvent2(_activity, false,
					"activity_keypress", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");

		if (onKeyUpSubExist) {
			Boolean res = (Boolean) processBA.raiseEvent2(_activity, false,
					"activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onNewIntent(Intent intent) {
		this.setIntent(intent);
	}

	/** Custom functions **/
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[]) null);
	}

	private static class WaitForLayout implements Runnable {

		public void run() {
			if (afterFirstLayout)
				return;

			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}

			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout
					.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout
					.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}

	private void afterFirstLayout() {
		activityBA = new BA(this, layout, processBA, "com.b4a.transits",
				"MainActivity");
		processBA.sharedProcessBA.activityBA = new WeakReference<BA>(activityBA);
		_activity = new ActivityWrapper(activityBA, "activity");
		Msgbox.isDismissing = false;
		initializeProcessGlobals();
		initializeGlobals();
		ViewWrapper.lastId = 0;
		Common.Log("** Activity (MainActivity) Create, isFirst = " + isFirst
				+ " **");
		processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;

		if (mostCurrent == null || mostCurrent != this)
			return;

		processBA.setActivityPaused(false);
		Common.Log("** Activity (MainActivity) Resume **");
		processBA.raiseEvent(null, "activity_resume");

	}

	public static Class<?> getObject() {
		return MainActivity.class;
	}

	/** Custom class **/
	private static class ResumeMessage implements Runnable {
		private final WeakReference<Activity> activity;

		public ResumeMessage(Activity activity) {
			this.activity = new WeakReference<Activity>(activity);
		}

		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
			Common.Log("** Activity (MainActivity) Resume **");
			processBA.raiseEvent(mostCurrent._activity, "activity_resume",
					(Object[]) null);
		}
	}

	/**
	 * I don't know part
	 * **/

	public Common __c = null;
	public static SweDate _vv3 = null;
	public static SwissEph _vvv2 = null;
	public static String[] _asp_names = null;
	public static String _last_interp_text = "";
	public static String[] _month_names = null;
	public static String _natal_data = "";
	public static String[] _p_names = null;
	public static String _prev_text = "";
	public static String _transit_data = "";
	public static String _natal_data_header = "";
	public static String _transit_data_header = "";
	public static int[][] _aspect_present = null;
	public static int _flag_help = 0;
	public static int[] _pl_glyph = null;
	public static int[] _sign_glyph = null;
	public static int[] _spot_filled = null;
	public static float _tz_offset = 0f;
	public static double _current_jd = 0;
	public static double _v0 = 0;
	public static double[] _vv4 = null;
	public static double[] _vvv1 = null;

	public ButtonWrapper _btnchange = null;
	public ButtonWrapper _btndate = null;
	public ButtonWrapper _btnhelp = null;
	public Button _btnprevious = null;
	public Button _btnnext = null;
	public Button _btnwheel_text = null;
	public ButtonWrapper _vvv3 = null;
	public CanvasWrapper.BitmapWrapper _v5 = null;
	public CanvasWrapper.BitmapWrapper _v6 = null;
	public EditTextWrapper _edittext1 = null;
	public CompoundButtonWrapper.CheckBoxWrapper _chkdebug = null;
	public CanvasWrapper _vv1 = null;
	public TypefaceWrapper _v7 = null;

	public static String _activity_create(boolean _firsttime) throws Exception {

		mostCurrent._activity.LoadLayout("Layout", mostCurrent.activityBA);
		mostCurrent._v5.Initialize(Common.File.getDirAssets(), "calendar.png");
		mostCurrent._v6.Initialize(Common.File.getDirAssets(), "clock.png");
		try {
			// mostCurrent._v7.setObject((Typeface)(Common.Typeface.LoadFromAssets("HamburgSymbols.ttf")));

			mostCurrent._v7.setObject((Typeface) (Common.Typeface.DEFAULT));
		} catch (Exception ex) {
			Log.v("Start mai zhoor raixa", "HMM: " + ex.getMessage());
		}
		_p_names[0] = "Sun";
		_p_names[1] = "Moon";
		_p_names[2] = "Mercury";
		_p_names[3] = "Venus";
		_p_names[4] = "Mars";
		_p_names[5] = "Jupiter";
		_p_names[6] = "Saturn";
		_p_names[7] = "Uranus";
		_p_names[8] = "Neptune";
		_p_names[9] = "Pluto";

		_asp_names[0] = "";
		_asp_names[1] = "";
		_asp_names[2] = " Conjunct ";
		_asp_names[3] = " Sextile ";
		_asp_names[4] = " Square ";
		_asp_names[5] = " Trine ";
		_asp_names[6] = " Opposite ";

		_month_names[1] = "Jan";
		_month_names[2] = "Feb";
		_month_names[3] = "Mar";
		_month_names[4] = "Apr";
		_month_names[5] = "May";
		_month_names[6] = "Jun";
		_month_names[7] = "Jul";
		_month_names[8] = "Aug";
		_month_names[9] = "Sep";
		_month_names[10] = "Oct";
		_month_names[11] = "Nov";
		_month_names[12] = "Dec";

		_pl_glyph[0] = 81;
		_pl_glyph[1] = 87;
		_pl_glyph[2] = 69;
		_pl_glyph[3] = 82;
		_pl_glyph[4] = 84;
		_pl_glyph[5] = 89;
		_pl_glyph[6] = 85;
		_pl_glyph[7] = 73;
		_pl_glyph[8] = 79;
		_pl_glyph[9] = 80;

		_sign_glyph[1] = 97;
		_sign_glyph[2] = 115;
		_sign_glyph[3] = 100;
		_sign_glyph[4] = 102;
		_sign_glyph[5] = 103;
		_sign_glyph[6] = 104;
		_sign_glyph[7] = 106;
		_sign_glyph[8] = 107;
		_sign_glyph[9] = 108;
		_sign_glyph[10] = 122;
		_sign_glyph[11] = 120;
		_sign_glyph[12] = 99;

		_v0 = 0.0174532;
		mostCurrent._vv1.Initialize((View) (mostCurrent._activity.getObject()));
		mostCurrent._edittext1.setInputType(EditTextWrapper.INPUT_TYPE_NONE);
		mostCurrent._edittext1.setSingleLine(false);
		mostCurrent._edittext1.setWrap(true);

		if (_firsttime == false) {
			mostCurrent._btnprevious.setEnabled(true);
			mostCurrent._btnnext.setEnabled(true);
			mostCurrent._btnwheel_text.setEnabled(true);
			mostCurrent._edittext1.setText(_last_interp_text);
		}
		;

		return "";
	}

	public static String _btnchange_click() throws Exception {
		try {
			DateDialog _dd = new DateDialog();
			TimeDialog _td = new TimeDialog();

			int _m = 0;
			int _d = 0;
			int _y = 0;
			int _h = 0;
			int _n = 0;
			List _list1 = new List();
			;
			float _tz = 0f;
			String _ret = "";

			mostCurrent._edittext1.setVisible(true);

			if (Common.File.Exists(Common.File.getDirInternal(),
					"natal_data.txt") == true) {

				_list1 = Common.File.ReadList(Common.File.getDirInternal(),
						"natal_data.txt");
				_m = (int) (BA.ObjectToNumber(_list1.Get(0)));
				_d = (int) (BA.ObjectToNumber(_list1.Get(1)));
				_y = (int) (BA.ObjectToNumber(_list1.Get(2)));
				_h = (int) (BA.ObjectToNumber(_list1.Get(3)));
				_n = (int) (BA.ObjectToNumber(_list1.Get(4)));
				_tz = (float) (BA.ObjectToNumber(_list1.Get(5)));
				_dd.setYear(_y);
				_dd.setMonth(_m);
				_dd.setDayOfMonth(_d);
				_ret = BA.NumberToString(_dd.Show("Set your birth date",
						"Tell me your birth date", "OK", "", "",
						mostCurrent.activityBA,
						(Bitmap) (mostCurrent._v5.getObject())));

				_td.setHour(_h);
				_td.setMinute(_n);
				_td.setIs24Hours(true);
				_ret = BA.NumberToString(_td.Show(
						"Set your birth time (use 24-hr time)",
						"Tell me your birth time", "OK", "", "",
						mostCurrent.activityBA,
						(Bitmap) (mostCurrent._v6.getObject())));
			} else {
				_dd.setYear(Common.DateTime.GetYear(Common.DateTime.getNow()));
				_dd.setMonth(Common.DateTime.GetMonth(Common.DateTime.getNow()));
				_dd.setDayOfMonth(Common.DateTime.GetDayOfMonth(Common.DateTime
						.getNow()));
				_ret = BA.NumberToString(_dd.Show("Set your birth date",
						"Tell me your birth date", "OK", "", "",
						mostCurrent.activityBA,
						(Bitmap) (mostCurrent._v5.getObject())));
				_td.setHour(Common.DateTime.GetHour(Common.DateTime.getNow()));
				_td.setMinute(Common.DateTime.GetMinute(Common.DateTime
						.getNow()));
				_td.setIs24Hours(true);
				_ret = BA.NumberToString(_td.Show(
						"Set your birth time (use 24-hr time)",
						"Tell me your birth time", "OK", "", "",
						mostCurrent.activityBA,
						(Bitmap) (mostCurrent._v6.getObject())));
			}
			;

			_m = _dd.getMonth();
			_d = _dd.getDayOfMonth();
			_y = _dd.getYear();
			_h = _td.getHour();
			_n = _td.getMinute();
			_tz = (float) (Double.parseDouble(_vv2(_tz)));

			_list1.Initialize();
			_list1.Add((Object) (_m));
			_list1.Add((Object) (_d));
			_list1.Add((Object) (_y));
			_list1.Add((Object) (_h));
			_list1.Add((Object) (_n));
			_list1.Add((Object) (_tz));
			Common.File.WriteList(Common.File.getDirInternal(),
					"natal_data.txt", _list1);
		} catch (Exception ex) {
			Log.v("ERROR AT BTN_CHANGE_CLICK", "Err found: " + ex.getMessage());
		}
		return "";
	}

	public static String _btndate_click() throws Exception {
		try {
			DateDialog _dd = new DateDialog();
			TimeDialog _td = new TimeDialog();
			float _tod = 0f;
			double _da = 0;
			double _jd = 0;
			int _m = 0;
			int _d = 0;
			int _y = 0;
			int _h = 0;
			int _n = 0;
			List _list1 = new List();
			;
			String _ret = "";
			String _tz = "";
			String _natal_date = "";
			String _natal_time = "";
			int _i = 0;

			mostCurrent._edittext1.setVisible(true);

			if (Common.File.Exists(Common.File.getDirInternal(),
					"natal_data.txt") == false) {
				Log.v("natal_data.txt NOT FOUND", "IMP file missing");
				_dd.setYear(Common.DateTime.GetYear(Common.DateTime.getNow()));
				_dd.setMonth(Common.DateTime.GetMonth(Common.DateTime.getNow()));
				_dd.setDayOfMonth(Common.DateTime.GetDayOfMonth(Common.DateTime
						.getNow()));
				_ret = BA.NumberToString(_dd.Show("Set your birth date",
						"Tell me your birth date", "OK", "", "",
						mostCurrent.activityBA,
						(Bitmap) (mostCurrent._v5.getObject())));
				_td.setHour(Common.DateTime.GetHour(Common.DateTime.getNow()));
				_td.setMinute(Common.DateTime.GetMinute(Common.DateTime
						.getNow()));
				_td.setIs24Hours(Common.True);
				_ret = BA.NumberToString(_td.Show(
						"Set your birth time (use 24-hr time)",
						"Tell me your birth time", "OK", "", "",
						mostCurrent.activityBA,
						(Bitmap) (mostCurrent._v6.getObject())));
				_m = _dd.getMonth();
				_d = _dd.getDayOfMonth();
				_y = _dd.getYear();
				_h = _td.getHour();
				_n = _td.getMinute();
				_tz = _vv2((float) (0));
				_list1.Initialize();
				_list1.Add((Object) (_m));
				_list1.Add((Object) (_d));
				_list1.Add((Object) (_y));
				_list1.Add((Object) (_h));
				_list1.Add((Object) (_n));
				_list1.Add((Object) (_tz));

				Common.File.WriteList(Common.File.getDirInternal(),
						"natal_data.txt", _list1);
			} else {
				_list1 = Common.File.ReadList(Common.File.getDirInternal(),
						"natal_data.txt");
				_m = (int) (BA.ObjectToNumber(_list1.Get(0)));
				_d = (int) (BA.ObjectToNumber(_list1.Get(1)));
				_y = (int) (BA.ObjectToNumber(_list1.Get(2)));
				_h = (int) (BA.ObjectToNumber(_list1.Get(3)));
				_n = (int) (BA.ObjectToNumber(_list1.Get(4)));
				_tz = String.valueOf(_list1.Get(5));
			}
			;

			_tod = (float) (_h + _n / (double) 60);
			_jd = _vv3.getJulDay(_y, _m, _d, _tod);
			_jd = _jd - ((double) (Double.parseDouble(_tz)) / (double) 24);

			mostCurrent._edittext1
					.setInputType(mostCurrent._edittext1.INPUT_TYPE_NONE);
			mostCurrent._edittext1.setSingleLine(Common.False);
			mostCurrent._edittext1.setWrap(Common.True);
			mostCurrent._edittext1.setText((Object) (""));

			_natal_date = Common.NumberFormat(_d, 2, 0) + " "
					+ _month_names[_m] + " " + BA.NumberToString(_y);
			_natal_time = BA.NumberToString(_h)
					+ ":"
					+ anywheresoftware.b4a.keywords.Common.NumberFormat(_n, 2,
							0);
			_natal_data_header = "   Natal date - " + _natal_date + " at "
					+ _natal_time + " (tz=" + _tz + ")";
			_natal_data = "";
			{
				final double step148 = 1;
				final double limit148 = (int) (9);
				for (_i = 0; (step148 > 0 && _i <= limit148)
						|| (step148 < 0 && _i >= limit148); _i += step148) {
					_vv4[_i] = _get_1_planet(_jd, _i);
					_natal_data = _natal_data + _p_names[_i] + " = "
							+ _convert_longitude((float) (_vv4[_i]))
							+ "   -   "
							+ Common.NumberFormat2(_vv4[_i], 1, 4, 4, false)
							+ Common.CRLF;
				}
			}
			;

			_dd.setYear(Common.DateTime.GetYear(Common.DateTime.getNow()));
			_dd.setMonth(Common.DateTime.GetMonth(Common.DateTime.getNow()));
			_dd.setDayOfMonth(Common.DateTime.GetDayOfMonth(Common.DateTime
					.getNow()));
			_ret = BA.NumberToString(_dd.Show("Set the transit date",
					"What transit date do you want?", "OK", "", "",
					mostCurrent.activityBA,
					(Bitmap) (mostCurrent._v5.getObject())));

			_td.setHour(Common.DateTime.GetHour(Common.DateTime.getNow()));
			_td.setMinute(Common.DateTime.GetMinute(Common.DateTime.getNow()));
			_td.setIs24Hours(Common.True);
			_ret = BA.NumberToString(_td.Show(
					"Set the transit time (use 24-hr time)",
					"What transit time do you want to use?", "OK", "", "",
					mostCurrent.activityBA,
					(android.graphics.Bitmap) (mostCurrent._v6.getObject())));
			_tz_offset = (float) (Double.parseDouble(_vv5()));
			_tod = (float) (_td.getHour() + _td.getMinute() / (double) 60);
			_jd = _vv3.getJulDay(_dd.getYear(), _dd.getMonth(),
					_dd.getDayOfMonth(), _tod);
			_jd = _jd - (_tz_offset / (double) 24);
			_current_jd = _jd;
			_get_transit_interps_for_this_day(_current_jd);
			mostCurrent._btnprevious.setEnabled(true);
			mostCurrent._btnnext.setEnabled(true);
			mostCurrent._btnwheel_text.setEnabled(true);
			mostCurrent._btnwheel_text.setText("Wheel");
		} catch (Exception ex) {
			Log.v("ERROR AT BTN_DATE_CLICK",
					"Error found at: " + ex.getMessage());
		}
		return "";
	}

	/**
	 * Triggered when the Help Button is Clicked
	 * **/
	public static String _btnhelp_click() throws Exception {
		try {
			mostCurrent._edittext1.setVisible(true);
			mostCurrent._btnwheel_text.setText("Wheel");

			if (_flag_help == 0) {
				_flag_help = 1;
				_prev_text = mostCurrent._edittext1.getText();
			} else {
				_flag_help = 0;
			}
			;

			if (_flag_help == 1) {
				if (Common.File.Exists(Common.File.getDirAssets(), "help.txt") == true) {
					mostCurrent._edittext1.setText(Common.File.ReadString(
							Common.File.getDirAssets(), "help.txt"));
					mostCurrent._btnprevious.setEnabled(false);
					mostCurrent._btnnext.setEnabled(false);
				}
				;
			} else {

				mostCurrent._edittext1.setText(_prev_text);
				mostCurrent._btnprevious.setEnabled(true);
				mostCurrent._btnnext.setEnabled(true);

			}
			;
		} catch (Exception ex) {
			Log.v("SOMETHING WENT WRONG :(", "Error at:" + ex.getMessage());
		}
		return "";
	}

	public static String _btnnext_click() throws Exception {
		if (_current_jd == 0) {
			if (true)
				return "";
		}
		;

		_current_jd = _current_jd + 1;
		_get_transit_interps_for_this_day(_current_jd);
		if ((mostCurrent._btnwheel_text.getText()).equals("Text")) {
			_display_wheel();
		}
		;
		return "";
	}

	public static String _btnprevious_click() throws Exception {
		if (_current_jd == 0) {
			if (true)
				return "";
		}
		;

		_current_jd = _current_jd - 1;
		_get_transit_interps_for_this_day(_current_jd);

		if ((mostCurrent._btnwheel_text.getText()).equals("Text")) {
			_display_wheel();
		}
		;
		return "";
	}

	public static String _btnwheel_text_click() throws Exception {
		if ((mostCurrent._btnwheel_text.getText()).equals("Wheel")) {
			mostCurrent._edittext1.setVisible(Common.False);
			mostCurrent._btnwheel_text.setText("Text");
			_display_wheel();
		} else {
			mostCurrent._edittext1.setVisible(Common.True);
			mostCurrent._btnwheel_text.setText("Wheel");
		}
		;
		return "";
	}

	public static boolean _check_for_overlap(float _angle, int _spacing)
			throws Exception {
		boolean _result = false;
		int _i = 0;
		_result = false;
		_result = Common.False;
		{
			final double step703 = 1;
			final double limit703 = (int) (_angle + _spacing);
			for (_i = (int) (_angle - _spacing); (step703 > 0 && _i <= limit703)
					|| (step703 < 0 && _i >= limit703); _i += step703) {
				if (_spot_filled[(int) (_vv6(Common.Round(_i)))] == 1) {
					_result = Common.True;
					if (true)
						break;
				}
				;
			}
		}
		;
		if (true)
			return _result;

		return false;
	}

	public static String _convert_longitude(float _longitude) throws Exception {
		String[] _signs = null;
		int _deg = 0;
		int _mn = 0;
		int _sign_num = 0;
		float _full_mn = 0f;
		float _pos_in_sign = 0f;
		String _full_sec = "";
		_signs = new String[(int) (12)];
		Arrays.fill(_signs, "");
		_deg = 0;
		_mn = 0;
		_sign_num = 0;
		_full_mn = 0f;
		_pos_in_sign = 0f;
		_signs[0] = "Ari";
		_signs[1] = "Tau";
		_signs[2] = "Gem";
		_signs[3] = "Can";
		_signs[4] = "Leo";
		_signs[5] = "Vir";
		_signs[6] = "Lib";
		_signs[7] = "Sco";
		_signs[8] = "Sag";
		_signs[9] = "Cap";
		_signs[10] = "Aqu";
		_signs[11] = "Pis";
		_sign_num = (int) (Common.Floor(_longitude / (double) 30));
		_pos_in_sign = (float) (_longitude - (_sign_num * 30));
		_deg = (int) (Common.Floor(_pos_in_sign));
		_full_mn = (float) ((_pos_in_sign - _deg) * 60);
		_mn = (int) (Common.Floor(_full_mn));
		_full_sec = BA.NumberToString(Common.Round((_full_mn - _mn) * 60));
		if (true)
			return Common.NumberFormat(_deg, (int) (2), (int) (0)) + " "
					+ _signs[_sign_num] + " "
					+ Common.NumberFormat(_mn, (int) (2), (int) (0)) + "'";

		return "";
	}

	public static String _vv7(double _jdx) throws Exception {
		double _fraction = 0;
		double _hh = 0;
		double _jd_to_use = 0;
		double _mm = 0;
		String _dt_string = "";
		_fraction = 0;
		_hh = 0;
		_jd_to_use = 0;
		_mm = 0;
		_dt_string = "";
		_jd_to_use = _jdx + _tz_offset / (double) 24;
		_dt_string = _vv0(_jd_to_use);
		_fraction = _jd_to_use - Common.Floor(_jd_to_use);
		if ((_fraction < 0.5)) {
			_fraction = _fraction + 0.5;
		} else {
			_fraction = _fraction - 0.5;
		}
		;
		_hh = _fraction * 24;
		_mm = _hh - Common.Floor(_hh);
		if (true)
			return "Transit date - " + _dt_string + " at "
					+ Common.NumberFormat(Common.Floor(_hh), 2, 0) + ":"
					+ Common.NumberFormat(_mm * 60, 2, 0) + " (tz="
					+ Common.NumberFormat2(_tz_offset, 1, 1, 1, Common.False)
					+ ")";
		return "";
	}

	public static double _vv6(double _x) throws Exception {
		if (_x >= 360) {
			if (true)
				return _x - 360;
		} else if (_x < 0) {
			if (true)
				return _x + 360;
		} else {
			if (true)
				return _x;
		}
		;
		return 0;
	}

	public static float[] _display_planet_glyph(float _our_angle,
			float _angle_to_use, int _radii) throws Exception {
		int _ch_pl_glyph = 0;
		int _cw_pl_glyph = 0;
		int _gap_pl_glyph = 0;
		float _center_pos_x = 0f;
		float _center_pos_y = 0f;
		float _offset_pos_x = 0f;
		float _offset_pos_y = 0f;
		float _this_angle = 0f;
		float[] _xy = null;
		_ch_pl_glyph = 0;
		_cw_pl_glyph = 0;
		_gap_pl_glyph = 0;
		_center_pos_x = 0f;
		_center_pos_y = 0f;
		_offset_pos_x = 0f;
		_offset_pos_y = 0f;
		_this_angle = 0f;
		_xy = new float[(int) (2)];
		;

		_this_angle = (float) (_vv6(_our_angle));
		if (_this_angle >= 1 && _this_angle <= 181) {
			_cw_pl_glyph = (int) (32);
			_ch_pl_glyph = (int) (32);
			_gap_pl_glyph = (int) (-10);
		} else {
			_cw_pl_glyph = (int) (28);
			_ch_pl_glyph = (int) (32);
			_gap_pl_glyph = (int) (-14);
		}
		;

		_center_pos_x = (float) (-_cw_pl_glyph / (double) 2);
		_center_pos_y = (float) (_ch_pl_glyph / (double) 2);
		_offset_pos_x = (float) (_center_pos_x * Common.Cos(_angle_to_use));
		_offset_pos_y = (float) (_center_pos_y * Common.Sin(_angle_to_use));
		_xy[(int) (0)] = (float) (_center_pos_x + _offset_pos_x + ((-_radii + _gap_pl_glyph) * Common
				.Cos(_angle_to_use)));
		_xy[(int) (1)] = (float) (_center_pos_y + _offset_pos_y + ((_radii - _gap_pl_glyph) * Common
				.Sin(_angle_to_use)));
		if (true)
			return _xy;
		return null;
	}

	public static String _display_wheel() throws Exception {
		int _center_pt_x = 0;
		int _center_pt_y = 0;
		int _cw_sign_glyph = 0;
		int _ch_sign_glyph = 0;
		int _gap_sign_glyph = 0;
		int _i = 0, _inner_radius = 0, _j = 0;
		int[] _nopih = null;
		int _radius = 0;
		int[] _sort_pos = null;
		int _temp1 = 0, _cnt = 0, _house_num = 0, _house_of_pl = 0, _how_many_more_can_fit_in_this_house = 0, _pl_num = 0, _planets_done = 0;
		int[] _sp = null;
		int _spacing = 0, _start_planet = 0, _start_planet_idx = 0, _ah = 0, _aw = 0, _offset_distance = 0;
		int[][] _planet_angle = null;
		int _q = 0;
		float _angle_to_use = 0f, _center_pos_x = 0f, _center_pos_y = 0f, _da = 0f, _offset_pos_x = 0f, _offset_pos_y = 0f;
		float _x1 = 0f, _x2 = 0f;
		float[] _xy = null;
		float _y1 = 0f, _y2 = 0f;
		double _angle = 0, _from_cusp = 0, _to_next_cusp = 0, _next_cusp = 0, _our_angle = 0, _temp = 0;
		;
		double[] _s = null;
		double[] _sort = null;
		int _kk = 0;

		_center_pt_x = 0;
		_center_pt_y = 0;
		_cw_sign_glyph = 0;
		_ch_sign_glyph = 0;
		_gap_sign_glyph = 0;
		_i = 0;
		_inner_radius = 0;
		_j = 0;
		_nopih = new int[(int) (13)];
		;
		_radius = 0;
		_sort_pos = new int[(int) (10)];
		;
		_temp1 = 0;

		_cnt = 0;
		_house_num = 0;
		_house_of_pl = 0;
		_how_many_more_can_fit_in_this_house = 0;
		_pl_num = 0;
		_planets_done = 0;
		_sp = new int[(int) (10)];
		;

		_spacing = 0;
		_start_planet = 0;
		_start_planet_idx = 0;
		_ah = 0;
		_aw = 0;
		_offset_distance = 0;
		_planet_angle = new int[(int) (2)][];
		{
			int d0 = _planet_angle.length;
			int d1 = 10;
			for (int i0 = 0; i0 < d0; i0++) {
				_planet_angle[i0] = new int[d1];
			}
		}
		;

		_q = 0;
		_angle_to_use = 0f;
		_center_pos_x = 0f;
		_center_pos_y = 0f;
		_da = 0f;
		_offset_pos_x = 0f;
		_offset_pos_y = 0f;
		_x1 = 0f;
		_x2 = 0f;
		_xy = new float[2];
		;

		_y1 = 0f;
		_y2 = 0f;
		_angle = 0;
		_from_cusp = 0;
		_to_next_cusp = 0;
		_next_cusp = 0;
		_our_angle = 0;
		_s = new double[10];
		;

		_sort = new double[10];
		;

		_temp = 0;
		_ah = mostCurrent._activity.getHeight();
		_aw = mostCurrent._activity.getWidth();
		if (_aw > _ah || _ah == 0 || _aw == 0) {
			mostCurrent._vv1.DrawText(mostCurrent.activityBA,
					"The wheel is not viewable in landscape mode.",
					(float) (50), (float) (104), Common.Typeface.DEFAULT,
					(float) (18), Common.Colors.White, BA.getEnumFromString(
							android.graphics.Paint.Align.class, "LEFT"));
			if (true)
				return "";
		}
		;

		if (_vv4[(int) (0)] == 0 && _vv4[(int) (1)] == 0
				&& _vvv1[(int) (0)] == 0 && _vvv1[(int) (1)] == 0
				&& _vv4[(int) (9)] == 0 && _vvv1[(int) (9)] == 0) {
			if (true)
				return "";
		}
		;
		_spacing = (int) (4);
		if (mostCurrent._edittext1.getVisible() == Common.False) {
			mostCurrent._vv1.DrawColor(Common.Colors.Black);
			mostCurrent._activity.Invalidate();
			_center_pt_x = (int) (300);
			_center_pt_y = (int) (452);
			_radius = (int) (250);
			_inner_radius = (int) (50);

			mostCurrent._vv1.DrawText(mostCurrent.activityBA,
					_transit_data_header, (float) (50), (float) (80),
					Common.Typeface.DEFAULT, (float) (18), Common.Colors.White,
					BA.getEnumFromString(Align.class, "LEFT"));
			mostCurrent._vv1.DrawText(mostCurrent.activityBA,
					_natal_data_header, (float) (50), (float) (104),
					Common.Typeface.DEFAULT, (float) (18), Common.Colors.White,
					BA.getEnumFromString(Align.class, "LEFT"));
			mostCurrent._vv1.DrawText(mostCurrent.activityBA,
					"Transit planets in", (float) (50), (float) (850),
					Common.Typeface.DEFAULT, (float) (18), Common.Colors.White,
					BA.getEnumFromString(Align.class, "LEFT"));
			mostCurrent._vv1.DrawText(mostCurrent.activityBA,
					"Natal planets in", (float) (50), (float) (874),
					Common.Typeface.DEFAULT, (float) (18), Common.Colors.White,
					BA.getEnumFromString(Align.class, "LEFT"));
			mostCurrent._vv1.DrawText(mostCurrent.activityBA, "magenta",
					(float) (200), (float) (850), Common.Typeface.DEFAULT,
					(float) (18), Common.Colors.Magenta,
					BA.getEnumFromString(Align.class, "LEFT"));
			mostCurrent._vv1.DrawText(mostCurrent.activityBA, "cyan",
					(float) (190), (float) (874), Common.Typeface.DEFAULT,
					(float) (18), Common.Colors.Cyan,
					BA.getEnumFromString(Align.class, "LEFT"));

			{
				final double step256 = 30, limit256 = 330;
				for (_i = (int) (0); (step256 > 0 && _i <= limit256)
						|| (step256 < 0 && _i >= limit256); _i += step256) {

					_x1 = (float) (_radius * Common.Cos(_v0 * _i));
					_y1 = (float) (_radius * Common.Sin(_v0 * _i));
					_x2 = (float) ((_inner_radius) * Common.Cos(_v0 * _i));
					_y2 = (float) ((_inner_radius) * Common.Sin(_v0 * _i));

					mostCurrent._vv1.DrawLine((float) (_x1 + _center_pt_x),
							(float) (_y1 + _center_pt_y),
							(float) (_x2 + _center_pt_x),
							(float) (_y2 + _center_pt_y), Common.Colors.Gray,
							(float) (Common.DipToCurrent(1)));
				}
			}
			;

			mostCurrent._vv1.DrawCircle((float) (_center_pt_x),
					(float) (_center_pt_y), (float) (_radius),
					Common.Colors.White, Common.False,
					(float) (Common.DipToCurrent((int) (2))));
			mostCurrent._vv1.DrawCircle((float) (_center_pt_x),
					(float) (_center_pt_y), (float) (_inner_radius),
					Common.Colors.White, Common.False,
					(float) (Common.DipToCurrent((int) (2))));
			_cw_sign_glyph = (int) (28);
			_ch_sign_glyph = (int) (28);
			_gap_sign_glyph = (int) (-32);
			{
				final double step268 = 1, limit268 = 12;
				for (_i = 1; (step268 > 0 && _i <= limit268)
						|| (step268 < 0 && _i >= limit268); _i += step268) {
					_angle_to_use = (float) (_v0 * (((_i - 1) * 30) + 15));
					_center_pos_x = (float) (-_cw_sign_glyph / (double) 2);
					_center_pos_y = (float) (_ch_sign_glyph / (double) 2);
					_offset_pos_x = (float) (_center_pos_x * Common
							.Cos(_angle_to_use));
					_offset_pos_y = (float) (_center_pos_y * Common
							.Sin(_angle_to_use));
					_x1 = (float) (_center_pos_x + _offset_pos_x + ((-_radius + _gap_sign_glyph) * Common
							.Cos(_angle_to_use)));
					_y1 = (float) (_center_pos_y + _offset_pos_y + ((_radius - _gap_sign_glyph) * Common
							.Sin(_angle_to_use)));
					mostCurrent._vv1
							.DrawText(
									mostCurrent.activityBA,
									String.valueOf(Common.Chr(_sign_glyph[_i])),
									(float) (_x1 + _center_pt_x),
									(float) (_y1 + _center_pt_y),
									(android.graphics.Typeface) (mostCurrent._v7
											.getObject()), (float) (28),
									Common.Colors.Green, BA.getEnumFromString(
											android.graphics.Paint.Align.class,
											"LEFT"));
				}
			}
			;

			{
				final double step278 = 1, limit278 = 1;
				for (_kk = (int) (0); (step278 > 0 && _kk <= limit278)
						|| (step278 < 0 && _kk >= limit278); _kk += step278) {
					{
						final double step279 = 1;
						final double limit279 = (int) (9);
						for (_i = (int) (0); (step279 > 0 && _i <= limit279)
								|| (step279 < 0 && _i >= limit279); _i += step279) {
							if (_kk == 0) {
								_sort[_i] = _vv4[_i];
							} else {
								_sort[_i] = _vvv1[_i];
							}
							;
							_sort_pos[_i] = _i;
						}
					}
					;

					{
						final double step287 = 1, limit287 = (int) (8);
						for (_i = 0; (step287 > 0 && _i <= limit287)
								|| (step287 < 0 && _i >= limit287); _i += step287) {
							{
								final double step288 = 1;
								final double limit288 = 9;
								for (_j = _i + 1; (step288 > 0 && _j <= limit288)
										|| (step288 < 0 && _j >= limit288); _j += step288) {
									if (_sort[_j] > _sort[_i]) {
										_temp = _sort[_i];
										_temp1 = _sort_pos[_i];
										_sort[_i] = _sort[_j];
										_sort_pos[_i] = _sort_pos[_j];
										_sort[_j] = _temp;
										_sort_pos[_j] = _temp1;
									}
									;
								}
							}
							;
						}
					}
					;

					{
						final double step299 = 1;
						final double limit299 = (int) (12);
						for (_i = 1; (step299 > 0 && _i <= limit299)
								|| (step299 < 0 && _i >= limit299); _i += step299) {
							_nopih[_i] = (int) (0);
						}
					}
					;

					{
						final double step302 = 1, limit302 = (int) (9);
						for (_i = 0; (step302 > 0 && _i <= limit302)
								|| (step302 < 0 && _i >= limit302); _i += step302) {
							if (_kk == 0) {
								_temp = Common.Floor(_vv4[_sort_pos[_i]]
										/ (double) 30) + 1;
							} else {
								_temp = Common.Floor(_vvv1[_sort_pos[_i]]
										/ (double) 30) + 1;
							}
							;
							_nopih[(int) (_temp)] = (int) (_nopih[(int) (_temp)] + 1);
						}
					}
					;
					{
						final double step310 = -1, limit310 = 0;
						for (_i = 8; (step310 > 0 && _i <= limit310)
								|| (step310 < 0 && _i >= limit310); _i += step310) {
							if (_sort[_i] - _sort[(int) (_i + 1)] >= 20) {
								_pl_num = _sort_pos[_i];
								if (_kk == 0) {
									_house_of_pl = (int) (Common
											.Floor(_vv4[_pl_num] / (double) 30) + 1);
								} else {
									_house_of_pl = (int) (Common
											.Floor(_vvv1[_pl_num] / (double) 30) + 1);
								}
								;
								if (_nopih[_house_of_pl] == 1) {
									_start_planet = _pl_num;
									_start_planet_idx = _i;
									if (true)
										break;
								}
								;
							}
							;
						}
					}
					;

					if (_i >= 0) {
						_cnt = 9;

						{
							final double step327 = -1, limit327 = 0;
							for (_i = _start_planet_idx; (step327 > 0 && _i <= limit327)
									|| (step327 < 0 && _i >= limit327); _i += step327) {
								_sp[_cnt] = _sort_pos[_i];
								_s[_cnt] = _sort[_i];
								_cnt = (int) (_cnt - 1);
							}
						}
						;

						{
							final double step332 = -1, limit332 = (int) (_start_planet_idx + 1);
							for (_i = 9; (step332 > 0 && _i <= limit332)
									|| (step332 < 0 && _i >= limit332); _i += step332) {
								_sp[_cnt] = _sort_pos[_i];
								_s[_cnt] = _sort[_i];
								_cnt = (int) (_cnt - 1);
							}
						}
						;

						{
							final double step337 = 1, limit337 = 9;
							for (_i = 0; (step337 > 0 && _i <= limit337)
									|| (step337 < 0 && _i >= limit337); _i += step337) {
								_sort_pos[_i] = _sp[_i];
								_sort[_i] = _s[_i];
							}
						}
						;
					}
					;

					{
						final double step342 = 1;
						final double limit342 = (int) (359);
						for (_i = 0; (step342 > 0 && _i <= limit342)
								|| (step342 < 0 && _i >= limit342); _i += step342) {

							_spot_filled[_i] = 0;
						}
					}
					;

					_house_num = 0;

					{
						final double step346 = -1;
						final double limit346 = (int) (0);
						for (_i = 9; (step346 > 0 && _i <= limit346)
								|| (step346 < 0 && _i >= limit346); _i += step346) {
							_temp = _house_num;
							if (_kk == 0) {
								_house_num = (int) (Common
										.Floor(_vv4[_sort_pos[_i]]
												/ (double) 30) + 1);
								_offset_distance = 44;
							} else {
								_house_num = (int) (Common
										.Floor(_vvv1[_sort_pos[_i]]
												/ (double) 30) + 1);
								_offset_distance = 12;
							}
							;

							if (_temp != _house_num) {
								_planets_done = 1;
							}
							;
							_from_cusp = _vv6(_sort[_i]
									- ((_house_num - 1) * 30));
							_to_next_cusp = _vv6((_house_num * 30) - _sort[_i]);
							_next_cusp = _house_num * 30;
							_angle = _sort[_i];
							_how_many_more_can_fit_in_this_house = (int) (Common
									.Floor(_to_next_cusp
											/ (double) (_spacing + 1)));
							if (_nopih[_house_num] - _planets_done >= _how_many_more_can_fit_in_this_house) {
								_angle = _vv6(_next_cusp
										- ((_nopih[_house_num] - _planets_done + 1) * (_spacing + 1)));
							}
							;
							while (_check_for_overlap((float) (_angle),
									_spacing) == Common.True) {
								_angle = _vv6(_angle + 1);
							}

							_spot_filled[(int) (Common.Round(_angle))] = 1;
							_spot_filled[(int) (_vv6(Common.Round(_angle) - 1))] = (int) (1);
							_planet_angle[_kk][_sort_pos[_i]] = (int) (_angle);
							_our_angle = _angle;
							_angle_to_use = (float) (_v0 * _our_angle);
							_planets_done = (int) (_planets_done + 1);
							_xy = _display_planet_glyph((float) (_our_angle),
									_angle_to_use,
									(int) (_radius - _offset_distance));
							if (_kk == 0) {
								mostCurrent._vv1
										.DrawText(
												mostCurrent.activityBA,
												String.valueOf(Common
														.Chr(_pl_glyph[_sort_pos[_i]])),
												(float) (_xy[(int) (0)] + _center_pt_x),
												(float) (_xy[(int) (1)] + _center_pt_y),
												(android.graphics.Typeface) (mostCurrent._v7
														.getObject()),
												(float) (32),
												Common.Colors.Cyan,
												BA.getEnumFromString(
														android.graphics.Paint.Align.class,
														"LEFT"));
							} else {
								mostCurrent._vv1
										.DrawText(
												mostCurrent.activityBA,
												String.valueOf(Common
														.Chr(_pl_glyph[_sort_pos[_i]])),
												(float) (_xy[(int) (0)] + _center_pt_x),
												(float) (_xy[(int) (1)] + _center_pt_y),
												(android.graphics.Typeface) (mostCurrent._v7
														.getObject()),
												(float) (32),
												Common.Colors.Magenta,
												BA.getEnumFromString(
														android.graphics.Paint.Align.class,
														"LEFT"));
							}
							;
						}
					}
					;
				}
			}
			;

			{
				final double step381 = 1, limit381 = 5;
				for (_i = 0; (step381 > 0 && _i <= limit381)
						|| (step381 < 0 && _i >= limit381); _i += step381) {
					{
						final double step382 = 1;
						final double limit382 = 9;
						for (_j = 0; (step382 > 0 && _j <= limit382)
								|| (step382 < 0 && _j >= limit382); _j += step382) {
							_q = _aspect_present[_i][_j];
							if (_q > 1) {
								_inner_radius = (int) (30);
								_x1 = (float) ((-_radius - 10) * Common.Cos(_v0
										* _planet_angle[(int) (1)][_i]));
								_y1 = (float) ((_radius + 10) * Common.Sin(_v0
										* _planet_angle[(int) (1)][_i]));
								_x2 = (float) ((-_radius + _inner_radius) * Common
										.Cos(_v0 * _planet_angle[(int) (0)][_j]));
								_y2 = (float) ((_radius - _inner_radius) * Common
										.Sin(_v0 * _planet_angle[(int) (0)][_j]));
								if (_q == 3 || _q == 5) {
									mostCurrent._vv1.DrawLine(
											(float) (_x1 + _center_pt_x),
											(float) (_y1 + _center_pt_y),
											(float) (_x2 + _center_pt_x),
											(float) (_y2 + _center_pt_y),
											Common.Colors.Green,
											(float) (Common
													.DipToCurrent((int) (2))));
								} else if (_q == 4 || _q == 6) {
									mostCurrent._vv1.DrawLine(
											(float) (_x1 + _center_pt_x),
											(float) (_y1 + _center_pt_y),
											(float) (_x2 + _center_pt_x),
											(float) (_y2 + _center_pt_y),
											Common.Colors.Red, (float) (Common
													.DipToCurrent((int) (2))));
								} else if (_q == 2) {
									mostCurrent._vv1.DrawLine(
											(float) (_x1 + _center_pt_x),
											(float) (_y1 + _center_pt_y),
											(float) (_x2 + _center_pt_x),
											(float) (_y2 + _center_pt_y),
											Common.Colors.Yellow,
											(float) (Common
													.DipToCurrent((int) (4))));
								}
								;
							}
							;
						}
					}
					;
				}
			}
			;
		}
		;
		return "";
	}

	public static String _find_specific_report_paragraph(
			String _phrase_to_look_for, String _filename) throws Exception {
		String _txt = "";
		int _n = 0;
		int _x = 0;
		int _y = 0;
		_txt = "";
		_n = 0;
		_x = 0;
		_y = 0;
		if (Common.File.Exists(Common.File.getDirAssets(), _filename) == Common.False) {
			if (true)
				return "";
		}
		;
		_txt = Common.File.ReadString(Common.File.getDirAssets(), _filename);
		_x = _txt.indexOf(_phrase_to_look_for);
		if (_x == -1) {
			if (true)
				return "";
		}
		;
		_y = _txt.indexOf("*", _x);
		_n = (int) (_y - _x - 1);
		if (true)
			return _txt.substring((int) (_x - 1), (int) (_x + _n - 1));
		return "";
	}

	public static double _get_1_planet(double _jd, int _p_num) throws Exception {
		double[] _xx = null;
		StringBuilderWrapper _serr = null;
		int _iflag = 0, _ret_flag = 0, _i = 0;
		_xx = new double[7];

		_serr = new StringBuilderWrapper();
		_iflag = 0;
		_ret_flag = 0;
		_serr.Initialize();
		{
			final double step443 = 1;
			final double limit443 = (int) (255);
			for (_i = 1; (step443 > 0 && _i <= limit443)
					|| (step443 < 0 && _i >= limit443); _i += step443) {
				_serr.Append("0");
			}
		}
		;
		_iflag = (int) (2 + 256);
		_ret_flag = _vvv2.swe_calc_ut(_jd, _p_num, _iflag, _xx,
				(java.lang.StringBuilder) (_serr.getObject()));
		if (true)
			return _xx[(int) (0)];
		return 0;
	}

	public static String _get_transit_interps_for_this_day(double _jdx)
			throws Exception {
		int _d = 0, _i = 0, _j = 0, _m = 0, _q = 0, _y = 0;
		long _ret_flag = 0L;
		String _any_text = "", _filename = "", _phrase_to_look_for = "", _t = "", _txt = "";
		double _orb = 0, _tod = 0;
		String _da = "";

		mostCurrent._edittext1.setText((Object) (""));
		_transit_data_header = _vv7(_jdx);
		_transit_data = "";
		{
			final double step596 = 1, limit596 = 9;
			for (_i = 0; (step596 > 0 && _i <= limit596)
					|| (step596 < 0 && _i >= limit596); _i += step596) {
				_vvv1[_i] = _get_1_planet(_jdx, _i);
				_transit_data = _transit_data
						+ _p_names[_i]
						+ " = "
						+ _convert_longitude((float) (_vvv1[_i]))
						+ "   -   "
						+ Common.NumberFormat2(_vvv1[_i], (int) (1), (int) (4),
								(int) (4), Common.False) + Common.CRLF;
			}
		}
		;

		if (mostCurrent._chkdebug.getChecked() == Common.True) {
			mostCurrent._edittext1
					.setText((Object) (_transit_data_header + Common.CRLF
							+ _transit_data + Common.CRLF + _natal_data_header
							+ Common.CRLF + _natal_data + Common.CRLF));
		} else {
			mostCurrent._edittext1.setText((Object) (_transit_data_header
					+ Common.CRLF + _natal_data_header + Common.CRLF));
		}
		;

		_orb = 2.0;
		_any_text = "";

		{
			final double step607 = 1, limit607 = 5;
			for (_i = 0; (step607 > 0 && _i <= limit607)
					|| (step607 < 0 && _i >= limit607); _i += step607) {
				if (_i == 1) {
					if (true)
						continue;
				}
				;
				{
					final double step609 = 1;
					final double limit609 = (int) (9);
					for (_j = 0; (step609 > 0 && _j <= limit609)
							|| (step609 < 0 && _j >= limit609); _j += step609) {
						_aspect_present[_i][_j] = (int) (0);
						_da = BA.NumberToString(Common
								.Abs(_vv4[_j] - _vvv1[_i]));

						if ((double) (Double.parseDouble(_da)) > 180) {
							_da = BA.NumberToString(360 - (double) (Double
									.parseDouble(_da)));
						}
						;
						_q = 1;
						if ((double) (Double.parseDouble(_da)) <= _orb) {
							_q = 2;
						} else if ((double) (Double.parseDouble(_da)) <= 60 + _orb
								&& (double) (Double.parseDouble(_da)) >= 60 - _orb) {
							_q = 3;
						} else if ((double) (Double.parseDouble(_da)) <= 90 + _orb
								&& (double) (Double.parseDouble(_da)) >= 90 - _orb) {
							_q = 4;
						} else if ((double) (Double.parseDouble(_da)) <= 120 + _orb
								&& (double) (Double.parseDouble(_da)) >= 120 - _orb) {
							_q = 5;
						} else if ((double) (Double.parseDouble(_da)) >= 180 - _orb) {
							_q = 6;
						}
						;

						if (_q > 1) {
							_aspect_present[_i][_j] = _q;
							_phrase_to_look_for = _p_names[_i] + _asp_names[_q]
									+ _p_names[_j];
							_filename = _p_names[_i] + "_tr.txt";
							_txt = _find_specific_report_paragraph(
									_phrase_to_look_for, _filename);
							_any_text = "xxx";
							mostCurrent._edittext1
									.setText((Object) (mostCurrent._edittext1
											.getText() + _txt + Common.CRLF));
						}
						;
					}
				}
				;
			}
		}
		;

		if ((_any_text).equals("")) {
			mostCurrent._edittext1
					.setText((Object) (mostCurrent._edittext1.getText()
							+ Common.CRLF
							+ "You have no transits active at this time." + Common.CRLF));
		}
		;
		_last_interp_text = mostCurrent._edittext1.getText();
		return "";
	}

	public static String _vv0(double _jdx) throws Exception {
		long _lw = 0L, _itw = 0L, _kw = 0L, _ikw = 0L, _nw = 0L, _jtw = 0L;
		String _calendar = "";

		_lw = (long) (Common.Floor(_jdx + 0.5) + 68569);
		_nw = (long) (Common.Floor(4 * _lw / (double) 146097));
		_lw = (long) (_lw - Common.Floor((146097 * _nw + 3) / (double) 4));
		_itw = (long) (Common.Floor(4000 * (_lw + 1) / (double) 1461001));
		_lw = (long) (_lw - Common.Floor(1461 * _itw / (double) 4) + 31);
		_jtw = (long) (Common.Floor(80 * _lw / (double) 2447));
		_kw = (long) (_lw - Common.Floor(2447 * _jtw / (double) 80));
		_lw = (long) (Common.Floor(_jtw / (double) 11));
		_jtw = (long) (_jtw + 2 - 12 * _lw);
		_ikw = (long) (100 * (_nw - 49) + _itw + _lw);

		if (true)
			return Common.NumberFormat(_kw, 2, 0) + " "
					+ _month_names[(int) (_jtw)] + " "
					+ BA.NumberToString(_ikw);
		return "";
	}

	public static String _vv2(float _current_tz) throws Exception {
		List _list2 = null;
		float _tz = 0f;
		int _idx = 0;
		String _res = "";
		_list2 = new List();
		_tz = 0f;
		_idx = 0;
		if (_current_tz == -12) {
			_idx = (int) (0);
		}
		;
		if (_current_tz == -11) {
			_idx = (int) (1);
		}
		;
		if (_current_tz == -10.5) {
			_idx = (int) (2);
		}
		;
		if (_current_tz == -10) {
			_idx = (int) (3);
		}
		;
		if (_current_tz == -9.5) {
			_idx = (int) (4);
		}
		;
		if (_current_tz == -9) {
			_idx = (int) (5);
		}
		;
		if (_current_tz == -8) {
			_idx = (int) (6);
		}
		;
		if (_current_tz == -7) {
			_idx = (int) (7);
		}
		;
		if (_current_tz == -6) {
			_idx = (int) (8);
		}
		;
		if (_current_tz == -5) {
			_idx = (int) (9);
		}
		;
		if (_current_tz == -4) {
			_idx = (int) (10);
		}
		;
		if (_current_tz == -3.5) {
			_idx = (int) (11);
		}
		;
		if (_current_tz == -3) {
			_idx = (int) (12);
		}
		;
		if (_current_tz == -2) {
			_idx = (int) (13);
		}
		;
		if (_current_tz == -1) {
			_idx = (int) (14);
		}
		;
		if (_current_tz == 0) {
			_idx = (int) (15);
		}
		;
		if (_current_tz == 1) {
			_idx = (int) (16);
		}
		;
		if (_current_tz == 2) {
			_idx = (int) (17);
		}
		;
		if (_current_tz == 3) {
			_idx = (int) (18);
		}
		;
		if (_current_tz == 3.5) {
			_idx = (int) (19);
		}
		;
		if (_current_tz == 4) {
			_idx = (int) (20);
		}
		;
		if (_current_tz == 5) {
			_idx = (int) (21);
		}
		;
		if (_current_tz == 5.5) {
			_idx = (int) (22);
		}
		;
		if (_current_tz == 6) {
			_idx = (int) (23);
		}
		;
		if (_current_tz == 6.5) {
			_idx = (int) (24);
		}
		;
		if (_current_tz == 7) {
			_idx = (int) (25);
		}
		;
		if (_current_tz == 7.5) {
			_idx = (int) (26);
		}
		;
		if (_current_tz == 8) {
			_idx = (int) (27);
		}
		;
		if (_current_tz == 8.5) {
			_idx = (int) (28);
		}
		;
		if (_current_tz == 9) {
			_idx = (int) (29);
		}
		;
		if (_current_tz == 9.5) {
			_idx = (int) (30);
		}
		;
		if (_current_tz == 10) {
			_idx = (int) (31);
		}
		;
		if (_current_tz == 10.5) {
			_idx = (int) (32);
		}
		;
		if (_current_tz == 11) {
			_idx = (int) (33);
		}
		;
		if (_current_tz == 11.5) {
			_idx = (int) (34);
		}
		;
		if (_current_tz == 12) {
			_idx = (int) (35);
		}
		;
		if (_current_tz == 12.5) {
			_idx = (int) (36);
		}
		;
		if (_current_tz == 13) {
			_idx = (int) (37);
		}
		;
		_list2.Initialize();
		_list2.Add((Object) ("GMT -12:00 hrs - IDLW"));
		_list2.Add((Object) ("GMT -11:00 hrs - BET or NT"));
		_list2.Add((Object) ("GMT -10:30 hrs - HST"));
		_list2.Add((Object) ("GMT -10:00 hrs - AHST"));
		_list2.Add((Object) ("GMT -09:30 hrs - HDT or HWT"));
		_list2.Add((Object) ("GMT -09:00 hrs - YST or AHDT or AHWT"));
		_list2.Add((Object) ("GMT -08:00 hrs - PST or YDT or YWT"));
		_list2.Add((Object) ("GMT -07:00 hrs - MST or PDT or PWT"));
		_list2.Add((Object) ("GMT -06:00 hrs - CST or MDT or MWT"));
		_list2.Add((Object) ("GMT -05:00 hrs - EST or CDT or CWT"));
		_list2.Add((Object) ("GMT -04:00 hrs - AST or EDT or EWT"));
		_list2.Add((Object) ("GMT -03:30 hrs - NST"));
		_list2.Add((Object) ("GMT -03:00 hrs - BZT2 or AWT"));
		_list2.Add((Object) ("GMT -02:00 hrs - AT"));
		_list2.Add((Object) ("GMT -01:00 hrs - WAT"));
		_list2.Add((Object) ("Greenwich Mean Time - GMT or UT"));
		_list2.Add((Object) ("GMT +01:00 hrs - CET or MET or BST"));
		_list2.Add((Object) ("GMT +02:00 hrs - EET or CED or MED"));
		_list2.Add((Object) ("GMT +03:00 hrs - BAT or EED"));
		_list2.Add((Object) ("GMT +03:30 hrs - IT"));
		_list2.Add((Object) ("GMT +04:00 hrs - USZ3"));
		_list2.Add((Object) ("GMT +05:00 hrs - USZ4"));
		_list2.Add((Object) ("GMT +05:30 hrs - IST"));
		_list2.Add((Object) ("GMT +06:00 hrs - USZ5"));
		_list2.Add((Object) ("GMT +06:30 hrs - NST"));
		_list2.Add((Object) ("GMT +07:00 hrs - SST or USZ6"));
		_list2.Add((Object) ("GMT +07:30 hrs - JT"));
		_list2.Add((Object) ("GMT +08:00 hrs - AWST or CCT"));
		_list2.Add((Object) ("GMT +08:30 hrs - MT"));
		_list2.Add((Object) ("GMT +09:00 hrs - JST or AWDT"));
		_list2.Add((Object) ("GMT +09:30 hrs - ACST or SAT or SAST"));
		_list2.Add((Object) ("GMT +10:00 hrs - AEST or GST"));
		_list2.Add((Object) ("GMT +10:30 hrs - ACDT or SDT or SAD"));
		_list2.Add((Object) ("GMT +11:00 hrs - UZ10 or AEDT"));
		_list2.Add((Object) ("GMT +11:30 hrs - NZ"));
		_list2.Add((Object) ("GMT +12:00 hrs - NZT or IDLE"));
		_list2.Add((Object) ("GMT +12:30 hrs - NZS"));
		_list2.Add((Object) ("GMT +13:00 hrs - NZST"));
		_res = BA.NumberToString(Common.InputList(_list2, "Choose time zone",
				_idx, mostCurrent.activityBA));
		_tz = (float) (0);
		if ((_res).equals(BA.NumberToString(Common.DialogResponse.CANCEL)) == false) {
			if ((_res).equals(BA.NumberToString(0))) {
				_tz = (float) (-12);
			}
			;
			if ((_res).equals(BA.NumberToString(1))) {
				_tz = (float) (-11);
			}
			;
			if ((_res).equals(BA.NumberToString(2))) {
				_tz = (float) (-10.5);
			}
			;
			if ((_res).equals(BA.NumberToString(3))) {
				_tz = (float) (-10);
			}
			;
			if ((_res).equals(BA.NumberToString(4))) {
				_tz = (float) (-9.5);
			}
			;
			if ((_res).equals(BA.NumberToString(5))) {
				_tz = (float) (-9);
			}
			;
			if ((_res).equals(BA.NumberToString(6))) {
				_tz = (float) (-8);
			}
			;
			if ((_res).equals(BA.NumberToString(7))) {
				_tz = (float) (-7);
			}
			;
			if ((_res).equals(BA.NumberToString(8))) {
				_tz = (float) (-6);
			}
			;
			if ((_res).equals(BA.NumberToString(9))) {
				_tz = (float) (-5);
			}
			;
			if ((_res).equals(BA.NumberToString(10))) {
				_tz = (float) (-4);
			}
			;
			if ((_res).equals(BA.NumberToString(11))) {
				_tz = (float) (-3.5);
			}
			;
			if ((_res).equals(BA.NumberToString(12))) {
				_tz = (float) (-3);
			}
			;
			if ((_res).equals(BA.NumberToString(13))) {
				_tz = (float) (-2);
			}
			;
			if ((_res).equals(BA.NumberToString(14))) {
				_tz = (float) (-1);
			}
			;
			if ((_res).equals(BA.NumberToString(15))) {
				_tz = (float) (0);
			}
			;
			if ((_res).equals(BA.NumberToString(16))) {
				_tz = (float) (1);
			}
			;
			if ((_res).equals(BA.NumberToString(17))) {
				_tz = (float) (2);
			}
			;
			if ((_res).equals(BA.NumberToString(18))) {
				_tz = (float) (3);
			}
			;
			if ((_res).equals(BA.NumberToString(19))) {
				_tz = (float) (3.5);
			}
			;
			if ((_res).equals(BA.NumberToString(20))) {
				_tz = (float) (4);
			}
			;
			if ((_res).equals(BA.NumberToString(21))) {
				_tz = (float) (5);
			}
			;
			if ((_res).equals(BA.NumberToString(22))) {
				_tz = (float) (5.5);
			}
			;
			if ((_res).equals(BA.NumberToString(23))) {
				_tz = (float) (6);
			}
			;
			if ((_res).equals(BA.NumberToString(24))) {
				_tz = (float) (6.5);
			}
			;
			if ((_res).equals(BA.NumberToString(25))) {
				_tz = (float) (7);
			}
			;
			if ((_res).equals(BA.NumberToString(26))) {
				_tz = (float) (7.5);
			}
			;
			if ((_res).equals(BA.NumberToString(27))) {
				_tz = (float) (8);
			}
			;
			if ((_res).equals(BA.NumberToString(28))) {
				_tz = (float) (8.5);
			}
			;
			if ((_res).equals(BA.NumberToString(29))) {
				_tz = (float) (9);
			}
			;
			if ((_res).equals(BA.NumberToString(30))) {
				_tz = (float) (9.5);
			}
			;
			if ((_res).equals(BA.NumberToString(31))) {
				_tz = (float) (10);
			}
			;
			if ((_res).equals(BA.NumberToString(32))) {
				_tz = (float) (10.5);
			}
			;
			if ((_res).equals(BA.NumberToString(33))) {
				_tz = (float) (11);
			}
			;
			if ((_res).equals(BA.NumberToString(34))) {
				_tz = (float) (11.5);
			}
			;
			if ((_res).equals(BA.NumberToString(35))) {
				_tz = (float) (12);
			}
			;
			if ((_res).equals(BA.NumberToString(36))) {
				_tz = (float) (12.5);
			}
			;
			if ((_res).equals(BA.NumberToString(37))) {
				_tz = (float) (13);
			}
			;
		}
		;
		if (true)
			return BA.NumberToString(_tz);
		return "";
	}

	public static String _vv5() throws Exception {
		String _d = "";
		String _s = "";
		long _l = 0L;
		int _res = 0;
		_d = "";
		_s = "";
		_l = 0L;
		_res = 0;
		_s = Common.DateTime.getDateFormat();
		Common.DateTime.setDateFormat("MM/dd/yyyy HH:mm:ss");
		_l = Common.DateTime.getNow();
		_d = Common.DateTime.Date(_l) + " GMT";
		Common.DateTime.setDateFormat("MM/dd/yyyy HH:mm:ss z");
		_res = (int) (-Common.Round((_l - Common.DateTime.DateParse(_d))
				/ (double) 3600000));
		Common.DateTime.setDateFormat(_s);
		if (true)
			return BA.NumberToString(_res);
		return "";
	}

	public static void initializeProcessGlobals() {

		if (processGlobalsRun == false) {
			processGlobalsRun = true;
			try {
				MainActivity._process_globals();

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static boolean isAnyActivityVisible() {
		boolean vis = false;
		vis = vis | (MainActivity.mostCurrent != null);
		return vis;
	}

	public static String _globals() throws Exception {
		mostCurrent._btnchange = new ButtonWrapper();
		mostCurrent._btndate = new ButtonWrapper();
		mostCurrent._btnhelp = new ButtonWrapper();
		mostCurrent._btnprevious = new Button(mostCurrent);
		mostCurrent._btnnext = new Button(mostCurrent);
		mostCurrent._btnwheel_text = new Button(mostCurrent);
		mostCurrent._vvv3 = new ButtonWrapper();
		mostCurrent._v5 = new BitmapWrapper();
		mostCurrent._v6 = new BitmapWrapper();
		mostCurrent._edittext1 = new EditTextWrapper();
		mostCurrent._chkdebug = new CompoundButtonWrapper.CheckBoxWrapper();
		mostCurrent._vv1 = new CanvasWrapper();
		mostCurrent._v7 = new TypefaceWrapper();
		return "";
	}

	public static String _process_globals() throws Exception {

		_vv3 = new SweDate();
		_vvv2 = new SwissEph();
		_asp_names = new String[(int) (7)];
		Arrays.fill(_asp_names, "");
		_last_interp_text = "";
		_month_names = new String[(int) (13)];
		Arrays.fill(_month_names, "");
		_natal_data = "";
		_p_names = new String[(int) (10)];
		Arrays.fill(_p_names, "");
		_prev_text = "";
		_transit_data = "";
		_natal_data_header = "";
		_transit_data_header = "";
		_aspect_present = new int[(int) (10)][];
		{
			int d0 = _aspect_present.length;
			int d1 = (int) (10);
			for (int i0 = 0; i0 < d0; i0++) {
				_aspect_present[i0] = new int[d1];
			}
		}
		;
		_flag_help = 0;
		_pl_glyph = new int[(int) (10)];
		;
		_sign_glyph = new int[(int) (13)];
		;
		_spot_filled = new int[(int) (360)];
		;
		_tz_offset = 0f;
		_current_jd = 0;
		_v0 = 0;
		_vv4 = new double[(int) (10)];
		;
		_vvv1 = new double[(int) (10)];
		;
		return "";
	}

	public static int _vvv4(String _s) throws Exception {

		if (true)
			return (int) (Double.parseDouble(_s));
		return 0;
	}

	/**
	 * Here goes my Code
	 * **/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case INSERT_ID: {
			gotoSignUpPage();
			break;
		}
		case LOGIN_ID: {
			gotoLoginPage();
			break;
		}
		case LOGOUT_ID: {
			// Log.d("Login name",
			// "Logged in as, "+ParseController.getCurrentUser());
			// ParseController.saveInInstallation(ParseController.getCurrentUser());

			// ParseController.getCloudServiceData();

			ParseController.logoutUser();
			Editor saveEditor = getSharedPreferences("TransitPref",
					Context.MODE_PRIVATE).edit();
			saveEditor.putString("uname", "");
			saveEditor.putString("pwd", "");
			saveEditor.commit();
			break;
		}
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void gotoSignUpPage() {
		Intent i = new Intent(this, AddUserActivity.class);
		startActivityForResult(i, ACTIVITY_CREATE);
	}

	private void gotoLoginPage() {
		Intent i = new Intent(this, LogInActivity.class);
		startActivityForResult(i, LOGIN_CODE);
	}

	/**
	 * Here goes the modified files
	 * **/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, INSERT_ID, 0, R.string.menu_insert);
		menu.add(0, LOGIN_ID, 1, "Login");
		menu.add(0, LOGOUT_ID, 2, "Logout");
		return result;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		// processBA.onActivityResult(requestCode, resultCode, intent);
		super.onActivityResult(requestCode, resultCode, intent);
		if (intent == null) {
			return;
		}
		if (requestCode == ACTIVITY_CREATE) {
			final Bundle extras = intent.getExtras();

			String uname = extras.getString("uname");
			String pwd = extras.getString("pwd");
			String email = extras.getString("email");
			String phone = extras.getString("phone");
			String DOB = extras.getString("DOB");
			DOB += "_" + extras.getString("TOB");

			// Toast.makeText(MainActivity.this, "Uname: "+uname,
			// Toast.LENGTH_SHORT).show();
			ParseController.createUser(uname, pwd, DOB, email, phone);
		}
		if (requestCode == LOGIN_CODE) {
			final Bundle extras = intent.getExtras();
			String uname = extras.getString("uname");
			String pwd = extras.getString("pwd");

			ParseController.loginUser(uname, pwd);

			Editor saveEditor = getSharedPreferences("TransitPref",
					Context.MODE_PRIVATE).edit();
			saveEditor.putString("uname", uname);
			saveEditor.putString("pwd", pwd);
			saveEditor.commit();
		}
	}

	/**
	 * Returns the instance of this Activity
	 * **/
	public static MainActivity getMainContext() {
		return mostCurrent;
	}
}
