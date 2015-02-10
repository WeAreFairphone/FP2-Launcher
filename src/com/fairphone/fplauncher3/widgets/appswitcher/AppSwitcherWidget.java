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
package com.fairphone.fplauncher3.widgets.appswitcher;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.fairphone.fplauncher3.R;

public class AppSwitcherWidget extends AppWidgetProvider
{
    private static final String TAG = AppSwitcherWidget.class.getSimpleName();
    private static final boolean APP_SWITCHER_DEBUG_MODE = false;

    // AppSwitcher settings
    public static final String ACTION_APP_SWITCHER_LAUNCH_APP = "com.fairphone.fplauncher3.ACTION_APP_SWITCHER_LAUNCH_APP";
    public static final String ACTION_APP_SWITCHER_LAUNCH_ALL_APPS = "com.fairphone.fplauncher3.ACTION_APP_SWITCHER_LAUNCH_ALL_APPS";
    public static final String EXTRA_LAUNCH_APP_NAME = "com.fairphone.fplauncher3.EXTRA_LAUNCH_APP_NAME";
    public static final String EXTRA_LAUNCH_APP_PACKAGE = "com.fairphone.fplauncher3.EXTRA_LAUNCH_APP_PACKAGE";
    public static final String EXTRA_LAUNCH_APP_CLASS_NAME = "com.fairphone.fplauncher3.EXTRA_LAUNCH_APP_CLASS_NAME";

    @Override
    public void onEnabled(Context context)
    {
        Log.d(TAG, "Fairphone - WidgetProvicer Context is " + context);
    }

