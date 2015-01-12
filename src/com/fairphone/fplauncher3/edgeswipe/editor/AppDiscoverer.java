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

package com.fairphone.fplauncher3.edgeswipe.editor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fairphone.fplauncher3.AppInfo;
import com.fairphone.fplauncher3.AppInfo.APP_AGE;
import com.fairphone.fplauncher3.LauncherModel;
import com.fairphone.fplauncher3.widgets.appswitcher.AppSwitcherManager;
import com.fairphone.fplauncher3.widgets.appswitcher.ApplicationRunInfoManager;
import com.fairphone.fplauncher3.widgets.appswitcher.ApplicationRunInformation;

import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.util.Pair;

public class AppDiscoverer
{
    private static AppDiscoverer _instance = new AppDiscoverer();

    public static final String PREFS_APPS_AGING_DATA = "com.fairphone.fplauncher3.applifecycle.FAIRPHONE_APP_AGING_DATA";

    private Map<ComponentName, AppInfo> _allApps;

    private ApplicationRunInfoManager agingManager;

    public static AppDiscoverer getInstance()
    {
        return _instance;
    }

    private AppDiscoverer()
    {
        _allApps = new HashMap<ComponentName, AppInfo>();
        agingManager = new ApplicationRunInfoManager(false);
    }

    public void loadAllApps(ArrayList<AppInfo> allApps)
    {
        for (AppInfo appInfo : allApps)
        {
            ApplicationRunInformation appRunInfo = agingManager.getApplicationRunInformation(appInfo.getComponentName());
            if (appRunInfo == null)
            {
                agingManager.applicationStarted(ApplicationRunInfoManager.generateApplicationRunInfo(appInfo.getComponentName(), false));

                appInfo.setAge(APP_AGE.FREQUENT_USE);
                appInfo.setIsPinned(false);
            }
            else
            {
                updateAgeInfo(appInfo);
                appInfo.setIsPinned(appRunInfo.isPinnedApp());
                appInfo.setIsNew(appRunInfo.isNewApp());
                appInfo.setIsUpdated(appRunInfo.isUpdatedApp());
            }

            _allApps.put(appInfo.getComponentName(), appInfo);
        }
    }

    private void updateAgeInfo(AppInfo appInfo)
    {
        Date now = Calendar.getInstance().getTime();
        ApplicationRunInformation appRunInfo = agingManager.getApplicationRunInformation(appInfo.getComponentName());
        long timePastSinceLastExec = now.getTime() - appRunInfo.getLastExecution().getTime();

        boolean isPinned = appRunInfo.isPinnedApp();

        if (timePastSinceLastExec < AppInfo.getAgeLevelInMiliseconds(APP_AGE.FREQUENT_USE) || isPinned)
        {
            appInfo.setAge(APP_AGE.FREQUENT_USE);
        }
        else
        {
            appInfo.setAge(APP_AGE.RARE_USE);
        }
    }

    public ArrayList<AppInfo> getPackages()
    {
        ArrayList<AppInfo> appList = new ArrayList<AppInfo>();

        for (AppInfo appInfo : _allApps.values())
        {
            appList.add(appInfo);
        }
        Collections.sort(appList, LauncherModel.getAppNameComparator());

        return appList;
    }

    public Pair<ArrayList<AppInfo>, ArrayList<AppInfo>> getUsedAndUnusedApps()
    {
        Pair<ArrayList<AppInfo>, ArrayList<AppInfo>> appLists =
                new Pair<ArrayList<AppInfo>, ArrayList<AppInfo>>(new ArrayList<AppInfo>(), new ArrayList<AppInfo>());

        for (AppInfo appInfo : _allApps.values())
        {
            updateAgeInfo(appInfo);
            if (appInfo.getAge() == APP_AGE.FREQUENT_USE)
            {
                appLists.first.add(appInfo);
            }
            else
            {
                appLists.second.add(appInfo);
            }
        }
        Collections.sort(appLists.first, LauncherModel.getAppNameComparator());
        Collections.sort(appLists.second, LauncherModel.getAppNameComparator());

        return appLists;
    }

    public void applicationInstalled(Context context, AppInfo installedApp)
    {
    	if(installedApp != null){
    		ComponentName componentName = installedApp.getComponentName();
    		
	        AppInfo newApp = new AppInfo(installedApp);
	        newApp.setAge(APP_AGE.FREQUENT_USE);
	        newApp.setIsNew(true);
	        newApp.setIsUpdated(false);
	        newApp.setIsPinned(false);
	        _allApps.put(componentName, newApp);
	        
	        ApplicationRunInformation appRunInfo = ApplicationRunInfoManager.generateApplicationRunInfo(componentName, true);
	        agingManager.applicationInstalled(appRunInfo);
	        saveAppAgingData(context);
    	}
    }

    public void applicationUpdated(Context context, ComponentName componentName)
    {
		AppInfo app = getApplicationFromComponentName(componentName);
		if(app != null){
			app.setIsNew(false);
			app.setIsUpdated(true);
		}
        ApplicationRunInformation appRunInfo = ApplicationRunInfoManager.generateApplicationRunInfo(componentName, false, true);
        agingManager.applicationUpdated(appRunInfo);
        saveAppAgingData(context);
    }

    public void applicationStarted(Context context, ComponentName componentName)
    {
		AppInfo app = getApplicationFromComponentName(componentName);
		if(app != null){
			app.setIsNew(false);
			app.setIsUpdated(false);
		}
        ApplicationRunInformation appRunInfo = ApplicationRunInfoManager.generateApplicationRunInfo(componentName, false);
        agingManager.applicationStarted(appRunInfo);
        saveAppAgingData(context);
    }

    public boolean applicationPinned(Context context, ComponentName componentName)
    {
        boolean isPinned = false;
        AppInfo appInfo = _allApps.get(componentName);
        if (appInfo != null)
        {
            appInfo.setIsPinned(!appInfo.isPinned());
            isPinned = appInfo.isPinned();
        }
        ApplicationRunInformation appRunInfo = agingManager.getApplicationRunInformation(componentName);
        agingManager.applicationPinned(appRunInfo);
        saveAppAgingData(context);

        return isPinned;
    }

    public void applicationRemoved(Context context, ComponentName component)
    {
        _allApps.remove(component);
        agingManager.applicationRemoved(component);
        saveAppAgingData(context);
    }

    public AppInfo getApplicationFromComponentName(ComponentName componentName)
    {

        if (_allApps.containsKey(componentName))
        {
            return _allApps.get(componentName);
        }

        return null;
    }

    public ApplicationRunInformation getApplicationRunInformation(ComponentName key)
    {
        return agingManager.getApplicationRunInformation(key);
    }

    public void saveAppAgingData(Context context)
    {
        ApplicationRunInformation.persistAppRunInfo(context, PREFS_APPS_AGING_DATA, agingManager.getAllAppRunInfo());
    }

    public void loadAppAgingData(Context context)
    {
        agingManager.setAllRunInfo(ApplicationRunInformation.loadAppRunInfo(context, PREFS_APPS_AGING_DATA));
    }
}
