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

package com.fairphone.fplauncher3.applifecycle;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.fairphone.fplauncher3.edgeswipe.editor.AppDiscoverer;
import com.fairphone.fplauncher3.widgets.appswitcher.AppSwitcherWidget;
import com.fairphone.fplauncher3.AppInfo;
import com.fairphone.fplauncher3.Launcher;
import com.fairphone.fplauncher3.R;

/**
 * Edit favorites activity implements functionality to edit your favorite apps
 * that will appear with the edge swipe.
 */
public class VerticalAppDrawerActivity extends Activity
{

    private static final String TAG = VerticalAppDrawerActivity.class.getSimpleName();

    private AgingAppsListAdapter mAllAppsListAdapter;

    private AgingAppsListAdapter mUnusedAppsListAdapter;

    private ArrayList<AppInfo> mUsedApps;

    private ArrayList<AppInfo> mUnusedApps;

    private ExpandedGridview mAllAppsGridView;

    private ExpandedGridview mUnusedAppsGridView;

    private TextView activeAppsDescription;

    private TextView unusedAppsDescription;

    private TextView mUnusedAppsButton;

    private ScrollView mScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fp_aging_app_drawer);

        Pair<ArrayList<AppInfo>, ArrayList<AppInfo>> appLists = AppDiscoverer.getInstance().getUsedAndUnusedApps();
        mUsedApps = appLists.first;
        mUnusedApps = appLists.second;

        setupAllAppsList();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        finishActivity();
    }

    /**
     * Setup the list with all the apps installed on the device.
     */
    private void setupAllAppsList()
    {
        mScroll = (ScrollView) findViewById(R.id.agingDrawerScroll);
        mScroll.setSmoothScrollingEnabled(true);

        mAllAppsGridView = (ExpandedGridview) findViewById(R.id.usedAppsGridView);
        mUnusedAppsGridView = (ExpandedGridview) findViewById(R.id.unusedAppsGridView);
        mUnusedAppsButton = (TextView) findViewById(R.id.unusedAppsTitle);

        activeAppsDescription = (TextView) findViewById(R.id.activeAppsDescription);
        unusedAppsDescription = (TextView) findViewById(R.id.unusedAppsDescription);

        //        mUnusedAppsButton.setOnClickListener(new OnClickListener()
        //        {
        //
        //            @Override
        //            public void onClick(View v)
        //            {
        //                if (mUnusedAppsGridView.getVisibility() == View.GONE)
        //                {
        //                    mUnusedAppsButton.setText(R.string.hide_unused_apps);
        //
        //                    mScroll.postDelayed(new Runnable()
        //                    {
        //                        @Override
        //                        public void run()
        //                        {
        //                            mScroll.smoothScrollTo(0, mAllAppsGridView.getBottom());
        //                        }
        //                    }, 100);
        //
        //                }
        //                else
        //                {
        //                    mUnusedAppsGridView.setVisibility(View.GONE);
        //
        //                    mScroll.postDelayed(new Runnable()
        //                    {
        //                        @Override
        //                        public void run()
        //                        {
        //                            mScroll.smoothScrollTo(0, 0);
        //                        }
        //                    }, 100);
        //                }
        //            }
        //        });

        setupListAdapter(mAllAppsGridView, mAllAppsListAdapter, mUsedApps);
        setupListAdapter(mUnusedAppsGridView, mUnusedAppsListAdapter, mUnusedApps);

        if (mUsedApps.isEmpty())
        {
            activeAppsDescription.setVisibility(View.VISIBLE);
        }
        else
        {
            activeAppsDescription.setVisibility(View.GONE);
        }

        if (mUnusedApps.isEmpty())
        {
            unusedAppsDescription.setVisibility(View.VISIBLE);

            //            mUnusedAppsButton.setTextColor(Color.argb(255, 51, 51, 51));
            mUnusedAppsButton.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    if (mUnusedAppsGridView.getVisibility() == View.GONE)
                    {
                        Toast.makeText(getApplicationContext(), R.string.empty_unused_apps_warning, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else
        {
            unusedAppsDescription.setVisibility(View.GONE);
        }
    }

    public void setupListAdapter(GridView listView, AgingAppsListAdapter appsListAdapter, ArrayList<AppInfo> appList)
    {
        appsListAdapter = new AgingAppsListAdapter(this);

        appsListAdapter.setAllApps(appList);

        listView.setLongClickable(true);

        listView.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                AppInfo appInfo = (AppInfo) view.getTag();
                startActivityViaLauncher(appInfo);

                finishActivity();
            }
        });

        listView.setOnItemLongClickListener(new OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
            {

                return true;
            }
        });

        listView.setAdapter(appsListAdapter);
    }

    /**
     * Capture the back button press, to make sure we save the selected apps
     * before exiting.
     */
    @Override
    public void onBackPressed()
    {
        finishActivity();
    }

    public void finishActivity()
    {
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();
    }

    public void startActivityViaLauncher(AppInfo appToLaunch)
    {
        if (appToLaunch != null)
        {

            Intent i = new Intent();
            i.setAction(AppSwitcherWidget.ACTION_APP_SWITCHER_LAUNCH_APP);
            Bundle extras = new Bundle();
            extras.putString(AppSwitcherWidget.EXTRA_LAUNCH_APP_NAME, appToLaunch.getApplicationTitle());
            extras.putString(AppSwitcherWidget.EXTRA_LAUNCH_APP_PACKAGE, appToLaunch.getComponentName().getPackageName());
            extras.putString(AppSwitcherWidget.EXTRA_LAUNCH_APP_CLASS_NAME, appToLaunch.getComponentName().getClassName());

            i.putExtras(extras);

            sendBroadcast(i);
        }
    }
}
