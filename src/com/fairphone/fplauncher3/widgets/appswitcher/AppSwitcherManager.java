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

import com.fairphone.fplauncher3.Launcher;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class AppSwitcherManager {
	private static final String TAG = AppSwitcherManager.class.getSimpleName();
	private static final String PREFS_APP_SWITCHER_APPS_DATA = "com.fairphone.fplauncher3.PREFS_APP_SWITCHER_APPS_DATA";

	private static final ApplicationRunInfoManager _instance = new ApplicationRunInfoManager(
			true);

	public static ApplicationRunInfoManager getInstance() {
		return _instance;
	}

	private Context mContext;
	private Launcher mLauncher;
	private BroadcastReceiver mBCastLaunchAllApps;
	private BroadcastReceiver mBCastAppLauncher;

	public AppSwitcherManager(Context context, Launcher launcher) {
		mContext = context;
		mLauncher = launcher;
	}

	public void unregisterAppSwitcherBroadcastReceivers() {
		if (mBCastLaunchAllApps != null) {
			mContext.unregisterReceiver(mBCastLaunchAllApps);
		}
		if (mBCastAppLauncher != null) {
			mContext.unregisterReceiver(mBCastAppLauncher);
		}
	}

	public void registerAppSwitcherBroadcastReceivers() {
		// launching the application
		mBCastLaunchAllApps = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i(TAG, "Received a call from widget....Show all apps");
				mLauncher.showAllAppsDrawer();
			}
		};

		mBCastAppLauncher = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				String packageName = intent
						.getStringExtra(AppSwitcherWidget.EXTRA_LAUNCH_APP_PACKAGE);
				String className = intent
						.getStringExtra(AppSwitcherWidget.EXTRA_LAUNCH_APP_CLASS_NAME);

				Log.i(TAG, "Received a call from widget....Launch App "
						+ packageName + " - " + className);

				Intent launchIntent = mContext.getPackageManager()
						.getLaunchIntentForPackage(packageName);

				if (launchIntent != null) {
					launchIntent.setComponent(new ComponentName(packageName,
							className));
					mLauncher.launchActivity(null, launchIntent,
							"AppSwitcherLaunch");
				}
			}
		};

		mContext.registerReceiver(mBCastLaunchAllApps, new IntentFilter(
				AppSwitcherWidget.ACTION_APP_SWITCHER_LAUNCH_ALL_APPS));
		mContext.registerReceiver(mBCastAppLauncher, new IntentFilter(
				AppSwitcherWidget.ACTION_APP_SWITCHER_LAUNCH_APP));
	}

	public void saveAppSwitcherData() {
		ApplicationRunInformation.persistAppRunInfo(mContext,
				PREFS_APP_SWITCHER_APPS_DATA, AppSwitcherManager.getInstance()
						.getAllAppRunInfo());
	}

	public void loadAppSwitcherData() {
		// Most Used
		Log.d(TAG, "loadAppRunInfos: loading ");
		AppSwitcherManager.getInstance().resetState();

		// set the all apps
		AppSwitcherManager.getInstance().setAllRunInfo(
				ApplicationRunInformation.loadAppRunInfo(mContext,
						PREFS_APP_SWITCHER_APPS_DATA));
	}
	
	public void updateAppSwitcherData(ArrayList<String> packageNames)
    {
        List<ApplicationRunInformation> allApps = AppSwitcherManager.getInstance().getAllAppRunInfo();

        List<ApplicationRunInformation> appsToRemove = new ArrayList<ApplicationRunInformation>();

        for (ApplicationRunInformation appRunInfo : allApps)
        {
            if (packageNames.contains(appRunInfo.getComponentName().getPackageName()))
            {
                appsToRemove.add(appRunInfo);
            }
        }

        for (ApplicationRunInformation appToRemove : appsToRemove)
        {
            applicationRemoved(appToRemove.getComponentName());
        }
    }

	public void updateAppSwitcherWidgets() {
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(mContext);
		int[] appWidgetIds = appWidgetManager
				.getAppWidgetIds(new ComponentName(mContext,
						AppSwitcherWidget.class));
		if (appWidgetIds.length > 0) {
			new AppSwitcherWidget().onUpdate(mContext, appWidgetManager,
					appWidgetIds);
		}
	}

	public void applicationStarted(ComponentName componentName,
			boolean isFreshInstall) {
		ApplicationRunInformation appRunInfo = ApplicationRunInfoManager
				.generateApplicationRunInfo(componentName, false);
		AppSwitcherManager.getInstance().applicationStarted(appRunInfo);
		updateAppSwitcherWidgets();
		saveAppSwitcherData();
	}

	public void applicationRemoved(ComponentName componentName) {
		AppSwitcherManager.getInstance().applicationRemoved(componentName);
		updateAppSwitcherWidgets();
		saveAppSwitcherData();
	}
}
