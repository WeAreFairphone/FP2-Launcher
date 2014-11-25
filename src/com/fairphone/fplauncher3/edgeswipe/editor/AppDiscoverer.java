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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fairphone.fplauncher3.AppInfo;
import com.fairphone.fplauncher3.AppInfo.APP_AGE;
import com.fairphone.fplauncher3.LauncherModel;
import com.fairphone.fplauncher3.widgets.appswitcher.ApplicationRunInfoManager;
import com.fairphone.fplauncher3.widgets.appswitcher.ApplicationRunInformation;

import android.content.ComponentName;

public class AppDiscoverer {
    private static AppDiscoverer _instance = new AppDiscoverer();

    private Map<ComponentName, AppInfo> _allApps;

    private ApplicationRunInfoManager agingManager;

    public static AppDiscoverer getInstance() {
        return _instance;
    }

    private AppDiscoverer() {
        _allApps = new HashMap<ComponentName, AppInfo>();
        agingManager = new ApplicationRunInfoManager(false);
    }

    public void loadAllApps(ArrayList<AppInfo> allApps) {
        for (AppInfo appInfo : allApps) {
            ApplicationRunInformation appRunInfo = agingManager
                    .getApplicationRunInformation(appInfo.getComponentName());
            if (appRunInfo == null) {
                agingManager.applicationStarted(ApplicationRunInfoManager
                        .generateApplicationRunInfo(appInfo.getComponentName(), false));
            }

            _allApps.put(appInfo.getComponentName(), appInfo);
        }
    }

    public void loadAllAppsAgingInfo(List<ApplicationRunInformation> allApps) {
        agingManager.setAllRunInfo(allApps);
    }

    public ArrayList<AppInfo> getPackages() {
        ArrayList<AppInfo> appList = new ArrayList<AppInfo>();

        for (AppInfo appInfo : _allApps.values()) {
            appList.add(appInfo);
        }
        Collections.sort(appList, LauncherModel.getAppNameComparator());

        return appList;
    }

    public ArrayList<AppInfo> getUnusedApps() {
        ArrayList<AppInfo> appList = new ArrayList<AppInfo>();

        for (AppInfo appInfo : _allApps.values()) {
            if (appInfo.getAge() == APP_AGE.RARE_USE) {
                appList.add(appInfo);
            }
        }
        Collections.sort(appList, LauncherModel.getAppNameComparator());

        return appList;
    }

    public ArrayList<AppInfo> getUsedApps() {
        ArrayList<AppInfo> appList = new ArrayList<AppInfo>();

        for (AppInfo appInfo : _allApps.values()) {
            if (appInfo.getAge() != APP_AGE.RARE_USE) {
                appList.add(appInfo);
            }
        }
        Collections.sort(appList, LauncherModel.getAppNameComparator());

        return appList;
    }

    public List<ApplicationRunInformation> getApplicationAgingInfoList() {
        return agingManager.getAllAppRunInfo();
    }


    public void applicationInstalled(ApplicationRunInformation appRunInfo) {
        agingManager.applicationInstalled(appRunInfo); 
    }
    
    public void applicationStarted(ApplicationRunInformation appRunInfo) {
        agingManager.applicationStarted(appRunInfo);
    }
    
    public void applicationPinned(ApplicationRunInformation appRunInfo) {
        agingManager.applicationPinned(appRunInfo);
    }

    public void applicationRemoved(ComponentName component) {
        _allApps.remove(component);
        agingManager.applicationRemoved(component);
    }

    public AppInfo getApplicationFromComponentName(ComponentName componentName) {

        if (_allApps.containsKey(componentName)) {
            return _allApps.get(componentName);
        }

        return null;
    }

    public ApplicationRunInformation getApplicationRunInformation(ComponentName key) {
        return agingManager.getApplicationRunInformation(key);
    }
}
