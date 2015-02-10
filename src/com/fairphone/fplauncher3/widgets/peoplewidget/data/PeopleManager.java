/*
 * Copyright (C) 2013 Fairphone Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fairphone.fplauncher3.widgets.peoplewidget.data;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.util.Log;

import com.fairphone.fplauncher3.Launcher;
import com.fairphone.fplauncher3.widgets.peoplewidget.PeopleWidget;
import com.fairphone.fplauncher3.widgets.peoplewidget.data.ContactInfo.LAST_ACTION;
import com.fairphone.fplauncher3.widgets.peoplewidget.receivers.CallInterceptorReceiver;
import com.fairphone.fplauncher3.widgets.peoplewidget.receivers.CallListener;
import com.fairphone.fplauncher3.widgets.peoplewidget.receivers.OutgoingCallInterceptor;
import com.fairphone.fplauncher3.widgets.peoplewidget.receivers.SmsObserver;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;


public class PeopleManager implements CallListener
{
	private static final String TAG = PeopleManager.class.getSimpleName();
	
    public static final String PREFS_PEOPLE_WIDGET_CONTACTS_DATA = "com.fairphone.fplauncher3.PREFS_PEOPLE_WIDGET_CONTACTS_DATA";
    
	private static final ContactInfoManager _instance = new ContactInfoManager();
    
	private final Context mContext;
	private final Launcher mLauncher;
	private CallInterceptorReceiver mCallBroadcastReceiver;
    private ContentObserver smsObserver;
    private BroadcastReceiver mBCastAllContactsLauncher;
    
	public static ContactInfoManager getInstance(){
		return _instance;
	}
	
	public PeopleManager(Context context, Launcher launcher) {
		mContext = context;
		mLauncher = launcher;
	}
	
    @Override
    public void onOutgoingCall(String number)
    {
        String msg = "Intercepted outgoing call. Number " + number;
        Log.d(TAG, msg);
        processNumberCalled(number, LAST_ACTION.CALL);
        updatePeopleWidgets();
    }

    @Override
    public void onOutgoingSMS(String number)
    {
        String msg = "Intercepted outgoing SMS. Number " + number;
        Log.d(TAG, msg);
        processNumberCalled(number, LAST_ACTION.SMS);
        updatePeopleWidgets();
    }

    private void processNumberCalled(String number, LAST_ACTION action)
    {
        PhoneNumber phoneNumber = ContactInfo.getPhoneNumber(mContext, number);
        if (phoneNumber != null)
        {
            ContactInfo contact = ContactInfo.getContactFromPhoneNumber(mContext, phoneNumber, action);
            PeopleManager.getInstance().contactUsed(contact);
            savePeopleWidgetData();
        }
    }

    private void registerCommsListeners()
    {
        registerCallListener();
        registerSmsListener();
    }

    private void unregisterCommsListeners()
    {
        mContext.unregisterReceiver(mCallBroadcastReceiver);
        mContext.getContentResolver().unregisterContentObserver(smsObserver);
    }

    private void registerSmsListener()
    {
    	if(smsObserver == null){
	        smsObserver = new SmsObserver(mContext, this);
	        mContext.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, smsObserver);
	        Log.d(TAG, "register sms listener");
	    }
    }

    private void registerCallListener()
    {
    	if(mCallBroadcastReceiver == null){
	        mCallBroadcastReceiver = new CallInterceptorReceiver(this);
	
	        IntentFilter iFilter = new IntentFilter(OutgoingCallInterceptor.ACTION_CALL_MADE);
	        mContext.registerReceiver(mCallBroadcastReceiver, iFilter);
	        Log.d(TAG, "register call listener");
    	}
    }

    public void loadContactsInfo()
    {
        // Most Used
        Log.d(TAG, "loadContactsInfo: loading ");
        PeopleManager.getInstance().resetState();
        
        // set the all contacts
        PeopleManager.getInstance().setAllContactInfo(ContactInfo.loadContactInfo(mContext, PREFS_PEOPLE_WIDGET_CONTACTS_DATA));
    }

    private void persistContactInfo()
    {
        SharedPreferences prefs = mContext.getSharedPreferences(PREFS_PEOPLE_WIDGET_CONTACTS_DATA, 0);

        // get the current prefs and clear to update
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.clear();

        for (ContactInfo contactInfo : PeopleManager.getInstance().getAllContactInfos())
        {
            editor.putString(contactInfo.getPhoneNumberOnE164Format(), ContactInfo.serializeContact(contactInfo));
        }

        editor.commit();
    }

    public void savePeopleWidgetData()
    {
        persistContactInfo();
    }

    public void updatePeopleWidgets()
    {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(mContext, PeopleWidget.class));
        if (appWidgetIds.length > 0)
        {
            new PeopleWidget().onUpdate(mContext, appWidgetManager, appWidgetIds);
        }
    }

    public void registerBroadcastReceivers()
    {
        // launching Contacts application
        mBCastAllContactsLauncher = new BroadcastReceiver()
        {

            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.i(TAG, "Launch contacts app");
                Intent contactsAppIntent = new Intent(Intent.ACTION_VIEW);
                contactsAppIntent.setData(Uri.parse("content://contacts/people/"));
                mLauncher.startActivity(contactsAppIntent);
            }
        };

        mContext.registerReceiver(mBCastAllContactsLauncher, new IntentFilter(PeopleWidget.ACTION_PEOPLE_WIDGET_LAUNCH_CONTACTS_APP));
    	
        registerCommsListeners();
    }

    public void unregisterBroadcastReceivers()
    {
        mContext.unregisterReceiver(mBCastAllContactsLauncher);
        //unregisterCommsListeners();
    }
}
