/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.app.Application;
import android.provider.Settings;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

public class LauncherApplication extends Application {
    private static final String TAG = LauncherApplication.class.getSimpleName();
    
    @Override
    public void onCreate() {
        super.onCreate();
        if (Settings.Global.getInt(getContentResolver(), Settings.Global.CRASHLYTICS_OPT_IN, 0) == 1)
        {
            Log.d(TAG, "Crash reports active.");
            Crashlytics.start(this);  
        }
        LauncherAppState.setApplicationContext(this);
        LauncherAppState.getInstance();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LauncherAppState.getInstance().onTerminate();
    }
}