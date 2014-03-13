package com.b4a.transits;

import org.json.JSONArray;
import org.json.JSONException;
import com.parse.ParseException;
import com.parse.ParseObject;
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

	/**
	 * Return the contacts from the Phone Contacts
	 * **/
	public JSONArray getContacts() throws ParseException, JSONException {

		ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);

		JSONArray contactsListArray = new JSONArray();

		if (cur.getCount() > 0) {

			int count = 0;
			while (cur.moveToNext()) {

				String id = cur.getString(cur
						.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				Log.v("NAMES IN CONTACT", "" + name);

				ParseObject contact = new ParseObject("Contacts");

				contact.put("firstName", name);
				contact.put("userId", id);

				// For phone Numbers
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
					JSONArray phoneArray = new JSONArray();

					while (pCur.moveToNext()) {

						int i = 0;
						number[i] = pCur.getString(pCur
								.getColumnIndex(CommonDataKinds.Phone.NUMBER));
						Log.e("number", "" + number[i]);
						phoneArray.put(number[i]);
					}
					pCur.close();

					contact.put("phoneNumber", phoneArray);
				}

				// For Notes
				String noteWhere = ContactsContract.Data.CONTACT_ID
						+ " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
				String[] noteWhereParams = new String[] { id,
						ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE };
				Cursor noteCur = cr.query(ContactsContract.Data.CONTENT_URI,
						null, noteWhere, noteWhereParams, null);
				if (noteCur.moveToFirst()) {
					String note = noteCur
							.getString(noteCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
					contact.put("note", note);
				}
				noteCur.close();

				// For email
				Cursor emailCur = cr.query(
						ContactsContract.CommonDataKinds.Email.CONTENT_URI,
						null, ContactsContract.CommonDataKinds.Email.CONTACT_ID
								+ " = ?", new String[] { id }, null);

				JSONArray emailArray = new JSONArray();

				while (emailCur.moveToNext()) {

					String email = emailCur
							.getString(emailCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
					String emailType = emailCur
							.getString(emailCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
					emailArray.put(email);
				}
				emailCur.close();
				contact.put("email", emailArray);

				// For IM
				String imWhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
						+ ContactsContract.Data.MIMETYPE + " = ?";
				String[] imWhereParams = new String[] { id,
						ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE };

				JSONArray imArray = new JSONArray();

				Cursor imCur = cr.query(ContactsContract.Data.CONTENT_URI,
						null, imWhere, imWhereParams, null);

				if (imCur.moveToFirst()) {

					String imName = imCur
							.getString(imCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));
					String imType = imCur
							.getString(imCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Im.TYPE));
					imArray.put(imName);
				}
				imCur.close();
				contact.put("IM", imArray);

				// For organization
				String orgWhere = ContactsContract.Data.CONTACT_ID
						+ " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
				String[] orgWhereParams = new String[] {
						id,
						ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE };
				Cursor orgCur = cr.query(ContactsContract.Data.CONTENT_URI,
						null, orgWhere, orgWhereParams, null);
				JSONArray orgArray = new JSONArray();

				if (orgCur.moveToFirst()) {

					String orgName = orgCur
							.getString(orgCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
					String title = orgCur
							.getString(orgCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
					orgArray.put(orgName);
				}
				orgCur.close();
				contact.put("organization", orgArray);

				contactsListArray.put(contact);

				// if (++count > 200)
				// break;
			}

		}
		return contactsListArray;
	}
}
