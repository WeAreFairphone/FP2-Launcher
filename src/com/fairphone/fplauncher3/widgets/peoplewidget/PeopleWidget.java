package com.fairphone.fplauncher3.widgets.peoplewidget;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.fairphone.fplauncher3.R;
import com.fairphone.fplauncher3.widgets.peoplewidget.data.ContactInfo;
import com.fairphone.fplauncher3.widgets.peoplewidget.data.ContactInfoManager;
import com.fairphone.fplauncher3.widgets.peoplewidget.data.PeopleManager;
import com.fairphone.fplauncher3.widgets.peoplewidget.utils.CircleTransform;

public class PeopleWidget extends AppWidgetProvider
{

    private static final String TAG = PeopleWidget.class.getSimpleName();
    public static final boolean HIDE_SECOND_ROW = false;
    public static final boolean SHOW_COUNTERS = false;
    
    public static final String ACTION_PEOPLE_WIDGET_LAUNCH_CONTACTS_APP = "com.fairphone.fplauncher3.ACTION_PEOPLE_WIDGET_LAUNCH_CONTACTS_APP";

    private RemoteViews mWidget;
    private Context mContext;

    @Override
    public void onEnabled(Context context)
    {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        mContext = context;

        for (int i = 0; i < appWidgetIds.length; i++)
        {
            Log.d(TAG, "Updating widget #" + i);
            updateBoard(appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions)
    {
        mContext = context;
        updateBoard(appWidgetManager, appWidgetId);

        // Obtain appropriate widget and update it.
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    private void updateImage(RemoteViews view, final int viewId, final String photoUrl)
    {
        Bitmap bitmap = loadContactPhoto(photoUrl, mContext);
        if (bitmap != null)
        {
            Bitmap circle = new CircleTransform().transform(bitmap, mContext.getResources().getDimension(R.dimen.contact_picture_size));
            view.setImageViewBitmap(viewId, circle);
        }
        else
        {
            view.setImageViewResource(viewId, R.drawable.icon_contacts_default);
        }
    }

    private Bitmap loadContactPhoto(final String photoData, Context context)
    {
        Uri thumbUri;
        AssetFileDescriptor afd = null;
        if (!TextUtils.isEmpty(photoData))
        {
            try
            {
                thumbUri = Uri.parse(photoData);
                /*
                 * Retrieves an AssetFileDescriptor object for the thumbnail URI
                 * using ContentResolver.openAssetFileDescriptor
                 */
                afd = context.getContentResolver().openAssetFileDescriptor(thumbUri, "r");
                /*
                 * Gets a file descriptor from the asset file descriptor. This
                 * object can be used across processes.
                 */
                FileDescriptor fileDescriptor = afd.getFileDescriptor();
                // Decode the photo file and return the result as a Bitmap
                // If the file descriptor is valid
                if (fileDescriptor != null)
                {
                    // Decodes the bitmap
                    Log.i(TAG, "Uri = " + thumbUri);
                    return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, null);
                }
                // If the file isn't found
            } catch (FileNotFoundException e)
            {
                Log.e(TAG, e.getMessage());
            } finally
            {
                if (afd != null)
                {
                    try
                    {
                        afd.close();
                    } catch (IOException e)
                    {
                    }
                }
            }
        }
        return null;
    }

    private void updateBoard(AppWidgetManager appWidgetManager, int appWidgetId)
    {

        mWidget = new RemoteViews(mContext.getPackageName(), R.layout.people_widget_main);
        
        ContactInfoManager instance = PeopleManager.getInstance();

        // clear the current data
        mWidget.removeAllViews(R.id.last_contacted_row_1);
        mWidget.removeAllViews(R.id.last_contacted_row_2);

        mWidget.removeAllViews(R.id.most_contacted_row_1);
        mWidget.removeAllViews(R.id.most_contacted_row_2);

        if (!HIDE_SECOND_ROW)
        {
            mWidget.setViewVisibility(R.id.most_contacted_row_2, View.VISIBLE);
            mWidget.setViewVisibility(R.id.last_contacted_row_2, View.VISIBLE);
        }

        List<ContactInfo> mostContacted = new ArrayList<ContactInfo>(instance.getMostContacted());
        List<ContactInfo> lastContacted = new ArrayList<ContactInfo>(instance.getLastContacted());
       
        toggleMostAndLastContactedViewsVisibility(mWidget, mostContacted, lastContacted);
        
        updateMostContactedList(mContext, mWidget, mostContacted);
        updateLastContactedList(mContext, mWidget, lastContacted);

        appWidgetManager.updateAppWidget(appWidgetId, null);
        appWidgetManager.updateAppWidget(appWidgetId, mWidget);

    }
    
    private void toggleMostAndLastContactedViewsVisibility(RemoteViews widget, List<ContactInfo> mostContacted, List<ContactInfo> lastContacted)
    {
        if (mostContacted.isEmpty() && lastContacted.isEmpty())
            widget.setViewVisibility(R.id.mostUsedContactsOOBEDescription, View.VISIBLE);
        else
            widget.setViewVisibility(R.id.mostUsedContactsOOBEDescription, View.GONE);
    }

    private void updateLastContactedList(Context context, RemoteViews widget, List<ContactInfo> contactInfoList)
    {
        int viewCounter = 0;
        int limit = PeopleManager.getInstance().getLastContactedLimit();

        // Add one to the limit to make room for the all contacts button
        limit = (limit % 2 == 0) ? limit : (limit + 1);

        for (ContactInfo contactInfo : contactInfoList)
        {
            RemoteViews view = getRecentView(context, contactInfo);

            if (view != null)
            {
                if (viewCounter < (!HIDE_SECOND_ROW ? (limit / 2) : limit))
                {
                    widget.addView(R.id.last_contacted_row_1, view);
                }
                else if (!HIDE_SECOND_ROW)
                {
                    widget.addView(R.id.last_contacted_row_2, view);
                }
                viewCounter++;
            }
        }

        RemoteViews allContactsView = getAllContactsView(context);
        if (contactInfoList.size() < (limit / 2))
        {
            widget.addView(R.id.last_contacted_row_1, allContactsView);
        }
        else
        {
            widget.addView(R.id.last_contacted_row_2, allContactsView);
        }
    }

    private void updateMostContactedList(Context context, RemoteViews widget, List<ContactInfo> contactInfoList)
    {
        int viewCounter = 0;
        for (ContactInfo contactInfo : contactInfoList)
        {
            RemoteViews view = getMostContactView(context, contactInfo);

            if (view != null)
            {
                int limit = PeopleManager.getInstance().getMostContactedLimit();
                if (viewCounter < (!HIDE_SECOND_ROW ? (limit / 2) : limit))
                {
                    widget.addView(R.id.most_contacted_row_1, view);
                }
                else if (!HIDE_SECOND_ROW)
                {
                    widget.addView(R.id.most_contacted_row_2, view);
                }
                viewCounter++;
            }
        }
    }

    private RemoteViews getRecentView(Context context, ContactInfo info)
    {
        RemoteViews recentRow = new RemoteViews(context.getPackageName(), R.layout.people_widget_last_contacted_item);
        setupView(recentRow, info);

        return recentRow;
    }

    private RemoteViews getAllContactsView(Context context)
    {
        RemoteViews recentRow = new RemoteViews(context.getPackageName(), R.layout.people_widget_last_contacted_item);
        setupAllContactsView(recentRow);

        return recentRow;
    }

    public void setupAllContactsView(RemoteViews view)
    {
        view.setImageViewResource(R.id.contact_photo, R.drawable.icon_contacts);
        view.setTextViewText(R.id.contact_name, mContext.getResources().getString(R.string.contacts_title));
        view.setImageViewResource(R.id.contact_indicator, android.R.color.transparent);

        // set up the all apps intent
        Intent launchIntent = new Intent();
        launchIntent.setAction(PeopleWidget.ACTION_PEOPLE_WIDGET_LAUNCH_CONTACTS_APP);

        PendingIntent launchPendingIntent = PendingIntent.getBroadcast(mContext, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.contact_photo, launchPendingIntent);
    }

    private RemoteViews getMostContactView(Context context, ContactInfo info)
    {
        RemoteViews mostContactRow = new RemoteViews(context.getPackageName(), R.layout.people_widget_most_contacted_item);
        setupView(mostContactRow, info);

        return mostContactRow;
    }

    public void setupView(RemoteViews view, ContactInfo info)
    {
        updateImage(view, R.id.contact_photo, info.photoUri);
        String contactName = TextUtils.isEmpty(info.name) ? info.getNumberTypeAsString(mContext) : info.name;
        String contactNumber = TextUtils.isEmpty(info.name) ? "" : info.getNumberTypeAsString(mContext);
        if (SHOW_COUNTERS)
        {
            contactName = info.getCount() + " - " + contactName;
        }
        view.setTextViewText(R.id.contact_name, contactName);
        if(!TextUtils.isEmpty(info.name))
        {
            view.setViewVisibility(R.id.contact_phone_number, View.VISIBLE);
            view.setTextViewText(R.id.contact_phone_number, contactNumber);
        }
        else
        {
            view.setViewVisibility(R.id.contact_phone_number, View.GONE);
        }

        // open contact
        addOpenContactBehaviour(view, info);

        switch (info.getLastAction())
        {
            case CALL:
                // call contact
                addCallContactBehaviour(view, info, false);
                view.setImageViewResource(R.id.contact_indicator, R.drawable.icon_call_indicator);
                break;

            case SMS:
                // sms contact
                addSmsContactBehaviour(view, info, false);
                view.setImageViewResource(R.id.contact_indicator, R.drawable.icon_sms_indicator);
                break;

            default:
                break;
        }
    }

    public void addSmsContactBehaviour(RemoteViews view, final ContactInfo contactInfo, boolean clearClickListener)
    {
        if (!clearClickListener)
        {
            String uriSms = "smsto:" + contactInfo.getPhoneNumberOnE164Format();
            Intent intentSms = new Intent(Intent.ACTION_SENDTO);
            intentSms.setData(Uri.parse(uriSms));

            PendingIntent pendingIntentSms = PendingIntent.getActivity(mContext, 0, intentSms, PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.last_action, pendingIntentSms);
        }
        else
        {
            view.setOnClickPendingIntent(R.id.last_action, null);
        }
    }

    public void addCallContactBehaviour(RemoteViews view, final ContactInfo contactInfo, boolean clearClickListener)
    {
        if (!clearClickListener)
        {
            String uriCall = "tel:" + contactInfo.getPhoneNumberOnE164Format();
            Intent intentCall = new Intent(Intent.ACTION_CALL);
            intentCall.setData(Uri.parse(uriCall));

            PendingIntent pendingIntentCall = PendingIntent.getActivity(mContext, 0, intentCall, PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.last_action, pendingIntentCall);
        }
        else
        {
            view.setOnClickPendingIntent(R.id.last_action, null);
        }
    }

    public void addOpenContactBehaviour(RemoteViews view, final ContactInfo contactInfo)
    {
        if (!TextUtils.isEmpty(contactInfo.contactId))
        {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, "" + contactInfo.contactId));
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.contact_photo, pendingIntent);
        }
        else
        {
            view.setOnClickPendingIntent(R.id.contact_photo, null);
        }
    }
}
