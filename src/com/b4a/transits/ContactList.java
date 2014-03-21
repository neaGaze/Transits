package com.b4a.transits;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.parse.ParseException;
import com.parse.ParseObject;

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

				while (imCur.moveToFirst()) {

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

				// For birthday
				String dOBWhen = ContactsContract.CommonDataKinds.Event.CONTACT_ID
						+ "= ? AND "
						+ ContactsContract.Data.MIMETYPE
						+ "= ? AND "
						+ ContactsContract.CommonDataKinds.Event.TYPE + "=?";
				String[] dOBWhenParam = new String[] {
						id,
						ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
						ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY
								+ "" };

				Cursor dOBCursor = cr.query(ContactsContract.Data.CONTENT_URI,
						null, dOBWhen, dOBWhenParam, null);
				if (dOBCursor.moveToFirst()) {
					String dob = dOBCursor.getString(0);
					contact.put("birthDate", dob);
				}
				dOBCursor.close();

				// For address
				String countryWhere = ContactsContract.Data.CONTACT_ID
						+ " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
				String[] countryWhereParam = new String[] {
						id,
						ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE };

				Cursor countryWhereCursor = cr.query(
						ContactsContract.Data.CONTENT_URI, null, countryWhere,
						countryWhereParam, null);

				JSONArray addr = new JSONArray();
				JSONObject addrObj = new JSONObject();

				if (countryWhereCursor.moveToFirst()) {
					String poBox = countryWhereCursor
							.getString(countryWhereCursor
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
					String street = countryWhereCursor
							.getString(countryWhereCursor
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
					String city = countryWhereCursor
							.getString(countryWhereCursor
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
					String state = countryWhereCursor
							.getString(countryWhereCursor
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
					String postalCode = countryWhereCursor
							.getString(countryWhereCursor
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
					String countryName = countryWhereCursor
							.getString(countryWhereCursor
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
					String type = countryWhereCursor
							.getString(countryWhereCursor
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));

					addrObj.put("POBox", poBox);
					addrObj.put("Street", street);
					addrObj.put("City", city);
					addrObj.put("State", state);
					addrObj.put("PostalCode", postalCode);
					addrObj.put("Country", countryName);
					addrObj.put("Type", type);
					addr.put(addrObj);
					Log.v("Country", "" + countryName);
				}
				countryWhereCursor.close();
				contact.put("address", addr);

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
					Log.d("Organization", title + "@" + orgName);
				}
				orgCur.close();
				contact.put("organization", orgArray);

				contactsListArray.put(contact);

				if (++count > 5)
					break;
			}

		}
		return contactsListArray;
	}
}
