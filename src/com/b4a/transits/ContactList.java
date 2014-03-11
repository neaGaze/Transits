package com.b4a.transits;

import android.R.integer;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.util.Log;

public class ContactList {

	private Context context;

	public ContactList(Context context) {
		this.context = context;
	}

	public void getContacts() {

		ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String id = cur.getString(cur
						.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				Log.v("NAMES IN CONTACT", "" + name);
				if (Integer
						.parseInt(cur.getString(cur
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = cr.query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = ?", new String[] { id }, null);

					if (pCur == null)
						continue;
					String[] number = new String[pCur.getCount()];
					while (pCur.moveToNext()) {
						int i = 0;
						number[i] = pCur.getString(pCur
								.getColumnIndex(CommonDataKinds.Phone.NUMBER));
						Log.e("number", "" + number[i]);
					}
					pCur.close();
				}
			}
		}
	}
}
