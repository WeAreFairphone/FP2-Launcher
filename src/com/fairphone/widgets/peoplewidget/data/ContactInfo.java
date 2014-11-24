package com.fairphone.widgets.peoplewidget.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class ContactInfo {
	private static final String TAG = ContactInfo.class.getSimpleName();

	private static final String SEPARATOR = ",";

	public enum LAST_ACTION {
		CALL, SMS
	}

	public String name;
	public String photoUri;
	public String lookup;
	public String contactId;
	public PhoneNumber phoneNumber;
	private long mCounter;
	private Date mLastExecution;
	private LAST_ACTION mLastAction;
	private int mNumberType;

	public ContactInfo(String name, String photoUri, String lookup,
			String contactID, PhoneNumber phoneNumber, int numberType) {
		this.name = name;
		this.photoUri = photoUri;
		this.lookup = lookup;
		this.contactId = contactID;
		this.phoneNumber = phoneNumber;
		this.mCounter = 0l;
		this.mLastExecution = null;
		this.mLastAction = null;
		this.mNumberType = numberType;
	}

	public ContactInfo(PhoneNumber phoneNumber) {
		this.name = "";
		this.photoUri = "";
		this.lookup = "";
		this.contactId = "";
		this.phoneNumber = phoneNumber;
		this.mCounter = 0l;
		this.mLastExecution = null;
		this.mLastAction = null;
		this.mNumberType = Phone.TYPE_HOME;
	}

	public ContactInfo(Context context, PhoneNumber phoneNumber, long counter,
			Date lastExecution, String lastAction) {
		ContactInfo contact = getContactFromPhoneNumber(context, phoneNumber,
				getLastActionFromString(lastAction));

		this.name = contact.name;
		this.photoUri = contact.photoUri;
		this.lookup = contact.lookup;
		this.contactId = contact.contactId;
		this.phoneNumber = contact.phoneNumber;
		this.mNumberType = contact.mNumberType;
		this.mCounter = counter;
		this.mLastExecution = lastExecution;
		this.mLastAction = contact.getLastAction();
	}

	public String getNumberTypeAsString(Context context) {
		String numberTypeName = "";
		if (!TextUtils.isEmpty(this.contactId)) {
			numberTypeName = Phone.getTypeLabel(context.getResources(),
					mNumberType, "").toString();
		} else {
			if (this.phoneNumber != null) {
				numberTypeName = ContactInfo.getPhoneNumberByFormat(
						this.phoneNumber, PhoneNumberFormat.NATIONAL);
			}
		}

		return numberTypeName;
	}

	public String getPhoneNumberOnNationalFormat() {
		return ContactInfo.getPhoneNumberByFormat(this.phoneNumber,
				PhoneNumberFormat.NATIONAL);
	}

	public String getPhoneNumberOnE164Format() {
		return ContactInfo.getPhoneNumberByFormat(this.phoneNumber,
				PhoneNumberFormat.E164);
	}

	private LAST_ACTION getLastActionFromString(String lastAction) {
		LAST_ACTION action = null;
		try {
			action = LAST_ACTION.valueOf(lastAction);
		} catch (Exception e) {
			Log.w(TAG, "Invalid action. Setting Call");
			action = LAST_ACTION.CALL;
		}
		return action;
	}

	public static String serializeContact(ContactInfo contact) {
		StringBuilder sb = new StringBuilder();
		sb.append(contact.getCount());
		sb.append(SEPARATOR);
		sb.append(contact.getLastExecution().getTime());
		sb.append(SEPARATOR);
		sb.append(contact.getLastAction().name());
		return sb.toString();
	}

	public LAST_ACTION getLastAction() {
		return mLastAction;
	}

	public void setLastAction(LAST_ACTION action) {
		mLastAction = action;
	}

	public static ContactInfo deserializeContact(Context context,
			String number, String data) {
		ContactInfo contactInfo = null;
		if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(data)) {
			PhoneNumber parsedPhoneNumber = ContactInfo.getPhoneNumber(context,
					number);
			if (parsedPhoneNumber != null) {
				Log.d(TAG, "Contact data > " + data);
				String[] splits = data.split(SEPARATOR);
				Date lastExecution = null;
				long count = 0l;
				String lastAction = null;
				if (splits != null && splits.length == 3) {
					try {
						count = Long.valueOf(splits[0]);
						lastExecution = new Date();
						lastExecution.setTime(Long.valueOf(splits[1]));
					} catch (NumberFormatException e) {
						e.printStackTrace();
						lastExecution = Calendar.getInstance().getTime();
					}

					lastAction = splits[2];
				}
				contactInfo = new ContactInfo(context, parsedPhoneNumber,
						count, lastExecution, lastAction);
			}
		}
		return contactInfo;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Contact [name=");
		builder.append(name);
		builder.append(", photoUri=");
		builder.append(photoUri);
		builder.append(", lookup=");
		builder.append(lookup);
		builder.append(", contactID=");
		builder.append(contactId);
		builder.append(", phoneNumber=");
		builder.append(getPhoneNumberOnE164Format());
		builder.append("]");
		return builder.toString();
	}

	public void resetCount() {
		mCounter = 0l;
	}

	public void incrementCount() {
		mCounter++;
	}

	public long getCount() {
		return mCounter;
	}

	public void decrementCount() {
		if (mCounter > 0) {
			mCounter--;
		}
	}

	public Date getLastExecution() {
		return mLastExecution;
	}

	public void setLastExecution(Date lastExecution) {
		this.mLastExecution = lastExecution;
	}

	public static ContactInfo getContactFromPhoneNumber(Context context,
			PhoneNumber number, LAST_ACTION action) {
		ContactInfo contact = null;
		if (number != null) {
			String nationalNumber = ContactInfo
					.getNormalizedPhoneNumberByFormat(number,
							PhoneNumberFormat.NATIONAL);

			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
					Uri.encode(nationalNumber));

			String[] projection = new String[] { PhoneLookup.DISPLAY_NAME,
					PhoneLookup.PHOTO_URI, PhoneLookup.TYPE,
					PhoneLookup.LOOKUP_KEY, PhoneLookup._ID,
					PhoneLookup.NUMBER, };
			ContentResolver contentResolver = context.getContentResolver();
			String selection = PhoneLookup.NUMBER + " LIKE %" + number + "%";
			Cursor cursor = contentResolver.query(uri, projection, selection,
					null, null);

			if (cursor != null && cursor.moveToNext()) {
				String contactId = cursor.getString(cursor
						.getColumnIndex(PhoneLookup._ID));
				String lookup = cursor.getString(cursor
						.getColumnIndex(PhoneLookup.LOOKUP_KEY));
				String photoUri = cursor.getString(cursor
						.getColumnIndex(PhoneLookup.PHOTO_URI));
				String name = cursor.getString(cursor
						.getColumnIndex(PhoneLookup.DISPLAY_NAME));
				int type = cursor.getInt(cursor
						.getColumnIndex(PhoneLookup.TYPE));

				// Log.d(TAG, DatabaseUtils.dumpCursorToString(cursor));
				String phonenumber = cursor.getString(cursor
						.getColumnIndex(PhoneLookup.NUMBER));

				contact = new ContactInfo(name, photoUri, lookup, contactId,
						number, type);
			}

			if (contact != null) {
				Log.d(TAG, "Number " + number + " belongs to contact "
						+ contact);
			} else {
				contact = new ContactInfo(number);
				Log.d(TAG, "Number " + number + " as no contact associated.");
			}
			contact.setLastExecution(Calendar.getInstance().getTime());
			contact.setLastAction(action);

			if (cursor != null) {
				cursor.close();
			}
		}
		return contact;
	}

	public static String normalizePhoneNumber(String number) {
		String normalizedNumber = "";
		if (!TextUtils.isEmpty(number)) {
			String cleanNumber = number.replaceAll("\\(+", "");
			String allNumbers = PhoneNumberUtil
					.convertAlphaCharactersInNumber(cleanNumber);
			String normalizeDigits = PhoneNumberUtil
					.normalizeDigitsOnly(allNumbers);

			if (cleanNumber.startsWith("+")) {
				normalizedNumber = "+" + normalizeDigits;
			} else {
				normalizedNumber = normalizeDigits;
			}
		}

		return normalizedNumber;
	}

	public static boolean isValidPhoneNumber(String number) {
		return !TextUtils.isEmpty(number)
				&& (number.startsWith("+") || Character.isDigit(number
						.charAt(0)));
	}

	public static boolean isInternationalPhoneNumber(String number) {
		return !TextUtils.isEmpty(number) && number.startsWith("+");
	}

	public static String getPhoneNumberByFormat(PhoneNumber number,
			PhoneNumberFormat format) {
		String formattedNumber = "";
		if (number != null) {
			PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
			formattedNumber = phoneUtil.format(number, format);
		}
		return formattedNumber;
	}

	public static String getNormalizedPhoneNumberByFormat(PhoneNumber number,
			PhoneNumberFormat format) {
		return normalizePhoneNumber(getPhoneNumberByFormat(number, format));
	}

	public static PhoneNumber getPhoneNumber(Context context, String number) {
		PhoneNumber parsedNumber = null;
		String normalizedNumber = normalizePhoneNumber(number);
		if (!TextUtils.isEmpty(normalizedNumber)
				&& isValidPhoneNumber(normalizedNumber)) {
			PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
			try {
				if (isInternationalPhoneNumber(normalizedNumber)) {
					parsedNumber = phoneUtil.parse(normalizedNumber, "");
				} else {
					String countryCode = getCountryCode(context);
					parsedNumber = phoneUtil.parse(normalizedNumber,
							countryCode);
				}
			} catch (NumberParseException e) {
				Log.e(TAG, "NumberParseException was thrown: " + e.toString());
				parsedNumber = null;
			}
		}
		return parsedNumber;
	}

	public static String getCountryCode(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String countryCode = tm.getNetworkCountryIso();
		Log.d(TAG, "Country code " + countryCode);

		if (TextUtils.isEmpty(countryCode)) {
			countryCode = Locale.getDefault().getLanguage();
		}
		return countryCode.toUpperCase();
	}

	public static List<ContactInfo> loadContactInfo(Context context,
			String preferencesKey) {
		SharedPreferences prefs = context.getSharedPreferences(
				preferencesKey, 0);

		List<ContactInfo> allContacts = new ArrayList<ContactInfo>();

		Map<String, ?> phoneNumbers = prefs.getAll();
		for (String number : phoneNumbers.keySet()) {
			String data = prefs.getString(number, "");

			if (data.length() == 0) {
				continue;
			}

			ContactInfo contact = ContactInfo.deserializeContact(context,
					number, data);
			if (contact != null) {
				allContacts.add(contact);
			}
		}
		return allContacts;
	}
}
