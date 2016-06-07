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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fairphone.fplauncher3.R;
import com.fairphone.fplauncher3.applifecycle.AppDrawerView;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Represents the run data for a specific application in the system.
 * It contains data for fast access like the component name and the icon.
 * All the info can still be obtained via the Application info.
 * 
 */
public class ApplicationRunInformation
{
    private static final int APP_RARE_USE_DAYS = 365;

	private static final String TAG = ApplicationRunInformation.class.getSimpleName();
	
	private static final String APP_RUN_INFO_SEPARATOR = ";";
	private static final String COMPONENT_NAME_SEPARATOR = ";";
    public static final long HOURS_IN_A_DAY = 24L;
    public static final long MINUTES_IN_HOUR = 60L;
    public static final long SECONDS_IN_MINUTE = 60L;
    public static final long MILLIS_IN_SECOND = 1000L;
    private ComponentName 	mComponentName;
	private int 			mRunCount;
    private Date			mLastExecution;
    private boolean mIsNewApp;
	private boolean mIsPinnedApp;
    private boolean mIsUpdatedApp;

	private APP_AGE mAppAge;
    
    /**
     * Create a base count zero Application Run information.
     * 
     * @param component The ComponentName of the application
     */
    public ApplicationRunInformation(ComponentName component){
    	this(component, 0);
    }
    
    /**
     * Create a application run information with a specific value for the count
     * the count value must be zero or above. 
     * 
     * @param component The ComponentName of the application
     * @param count the number of run times (used when starting)
     */
    public ApplicationRunInformation(ComponentName component, int count) {
    	if(component == null){
    		throw new IllegalArgumentException("Invalid value for ComponentName");
    	}
    	
    	setComponentName(component);
    	
    	if(count < 0){
    		throw new IllegalArgumentException("Run count cannot be negative");
    	}
    	
    	mRunCount = count;
	}

	public int getCount(){
    	return mRunCount;
    }
    
    public void incrementCount(){
    	mRunCount++;
    }
    
    public void decrementCount(){
    	if(mRunCount > 0){
    		mRunCount--;
    	}
    }

	public ComponentName getComponentName() {
		return mComponentName;
	}

	private void setComponentName(ComponentName component) {
		this.mComponentName = new ComponentName(component.getPackageName(), component.getClassName());
	}

	public Date getLastExecution() {
		return mLastExecution;
	}

