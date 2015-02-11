package com.fairphone.fplauncher3.widgets.peoplewidget.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CallInterceptorReceiver extends BroadcastReceiver
{
    private static final String TAG = CallInterceptorReceiver.class.getSimpleName();
    private final CallListener mListener;

    public CallInterceptorReceiver(CallListener listener)
    {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(OutgoingCallInterceptor.ACTION_CALL_MADE))
        {
            final String originalNumber = intent.getStringExtra(OutgoingCallInterceptor.EXTRA_CALLED_PHONE_NUMBER);
            String msg = "Intercepted outgoing call. Number " + originalNumber;
            Log.d(TAG, msg);
            System.out.println(TAG + " " + msg );
            mListener.onOutgoingCall(originalNumber);
        }
    }
}