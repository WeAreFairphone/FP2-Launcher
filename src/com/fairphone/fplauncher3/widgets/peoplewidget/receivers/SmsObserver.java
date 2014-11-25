package com.fairphone.fplauncher3.widgets.peoplewidget.receivers;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.fairphone.fplauncher3.widgets.peoplewidget.data.PeopleManager;

public class SmsObserver extends ContentObserver
{

    private static final String TAG = SmsObserver.class.getSimpleName();
    public static final String CONTENT_SMS = "content://sms/sent";
    public static final String PEOPLE_WIDGET_LAST_SENT_SMS_TIMESTAMP = "com.fairphone.fplauncher3.PEOPLE_WIDGET_LAST_SENT_SMS_TIMESTAMP";

    public static final int MESSAGE_TYPE_SENT = 2;

    private Context mContext;

    CallListener mListener;

    public SmsObserver(Context context, CallListener listener)
    {
        super(null);
        mContext = context;
        mListener = listener;
    }

    @Override
    public void onChange(boolean selfChange)
    {
        super.onChange(selfChange);

        String[] reqCols = new String[] {
                "_id", "protocol", "type", "address", "body", "date"
        };

        String selection = "date > " + getLastSentSmsTimestamp(mContext);

        Cursor cursor = mContext.getContentResolver().query(Uri.parse(CONTENT_SMS), reqCols, selection, null, "date desc");
        if (cursor != null && cursor.moveToNext())
        {
            // Log.d(TAG, DatabaseUtils.dumpCursorToString(cursor));
            String protocol = cursor.getString(cursor.getColumnIndex("protocol"));
            int type = cursor.getInt(cursor.getColumnIndex("type"));

            Log.d(TAG, "Protocol: " + protocol + " - Type: " + type);
            // Only processing outgoing sms event & only when it
            // is sent successfully (available in SENT box).
            if (type == MESSAGE_TYPE_SENT)
            {
                Log.d(TAG, "Sms Sent");
                int bodyColumn = cursor.getColumnIndex("body");
                int addressColumn = cursor.getColumnIndex("address");
                int dateColumn = cursor.getColumnIndex("date");
                String to = cursor.getString(addressColumn);
                String message = cursor.getString(bodyColumn);
                long date = cursor.getLong(dateColumn);
                Log.d(TAG, "To: " + to + " - Message: " + message + " - Date: " + date);
                if (!TextUtils.isEmpty(to))
                {
                    setLastSentSmsTimestamp(mContext, date);
                    mListener.onOutgoingSMS(to);
                }
            }

        }
        else if (cursor != null)
        {
            cursor.close();
        }
    }

    private long getLastSentSmsTimestamp(Context context)
    {
        Log.d(TAG, "getLastSentSmsTimestamp");
        SharedPreferences prefs = context.getSharedPreferences(PeopleManager.PREFS_PEOPLE_WIDGET_CONTACTS_DATA, 0);

        // for the first time use the current time minus 5 seconds or else the
        // message will never be retrieved
        long defaultTimestamp = new Date().getTime() - 5000;
        return prefs.getLong(SmsObserver.PEOPLE_WIDGET_LAST_SENT_SMS_TIMESTAMP, defaultTimestamp);
    }

    private void setLastSentSmsTimestamp(Context context, long timestamp)
    {
        SharedPreferences prefs = context.getSharedPreferences(PeopleManager.PREFS_PEOPLE_WIDGET_CONTACTS_DATA, 0);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(SmsObserver.PEOPLE_WIDGET_LAST_SENT_SMS_TIMESTAMP, timestamp);
        editor.commit();
    }
}