	public void setLastExecution(Date lastExecution) {
		this.mLastExecution = lastExecution;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mComponentName == null) ? 0 : mComponentName.hashCode());
		result = prime * result
				+ ((mLastExecution == null) ? 0 : mLastExecution.hashCode());
		result = prime * result + mRunCount;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
            return true;
        }
		if (obj == null) {
            return false;
        }
		if (getClass() != obj.getClass()) {
            return false;
        }
		ApplicationRunInformation other = (ApplicationRunInformation) obj;
		if (mComponentName == null) {
			if (other.mComponentName != null) {
                return false;
            }
		} else if (!mComponentName.equals(other.mComponentName)) {
            return false;
        }
		if (mLastExecution == null) {
			if (other.mLastExecution != null) {
                return false;
            }
		} else if (!mLastExecution.equals(other.mLastExecution)) {
            return false;
        }
		if (mRunCount != other.mRunCount) {
            return false;
        }
		return true;
	}

	public void resetCount() {
		mRunCount = 0;
	}
	
	/**
	 * Serializes a component in order to be used has a map key
	 * @param componentName component to serialize
	 * @return the serialized component
	 */
	public static String serializeComponentName(ComponentName componentName) {
		StringBuffer sb = new StringBuffer();
    	
    	sb.append(componentName.getPackageName()).append(COMPONENT_NAME_SEPARATOR).append(componentName.getClassName());

		return sb.toString();
	}
	
	/**
	 * Transforms a string into a ComponentName
	 * @param componentNameString serialized component
	 * @return the ComponentName object
	 */
	public static ComponentName deserializeComponentName(String componentNameString) {
		String[] strings = componentNameString.split(COMPONENT_NAME_SEPARATOR);
    	
		return strings.length == 2 ? new ComponentName(strings[0],strings[1]): null;
	}

    public boolean isNewApp() {
        return mIsNewApp;
    }
    
    public void setIsNewApp(boolean isNewApp) {
        this.mIsNewApp = isNewApp;
    }
    
    public boolean isUpdatedApp() {
        return mIsUpdatedApp;
    }
    
    public void setIsUpdatedApp(boolean isUpdatedApp)
    {
        this.mIsUpdatedApp = isUpdatedApp;
    }

    public boolean isPinnedApp() {
        return mIsPinnedApp;
    } 
    
	public void setIsPinnedApp(boolean isPinnedApp) {
		this.mIsPinnedApp = isPinnedApp;
	}

	public static void persistAppRunInfo(Context context, String preferencesKey, List<ApplicationRunInformation> appsToSave)
    {
        SharedPreferences prefs = context.getSharedPreferences(preferencesKey, 0);

        // get the current prefs and clear to update
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.clear();

        for (ApplicationRunInformation appInfo : appsToSave)
        {
            String appSerialized = ApplicationRunInformation.serializeApplicationRunInformation(appInfo);

            editor.putString(ApplicationRunInformation.serializeComponentName(appInfo.getComponentName()), appSerialized);
        }

        editor.commit();
    }
	
	public static List<ApplicationRunInformation> loadAppRunInfo(Context context, String preferencesKey)
    {
		SharedPreferences prefs = context.getSharedPreferences(preferencesKey, 0);
	
	    List<ApplicationRunInformation> allApps = new ArrayList<ApplicationRunInformation>();
	
	    Map<String, ?> componentNames = prefs.getAll();
	    for (String component : componentNames.keySet())
	    {
	        String data = prefs.getString(component, "");
	
	        if (data.isEmpty())
	        {
	            continue;
	        }
	
	        allApps.add(deserializeApplicationRunInformation(component, data));
	    }
	    
	    return allApps;
	}
	
	public static String serializeApplicationRunInformation(ApplicationRunInformation appInfo)
    {
        StringBuffer sb = new StringBuffer();

        sb.append(appInfo.getCount()).append(APP_RUN_INFO_SEPARATOR).append(appInfo.getLastExecution().getTime()).append(APP_RUN_INFO_SEPARATOR)
                .append(appInfo.isNewApp()).append(APP_RUN_INFO_SEPARATOR).append(appInfo.isPinnedApp()).append(APP_RUN_INFO_SEPARATOR).append(appInfo.isUpdatedApp());

        return sb.toString();
    }
	
	public static ApplicationRunInformation deserializeApplicationRunInformation(String component, String data)
    {
        String[] splits = data.split(APP_RUN_INFO_SEPARATOR);
        int count;
        Date lastExecution = Calendar.getInstance().getTime();
        boolean isNewApp;
        boolean isUpdatedApp;
        boolean isPinnedApp;

        try
        {
            count = Integer.parseInt(splits[0]);
            lastExecution.setTime(Long.valueOf(splits[1]));
            isNewApp = Boolean.parseBoolean(splits[2]);
            isPinnedApp = Boolean.parseBoolean(splits[3]);
            isUpdatedApp = Boolean.parseBoolean(splits[4]);            
        } catch (NumberFormatException e)
        {
            e.printStackTrace();
            count = 0;
            lastExecution = Calendar.getInstance().getTime();
            isNewApp = false;
            isUpdatedApp = false;
            isPinnedApp = false;
        } catch (ArrayIndexOutOfBoundsException e)
        {
            e.printStackTrace();
            count = 0;
            lastExecution = Calendar.getInstance().getTime();
            isNewApp = false;
            isUpdatedApp = false;
            isPinnedApp = false;
        }

        ApplicationRunInformation appRunInfo = new ApplicationRunInformation(ApplicationRunInformation.deserializeComponentName(component), count);
        appRunInfo.mLastExecution = lastExecution;
        appRunInfo.mIsNewApp = isNewApp;
        appRunInfo.mIsUpdatedApp = isUpdatedApp;
        appRunInfo.mIsPinnedApp = isPinnedApp;

        return appRunInfo;
    }

	public static enum APP_AGE
    {
        FREQUENT_USE, RARE_USE
    }

    public static long getAgeLevelInMiliseconds(Context context, APP_AGE age)
    {
        long result = toMilliSeconds(APP_RARE_USE_DAYS);
        int frequentUseDays = getAppIdleLimitInDays(context);
        switch (age)
        {
            case FREQUENT_USE:
                result = toMilliSeconds(frequentUseDays);
                break;
            case RARE_USE:
                result = toMilliSeconds(APP_RARE_USE_DAYS);
                break;
        }
        return result;
    }

    public static int getAppIdleLimitInDays(Context context){
        int frequentUseDays = context.getResources().getInteger(R.integer.app_frequent_use_default);
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppDrawerView.APP_LIFECYCLE_PREFERENCES, Activity.MODE_PRIVATE);
        return sharedPreferences.getInt(AppDrawerView.APP_AGE_LIMIT_IN_DAYS, frequentUseDays);
    }

    public static void setAppIdleLimitInDays(Context context, int days){
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppDrawerView.APP_LIFECYCLE_PREFERENCES, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(AppDrawerView.APP_AGE_LIMIT_IN_DAYS, days);
        editor.commit();
    }

    public static long toMilliSeconds(long days)
    {
        return days * HOURS_IN_A_DAY * MINUTES_IN_HOUR * SECONDS_IN_MINUTE * MILLIS_IN_SECOND;
    }

    public void setAge(APP_AGE age)
    {
        mAppAge = age;
    }

    public APP_AGE getAge()
    {
        return mAppAge;
    }
}
