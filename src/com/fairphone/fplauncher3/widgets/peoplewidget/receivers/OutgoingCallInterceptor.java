package com.fairphone.fplauncher3.widgets.peoplewidget.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OutgoingCallInterceptor extends BroadcastReceiver
{
    private static final String TAG = OutgoingCallInterceptor.class.getSimpleName();
    
    public static final String ACTION_CALL_MADE = "com.fairphone.fplauncher3.OutgoingCallInterceptor.ACTION_CALL_MADE";
    public static final String EXTRA_CALLED_PHONE_NUMBER = "com.fairphone.fplauncher3.OutgoingCallInterceptor.EXTRA_CALLED_PHONE_NUMBER";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String originalNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        String msg = "Intercepted outgoing call. Number " + originalNumber;
        Log.d(TAG, msg);
        System.out.println(TAG + " " + msg );

        Intent intentCall = new Intent(ACTION_CALL_MADE);
        intentCall.putExtra(EXTRA_CALLED_PHONE_NUMBER, originalNumber);
        context.sendBroadcast(intentCall);
    }
}