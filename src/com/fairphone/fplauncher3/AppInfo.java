/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.fairphone.fplauncher3;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.fairphone.fplauncher3.compat.LauncherActivityInfoCompat;
import com.fairphone.fplauncher3.compat.UserHandleCompat;
import com.fairphone.fplauncher3.compat.UserManagerCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Represents an app in AllAppsView.
 */
public class AppInfo extends ItemInfo {
    private static final String TAG = "Launcher3.AppInfo";

    /**
     * The intent used to start the application.
     */
    Intent intent;

    /**
     * A bitmap version of the application icon.
     */
    Bitmap iconBitmap;

    /**
     * The time at which the app was first installed.
     */
    long firstInstallTime;

    ComponentName componentName;

    static final int DOWNLOADED_FLAG = 1;
    static final int UPDATED_SYSTEM_APP_FLAG = 2;

    int flags = 0;

    private APP_AGE mAppAge;

	private boolean mIsPinned;

    AppInfo() {
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT;
    }

    public Intent getIntent() {
        return intent;
    }

    protected Intent getRestoredIntent() {
        return null;
    }

    /**
     * Must not hold the Context.
     */
    public AppInfo(Context context, LauncherActivityInfoCompat info, UserHandleCompat user,
            IconCache iconCache, HashMap<Object, CharSequence> labelCache) {
        this.componentName = info.getComponentName();
        this.container = ItemInfo.NO_ID;

        flags = initFlags(info);
        firstInstallTime = info.getFirstInstallTime();
        iconCache.getTitleAndIcon(this, info, labelCache);
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(info.getComponentName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        long serialNumber = UserManagerCompat.getInstance(context).getSerialNumberForUser(user);
        intent.putExtra(EXTRA_PROFILE, serialNumber);
        this.user = user;
    }

    private static int initFlags(LauncherActivityInfoCompat info) {
        int appFlags = info.getApplicationInfo().flags;
        int flags = 0;
        if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
            flags |= DOWNLOADED_FLAG;

            if ((appFlags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                flags |= UPDATED_SYSTEM_APP_FLAG;
            }
        }
        return flags;
    }

    public AppInfo(AppInfo info) {
        super(info);
        componentName = info.componentName;
        title = info.title.toString();
        intent = new Intent(info.intent);
        flags = info.flags;
        firstInstallTime = info.firstInstallTime;
        iconBitmap = info.iconBitmap;
        mAppAge = info.mAppAge;
    }

    @Override
    public String toString() {
        return "ApplicationInfo(title=" + title.toString() + " id=" + this.id
                + " type=" + this.itemType + " container=" + this.container
                + " screen=" + screenId + " cellX=" + cellX + " cellY=" + cellY
                + " spanX=" + spanX + " spanY=" + spanY + " dropPos=" + Arrays.toString(dropPos)
                + " user=" + user + ")";
    }

    public static void dumpApplicationInfoList(String tag, String label, ArrayList<AppInfo> list) {
        Log.d(tag, label + " size=" + list.size());
        for (AppInfo info: list) {
            Log.d(tag, "   title=\"" + info.title + "\" iconBitmap="
                    + info.iconBitmap + " firstInstallTime="
                    + info.firstInstallTime);
        }
    }

    public ShortcutInfo makeShortcut() {
        return new ShortcutInfo(this);
    }
    
    /**
     * Returns the application title
     * @return application title
     */
    public String getApplicationTitle(){
    	return title.toString();
    }

    public ComponentName getComponentName() {
        return componentName;
    }

    public Bitmap getIconBitmap() {
        return iconBitmap;
    }
    
    public static enum APP_AGE{
        NEW_APP, FREQUENT_USE, RARE_USE 
    }
    
    public static long getAgeLevelInMiliseconds(APP_AGE age){
        long result = toMilliSeconds(365000);
        switch (age) {
            case NEW_APP:
                result = toMilliSeconds(1);
                break;
            case FREQUENT_USE:
                result = toMilliSeconds(15);
                break;
            case RARE_USE:
                result = toMilliSeconds(365000);
                break;
        }
        return result;
    }
    
    public static long toMilliSeconds(long days)
    {
        return days /* 24l * 60l * 60l*/ * 1000l;
    }
    
    public void setAge(APP_AGE age){
        mAppAge = age;
    }
    
    public APP_AGE getAge(){
        return mAppAge;
    }
    
    public void setIsPinned(boolean isPinned){
        mIsPinned = isPinned;
    }
    
    public boolean isPinned(){
        return mIsPinned;
    }
}