    @Override
    public void onDisabled(Context context)
    {
        // Called once the last instance of your widget is removed from the
        // homescreen
        super.onDisabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        // Widget instance is removed from the homescreen
        Log.d(TAG, "onDeleted - " + appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions)
    {
        updateUI(context, appWidgetManager, appWidgetId);
        // Obtain appropriate widget and update it.
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    private void updateUI(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        int code = 0;
        // get the widgets
        RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.fp_app_switcher);

        // clear the current data
        widget.removeAllViews(R.id.lastUsedApps);
        widget.removeAllViews(R.id.mostUsedApps);

        // obtain the current data saved
        ApplicationRunInfoManager instance = AppSwitcherManager.getInstance();

        List<ApplicationRunInformation> mostRecent = new ArrayList<ApplicationRunInformation>(instance.getRecentApps());
        List<ApplicationRunInformation> mostUsed = new ArrayList<ApplicationRunInformation>(instance.getMostUsedApps());

        Log.d(TAG, "mostRecent lenght: " + mostRecent.size());
        Log.d(TAG, "mostUsed lenght: " + mostUsed.size());

        toggleMostAndLastUsedViewsVisibility(widget, mostRecent, mostUsed);

        // update the recent apps
        code = updateLastUsedAppsList(context, code, widget, mostRecent);

        // Process the most used apps
        code = updateMostUsedAppsList(context, code, widget, mostUsed);

        // update the widget data
        appWidgetManager.updateAppWidget(appWidgetId, null);
        appWidgetManager.updateAppWidget(appWidgetId, widget);
    }

    private int updateMostUsedAppsList(Context context, int code, RemoteViews widget, List<ApplicationRunInformation> mostUsed)
    {
        for (ApplicationRunInformation mostUsedInfo : mostUsed)
        {
            RemoteViews view = null;
            try
            {
                view = getMostUsedView(context, mostUsedInfo, code);

                if (view != null)
                {
                    widget.addView(R.id.mostUsedApps, view);
                }
            } catch (NameNotFoundException e)
            {
                // if no information is available log it and continue
                Log.e(TAG, "Could not find the correct package", e);

                continue;
            }

            // update the code
            code++;
        }

        RemoteViews allAppsView = getAllAppsButton(context, code);

        if (allAppsView != null)
        {
            widget.addView(R.id.mostUsedApps, allAppsView);
        }

        return code;
    }

    private int updateLastUsedAppsList(Context context, int code, RemoteViews widget, List<ApplicationRunInformation> mostRecent)
    {
        for (ApplicationRunInformation appRunInfo : mostRecent)
        {
            RemoteViews view = null;
            try
            {
                view = getRecentView(context, appRunInfo, code);

                if (view != null)
                {
                    widget.addView(R.id.lastUsedApps, view);
                }
            } catch (NameNotFoundException e)
            {
                // if no information is available log it and continue
                Log.e(TAG, "Could not find the correct package", e);

                continue;
            }

            // update the code
            code++;
        }
        return code;
    }

    private void toggleMostAndLastUsedViewsVisibility(RemoteViews widget, List<ApplicationRunInformation> mostRecent, List<ApplicationRunInformation> mostUsed)
    {
        if (mostRecent.isEmpty() && mostUsed.isEmpty())
            widget.setViewVisibility(R.id.mostUsedAppsOOBEDescription, View.VISIBLE);
        else
            widget.setViewVisibility(R.id.mostUsedAppsOOBEDescription, View.GONE);
    }

    private RemoteViews getMostUsedView(Context context, ApplicationRunInformation info, int code) throws NameNotFoundException
    {
        // generate the mostUsed row
        RemoteViews mostUsedRow = new RemoteViews(context.getPackageName(), R.layout.fp_most_used_item);
        PackageManager pm = context.getPackageManager();

        // get app icon and label
        Drawable icon = pm.getActivityIcon(info.getComponentName());
        Bitmap iconBitmap = ((BitmapDrawable) icon).getBitmap();
        CharSequence appLabel = pm.getActivityInfo(info.getComponentName(), 0).loadLabel(pm);

        // debug String with app count
        String fullAppLabel = info.getCount() + "# " + appLabel;

        mostUsedRow.setImageViewBitmap(android.R.id.content, iconBitmap);

        mostUsedRow.setTextViewText(R.id.mostUsedButton, APP_SWITCHER_DEBUG_MODE ? fullAppLabel : appLabel);

        Intent launchIntent = generateLaunchIntent(info, appLabel.toString());

        PendingIntent clickRecentApps = PendingIntent.getBroadcast(context, code, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mostUsedRow.setOnClickPendingIntent(R.id.mostUsedButton, clickRecentApps);

        return mostUsedRow;
    }

    private RemoteViews getAllAppsButton(Context context, int code)
    {
        // generate the mostUsed row
        RemoteViews allAppsButton = new RemoteViews(context.getPackageName(), R.layout.fp_most_used_item);

        Drawable draw = context.getResources().getDrawable(R.drawable.icon_allapps_white_widget);
        Bitmap icon = ((BitmapDrawable) draw).getBitmap();
        allAppsButton.setImageViewBitmap(android.R.id.content, icon);

        allAppsButton.setTextViewText(R.id.mostUsedButton, context.getResources().getString(R.string.edge_swipe_all_apps).toUpperCase());

        Intent launchIntent = new Intent();
        launchIntent.setAction(ACTION_APP_SWITCHER_LAUNCH_ALL_APPS);

        PendingIntent launchPendingIntent = PendingIntent.getBroadcast(context, code++, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        allAppsButton.setOnClickPendingIntent(R.id.mostUsedButton, launchPendingIntent);

        return allAppsButton;
    }

    private Intent generateLaunchIntent(ApplicationRunInformation info, String appLabel) throws NameNotFoundException
    {
        Intent i = new Intent();
        i.setAction(ACTION_APP_SWITCHER_LAUNCH_APP);

        Bundle extras = new Bundle();
        extras.putString(EXTRA_LAUNCH_APP_NAME, appLabel);
        extras.putString(EXTRA_LAUNCH_APP_PACKAGE, info.getComponentName().getPackageName());
        extras.putString(EXTRA_LAUNCH_APP_CLASS_NAME, info.getComponentName().getClassName());

        i.putExtras(extras);

        return i;
    }

    private RemoteViews getRecentView(Context context, ApplicationRunInformation info, int code) throws NameNotFoundException
    {
        RemoteViews recentRow = new RemoteViews(context.getPackageName(), R.layout.fp_last_used_item);
        PackageManager pm = context.getPackageManager();

        // get application icon and label
        Drawable icon = pm.getActivityIcon(info.getComponentName());
        Bitmap iconBitmap = ((BitmapDrawable) icon).getBitmap();
        CharSequence appLabel = pm.getActivityInfo(info.getComponentName(), 0).loadLabel(pm);

        // debug String with app count
        String fullAppLabel = info.getCount() + "# " + appLabel;

        recentRow.setTextViewText(R.id.recentButton, APP_SWITCHER_DEBUG_MODE ? fullAppLabel : appLabel);
        recentRow.setImageViewBitmap(android.R.id.background, iconBitmap);

        // create the intent for this app
        Intent launchIntent = generateLaunchIntent(info, appLabel.toString());

        PendingIntent clickRecentApps = PendingIntent.getBroadcast(context, code, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        recentRow.setOnClickPendingIntent(R.id.recentButton, clickRecentApps);

        return recentRow;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.appwidget.AppWidgetProvider#onUpdate(android.content.Context,
     * android.appwidget.AppWidgetManager, int[])
     * 
     * OnUpdate ==============================================================
     * context The Context in which this receiver is running. appWidgetManager A
     * AppWidgetManager object you can call updateAppWidget(ComponentName,
     * RemoteViews) on. appWidgetIds The appWidgetIds for which an update is
     * needed. Note that this may be all of the AppWidget instances for this
     * provider, or just a subset of them.
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // Called in response to the ACTION_APPWIDGET_UPDATE broadcast when this
        // AppWidget provider
        // is being asked to provide RemoteViews for a set of AppWidgets.
        // Override this method to implement your own AppWidget functionality.

        // iterate through every instance of this widget
        // remember that it can have more than one widget of the same type.
        for (int i = 0; i < appWidgetIds.length; i++)
        {
            Log.d(TAG, "Updating AppSwitcher widget #" + i);
            updateUI(context, appWidgetManager, appWidgetIds[i]);
        }

    }

}