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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.fairphone.fplauncher3.edgeswipe.editor.AppDiscoverer;
import com.fairphone.fplauncher3.widgets.appswitcher.AppSwitcherWidget;
import com.fairphone.fplauncher3.AppInfo;
import com.fairphone.fplauncher3.BubbleTextView;
import com.fairphone.fplauncher3.DragSource;
import com.fairphone.fplauncher3.DropTarget.DragObject;
import com.fairphone.fplauncher3.ItemInfo;
import com.fairphone.fplauncher3.Launcher;
import com.fairphone.fplauncher3.R;

/**
 * Edit favorites activity implements functionality to edit your favorite apps
 * that will appear with the edge swipe.
 */
public class AppDrawerView extends FrameLayout implements View.OnLongClickListener, DragSource
{
	private static final String TAG = AppDrawerView.class.getSimpleName();
    
	private AgingAppsListAdapter mAllAppsListAdapter;

    private AgingAppsListAdapter mUnusedAppsListAdapter;

    private ArrayList<AppInfo> mUsedApps;

    private ArrayList<AppInfo> mUnusedApps;

    private ExpandedGridview mAllAppsGridView;

    private ExpandedGridview mUnusedAppsGridView;

    private TextView activeAppsDescription;

    private TextView unusedAppsDescription;

    private ScrollView mScroll;

	private Context mContext;

	private Launcher mLauncher;

	private boolean mIsDragging;

	private boolean mIsDragEnabled;

	private View mLastTouchedItem;

	private float mDragSlopeThreshold;
    
	public AppDrawerView(Context context) {
		super(context);
		init(context);
	}

	public AppDrawerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public AppDrawerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@SuppressLint("NewApi")
	public AppDrawerView(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

    protected void init(Context context)
    {
        mContext = context;
        this.removeAllViews();
        View view = inflate(mContext, R.layout.fp_aging_app_drawer, null);
        
        Pair<ArrayList<AppInfo>, ArrayList<AppInfo>> appLists = AppDiscoverer.getInstance().getUsedAndUnusedApps();
        mUsedApps = appLists.first;
        mUnusedApps = appLists.second;

        setupAllAppsList(view);
        
        addView(view);
    }

    public void refreshView(Context context, Launcher launcher){
    	mLauncher = launcher;
    	init(context);
    }
    
    /**
     * Setup the list with all the apps installed on the device.
     * @param view 
     */
    private void setupAllAppsList(View view)
    {
        mScroll = (ScrollView) view.findViewById(R.id.agingDrawerScroll);
        mScroll.setSmoothScrollingEnabled(true);

        mAllAppsGridView = (ExpandedGridview) view.findViewById(R.id.usedAppsGridView);
        mUnusedAppsGridView = (ExpandedGridview) view.findViewById(R.id.unusedAppsGridView);

        activeAppsDescription = (TextView) view.findViewById(R.id.activeAppsDescription);
        unusedAppsDescription = (TextView) view.findViewById(R.id.unusedAppsDescription);

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
        }
        else
        {
            unusedAppsDescription.setVisibility(View.GONE);
        }
    }

    public void setupListAdapter(GridView listView, AgingAppsListAdapter appsListAdapter, ArrayList<AppInfo> appList)
    {
        appsListAdapter = new AgingAppsListAdapter(mContext, mLauncher, this);

        appsListAdapter.setAllApps(appList);

        listView.setAdapter(appsListAdapter);
    }

	public void startActivityViaLauncher(AppInfo appToLaunch) {
		if (appToLaunch != null) {
			ComponentName compName = appToLaunch.getComponentName();
			if (compName != null) {
				PackageManager pacManager = mContext.getPackageManager();
				Intent launchIntent = pacManager
						.getLaunchIntentForPackage(compName.getPackageName());
				if (launchIntent != null) {
					launchIntent.setComponent(compName);
					mLauncher.startActivity(this, launchIntent, null);
				}
			}
		}
	}
	protected boolean beginDragging(View v) {
        boolean wasDragging = mIsDragging;
        mIsDragging = true;
        return !wasDragging;
    }

    protected void cancelDragging() {
        mIsDragging = false;
        mLastTouchedItem = null;
        mIsDragEnabled = false;
    }

    @Override
    public boolean onLongClick(View v) {
        // Return early if this is not initiated from a touch
//        if (!v.isInTouchMode()) return false;
        // When we have exited all apps or are in transition, disregard long clicks
//        if (!mLauncher.isAgingAppDrawerVisible() || !mLauncher.isAllAppsVisible() ||
//                mLauncher.getWorkspace().isSwitchingState()) return false;
//        // Return if global dragging is not enabled
//        if (!mLauncher.isDraggingEnabled()) return false;
       
        mLauncher.hideAgingAppDrawer();
        //mLauncher.startDrag(v, (ItemInfo)v.getTag(), this);
        mLauncher.showOverviewMode(true);
        mLauncher.getWorkspace().beginDragShared(v, this);
        
        postDelayed(new Runnable() {
            @Override
            public void run() {
                // We don't enter spring-loaded mode if the drag has been cancelled
//                if (mLauncher.getDragController().isDragging()) {
                    // Go into spring loaded mode (must happen before we startDrag())
                    mLauncher.enterSpringLoadedDragMode();
//                }
            }
        }, 150);
        
        return beginDragging(v);
    }

	@Override
	public boolean supportsFlingToDelete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsAppInfoDropTarget() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsDeleteDropTarget() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float getIntrinsicIconScaleFactor() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onFlingToDeleteCompleted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDropCompleted(View target, DragObject d,
			boolean isFlingToDelete, boolean success) {
		// TODO Auto-generated method stub
		
	}
}
